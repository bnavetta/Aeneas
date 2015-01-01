/**
 * Copyright 2014 Benjamin Navetta
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
package com.bennavetta.aeneas.zookeeper.node;

import com.google.common.base.Preconditions;
import mousio.etcd4j.responses.EtcdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

/**
 * Runs and watches a ZooKeeper instance
 */
public class ZooKeeper
{
	private static final Logger LOG = LoggerFactory.getLogger(ZooKeeper.class);

	private final Path installDir;

	private final Registration registration;
	private Configuration configuration;
	private String serverSpec;

	private Process process;

	public ZooKeeper(Path installDir, Registration registration)
	{
		this.installDir = installDir;
		this.registration = registration;
		Preconditions.checkArgument(isValidLocation(), "Must specify a valid ZooKeeper installation");
	}

	public boolean isValidLocation()
	{
		Path serverBin = installDir.resolve("bin/zkServer.sh");
		return Files.isExecutable(serverBin);
	}

	public void configure() throws IOException
	{
		LOG.info("Configuring ZooKeeper installation in {}", installDir);

		configuration = new Configuration();
		configuration.addDefaults();
		configuration.addFromEnvironment();
		try
		{
			configuration.addServers(registration);
			configuration.obtainId(registration);
		}
		catch (EtcdException | TimeoutException e)
		{
			throw new IOException("Communicating with etcd failed", e);
		}

		String serverAddress = Registration.getLocalAddress().getHostAddress();

		int peerPort = Integer.parseInt(System.getenv().getOrDefault("PEER_PORT", "2888"));
		int electionPort = Integer.parseInt(System.getenv().getOrDefault("ELECTION_PORT", "3888"));
		int clientPort = Integer.parseInt(System.getenv().getOrDefault("CLIENT_PORT", "2181"));

		serverSpec = String.format("%s:%d:%d;%d", serverAddress, peerPort, electionPort, clientPort);
		configuration.setDynamic("server." + configuration.getMyId(), serverSpec);

		Path configRoot = installDir.resolve("conf");
		Files.createDirectories(configRoot);

		configuration.writeId();
		configuration.writeConfiguration(configRoot.resolve("zoo.cfg"), configRoot.resolve("zoo.cfg.dynamic"));

	}

	public void launch() throws IOException, TimeoutException, EtcdException
	{
		Preconditions.checkState(process == null, "Already running");
		Preconditions.checkState(configuration != null, "ZooKeeper not configured");

		LOG.info("Starting ZooKeeper with ID {} and server specification {}", configuration.getMyId(), serverSpec);
		process = new ProcessExecutor()
				.command(installDir.resolve("bin/zkServer.sh").toAbsolutePath().toString(), "start-foreground")
				.redirectError(System.err)
				.redirectOutput(System.out)
				.destroyOnExit()
				.start().getProcess();

		registration.register(configuration.getMyId(), serverSpec);
		registration.startCluster();
	}

	public void kill()
	{
		Preconditions.checkState(process != null, "ZooKeeper not started");
		process.destroy();
	}

	public int waitFor() throws InterruptedException
	{
		Preconditions.checkState(process != null, "ZooKeeper not started");
		return process.waitFor();
	}

	public void unregister() throws EtcdException, TimeoutException, IOException
	{
		registration.unregister(configuration.getMyId());
	}
}
