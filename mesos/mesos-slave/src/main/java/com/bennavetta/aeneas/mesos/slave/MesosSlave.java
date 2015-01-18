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
package com.bennavetta.aeneas.mesos.slave;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Launch a Mesos slave instance
 */
public class MesosSlave
{
	// TODO: reduce code duplication between this and MesosMaster

	private static final Logger LOG = LoggerFactory.getLogger(MesosSlave.class);

	private final Map<String, String> configuration = Maps.newHashMap();
	private final Path executable;
	private Process process;

	public MesosSlave(Path executable)
	{
		this.executable = Preconditions.checkNotNull(executable);
	}

	public void setOption(String name, String value)
	{
		configuration.put(name, value);
	}

	/**
	 * IP address to listen on
	 *
	 * @param ip an IP address
	 */
	public void setIp(String ip)
	{
		setOption("ip", ip);
	}

	/**
	 * Port to listen on (master default: 5050 and slave default: 5051)
	 *
	 * @param port a port number
	 */
	public void setPort(int port)
	{
		setOption("port", String.valueOf(port));
	}

	/**
	 * Specifies how to connect to a master or quorum of masters.
	 * This can be
	 * <ul>
	 * <li>A comma-separated list of master hostnames or addresses</li>
	 * <li>A ZooKeeper registration path</li>
	 * <li>A path to a file with either of the above</li>
	 * </ul>
	 *
	 * @param master a master specification
	 */
	public void setMaster(String master)
	{
		setOption("master", master);
	}

	/**
	 * Set the containerizer implementations to use.
	 * @param containerizers any of {@code mesos}, {@code docker}, or {@code external}
	 */
	public void setContainerizers(String... containerizers)
	{
		setOption("containerizers", Joiner.on(',').join(containerizers));
	}

	public void configureFromEnvironment()
	{
		final ImmutableSet<String> ignoredKeys = ImmutableSet.of("version", "download_sha1", "download_url");

		System.getenv().forEach((key, value) -> {
			if(key.startsWith("MESOS_"))
			{
				String argName = key.substring(6).toLowerCase();
				if(!ignoredKeys.contains(argName))
				{
					setOption(argName, value);
				}
			}
		});
	}

	public void launch() throws IOException
	{
		Preconditions.checkState(process == null, "Mesos slave already running");

		ImmutableMap.Builder<String, String> environment = ImmutableMap.<String, String>builder();
		configuration.forEach((key, value) -> environment.put("MESOS_" + key.toUpperCase(), value));
		environment.put("MESOS_VERSION", "false"); // Mesos interprets the Dockerfile environment variable as the '--version' flag

		LOG.info("Starting Mesos slave '{}' with configuration {}", executable, environment.build());
		process = new ProcessExecutor()
				.command(executable.toAbsolutePath().toString())
				.redirectError(System.err)
				.redirectOutput(System.out)
				.environment(environment.build())
				.destroyOnExit()
				.start().getProcess();
	}

	public void kill()
	{
		Preconditions.checkState(process != null, "Mesos slave not running");
		process.destroy();
	}

	public int waitFor() throws InterruptedException
	{
		Preconditions.checkState(process != null, "Mesos slave not running");
		return process.waitFor();
	}
}
