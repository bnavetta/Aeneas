/**
 * Copyright 2015 Benjamin Navetta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bennavetta.aeneas.mesos.slave.docker;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Launch a nested Docker daemon
 * @see <a href="https://github.com/jpetazzo/dind">dind</a>
 */
public final class Daemon
{
	private static final Logger LOG = LoggerFactory.getLogger(Daemon.class);
	
	private static void mountFilesystems() throws IOException
	{
		Path cgroup = Paths.get("/sys/fs/cgroup");
		if(!Files.isDirectory(cgroup))
			Files.createDirectory(cgroup);

		LOG.debug("Creating '{}' tmpfs", cgroup);
		if(!isMountpoint(cgroup))
		{
			try
			{
				mount(cgroup, "cgroup", "tmpfs", "uid=0,gid=0,mode=0755", false);
			}
			catch(IOException e)
			{
				throw new IOException("Could not make a tmpfs mount. Was the --privileged flag used?", e);
			}
		}

		Path security = Paths.get("/sys/kernel/security");
		LOG.debug("Creating security file system in '{}'", security);
		if(Files.isDirectory(security) && !isMountpoint(security))
		{
			try
			{
				mount(security, "none", "securityfs", null, true);
			}
			catch(IOException e)
			{
				LOG.warn("Could not mount {}. AppArmor detection and --privileged mode might break.", security);
			}
		}

		String cgroupsText = new String(Files.readAllBytes(Paths.get("/proc/1/cgroup")));
		List<String> cgroups = StreamSupport.stream(Splitter.on('\n').split(cgroupsText).spliterator(), false)
									.filter(s -> !s.trim().isEmpty())
									.map(s -> Splitter.on(':').split(s))
		                            .map(i -> Iterables.get(i, 1))
									.collect(Collectors.toList());
		for(String subsystem : cgroups)
		{
			LOG.debug("Creating '{}' cgroup", subsystem);
			Path subsysPath = cgroup.resolve(subsystem);
			if(!Files.isDirectory(subsysPath))
				Files.createDirectory(subsysPath);
			if(!isMountpoint(subsysPath))
			{
				mount(subsysPath, "cgroup", "cgroup", subsystem, false);
			}

			/*
			 * Address some bugs
			 * See https://github.com/jpetazzo/dind/blob/master/wrapdocker
 			 */
			if(subsystem.startsWith("name="))
			{
				String name = subsystem.substring(6);
				Files.createSymbolicLink(cgroup.resolve(name), Paths.get(subsystem));
			}

			if(subsystem.equals("cpuacct,cpu"))
			{
				Files.createSymbolicLink(cgroup.resolve("cpu,cpuacct"), Paths.get(subsystem));
			}
		}

		if(!cgroupsText.contains(":devices:"))
		{
			LOG.warn("The 'devices' cgroup should be in its own hierarchy.");
		}

		if(!Iterables.contains(Splitter.on(CharMatcher.BREAKING_WHITESPACE).split(cgroupsText), "devices"))
		{
			LOG.warn("The 'devices' cgroup does not appear to be mounted.");
		}
	}

	private static boolean isMountpoint(Path path) throws IOException
	{
		try
		{
			int exit = new ProcessExecutor("mountpoint", "-q", path.toAbsolutePath().toString())
					.exitValueAny()
					.execute()
					.getExitValue();

			return exit == 0;
		}
		catch(InterruptedException | TimeoutException e)
		{
			throw new IOException("Unable to wait for 'mountpoint' to exit", e);
		}
	}

	private static void mount(Path path, String device, String type, String options, boolean mtab) throws IOException
	{
		List<String> commandLine = Lists.newArrayList("mount");
		if(!mtab)
			commandLine.add("-n");

		commandLine.add("-t");
		commandLine.add(type);

		if(options != null)
		{
			commandLine.add("-o");
			commandLine.add(options);
		}

		commandLine.add(device);
		commandLine.add(path.toString());

		try
		{
			new ProcessExecutor(commandLine).execute();
		}
		catch(InterruptedException | TimeoutException e)
		{
			throw new IOException("Unable to wait for 'mount' to exit", e);
		}
	}

	private static void removePidfile() throws IOException
	{
		Path pidfile = Paths.get("/var/run/docker.pid");
		if(Files.exists(pidfile))
		{
			Files.delete(pidfile);
		}
	}

	public static Process launchDaemon(List<String> args) throws IOException
	{
		mountFilesystems();
		removePidfile();

		List<String> commandLine = Lists.newArrayList("docker", "-d");
		commandLine.addAll(args);

		return new ProcessExecutor(commandLine)
				.redirectOutput(Slf4jStream.of("DockerDaemon").asInfo())
				.redirectError(Slf4jStream.of("DockerDaemon").asError())
				.start().getProcess();
	}
}
