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
package com.bennavetta.aeneas.mesos.master;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 *  Launch a Mesos master instance
 *  @see <a href="http://mesos.apache.org/documentation/latest/configuration/">Mesos Configuration</a>
 */
public final class MesosMaster
{
	private static final Logger LOG = LoggerFactory.getLogger(MesosMaster.class);

	private final Map<String, String> configuration = Maps.newHashMap();
	private final Path masterExecutable;

	private Process process;

	/**
	 * Create a new Mesos master handler that will run the given {@code mesos-master} executable.
	 * @param masterExecutable the path to {@code mesos-master}
	 */
	public MesosMaster(Path masterExecutable)
	{
		this.masterExecutable = Preconditions.checkNotNull(masterExecutable);
	}

	/**
	 * The size of the quorum of replicas when using 'replicated_log' based registry. It is imperative to set this
	 * value to be a majority of masters i.e., quorum > (number of masters)/2.
	 *
	 * @param size the quorum size
	 */
	public void setQuorumSize(int size)
	{
		setOption("quorum", String.valueOf(size));
	}

	/**
	 * Where to store the persistent information stored in the Registry.
	 * @param workDir a directory path
	 */
	public void setWorkDir(String workDir)
	{
		setOption("work_dir", workDir);
	}

	/**
	 * IP address to listen on
	 * @param ip an IP address
	 */
	public void setIp(String ip)
	{
		setOption("ip", ip);
	}

	/**
	 * Port to listen on (master default: 5050 and slave default: 5051)
	 * @param port a port number
	 */
	public void setPort(int port)
	{
		setOption("port", String.valueOf(port));
	}

	/**
	 * ZooKeeper URL (used for leader election amongst masters) May be one of:
	 * <ul>
	 *     <li>zk://host1:port1,host2:port2,.../path</li>
	 *     <li>zk://username:password@host1:port1,host2:port2,.../path</li>
	 *     <li>file://path/to/file (where file contains one of the above)</li>
	 * </ul>
	 * @param connection a ZooKeeper connection string
	 */
	public void setZk(String connection)
	{
		setOption("zk", connection);
	}

	public void setOption(String name, String value)
	{
		configuration.put(name, value);
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
		Preconditions.checkState(process == null, "Mesos master already running");

		ImmutableMap.Builder<String, String> environment = ImmutableMap.<String, String>builder();
		configuration.forEach((key, value) -> environment.put("MESOS_" + key.toUpperCase(), value));
		environment.put("MESOS_VERSION", "false"); // Mesos interprets the Dockerfile environment variable as the '--version' flag

		LOG.info("Starting Mesos master '{}' with configuration {}", masterExecutable, environment.build());
		process = new ProcessExecutor()
	        .command(masterExecutable.toAbsolutePath().toString())
	        .redirectError(System.err)
	        .redirectOutput(System.out)
	        .environment(environment.build())
	        .destroyOnExit()
	        .start().getProcess();
	}

	public void kill()
	{
		Preconditions.checkState(process != null, "Mesos master not running");
		process.destroy();
	}

	public int waitFor() throws InterruptedException
	{
		Preconditions.checkState(process != null, "Mesos master not running");
		return process.waitFor();
	}
}
