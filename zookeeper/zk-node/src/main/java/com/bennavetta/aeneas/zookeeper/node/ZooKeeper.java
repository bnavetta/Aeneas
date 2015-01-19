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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import com.bennavetta.aeneas.Networking;
import com.bennavetta.aeneas.zookeeper.IdGenerator;
import com.bennavetta.aeneas.zookeeper.ServerRegistry;
import com.bennavetta.aeneas.zookeeper.ZkException;
import com.bennavetta.aeneas.zookeeper.ZkServer;
import com.bennavetta.aeneas.zookeeper.ZkServer.Role;
import com.google.common.base.Preconditions;
import com.google.common.net.HostSpecifier;

/**
 * Runs and watches a ZooKeeper instance
 */
public class ZooKeeper
{
	private static final Logger LOG = LoggerFactory.getLogger(ZooKeeper.class);

	private final Path installDir;

	private final ServerRegistry registry;
	private final IdGenerator idGenerator;
	private Configuration configuration;
	private ZkServer server;

	private Process process;

	public ZooKeeper(Path installDir, ServerRegistry registry, IdGenerator idGenerator)
	{
		this.installDir = Preconditions.checkNotNull(installDir);
		this.registry = Preconditions.checkNotNull(registry);
		this.idGenerator = Preconditions.checkNotNull(idGenerator);

		Preconditions.checkArgument(isValidLocation(), "Must specify a valid ZooKeeper installation");
	}

	public boolean isValidLocation()
	{
		Path serverBin = installDir.resolve("bin/zkServer.sh");
		return Files.isExecutable(serverBin);
	}

	public void configure() throws IOException, ZkException
	{
		LOG.info("Configuring ZooKeeper installation in {}", installDir);

		configuration = new Configuration();
		configuration.addDefaults();
		configuration.addFromEnvironment();

		configuration.addServers(registry);
		configuration.setMyId(Util.obtainId(idGenerator, configuration));

		String serverAddress = Networking.getLocalAddress().getHostAddress();

		int peerPort = Integer.parseInt(System.getenv().getOrDefault("PEER_PORT", "2888"));
		int electionPort = Integer.parseInt(System.getenv().getOrDefault("ELECTION_PORT", "3888"));
		int clientPort = Integer.parseInt(System.getenv().getOrDefault("CLIENT_PORT", "2181"));

		server = new ZkServer(configuration.getMyId(),
		                               HostSpecifier.fromValid(serverAddress),
		                               Role.defaultRole(),
		                               peerPort,
		                               electionPort,
		                               clientPort);

		configuration.setDynamic("server." + server.getId(), server.toConnectionSpec());

		Path configRoot = installDir.resolve("conf");
		Files.createDirectories(configRoot);

		configuration.writeId();
		configuration.writeConfiguration(configRoot.resolve("zoo.cfg"), configRoot.resolve("zoo.cfg.dynamic"));

	}

	public void launch() throws ZkException, IOException
	{
		Preconditions.checkState(process == null, "Already running");
		Preconditions.checkState(configuration != null, "ZooKeeper not configured");

		LOG.info("Starting ZooKeeper with server specification {}", server.toServerSpec());
		process = new ProcessExecutor()
				.command(installDir.resolve("bin/zkServer.sh").toAbsolutePath().toString(), "start-foreground")
				.redirectError(System.err)
				.redirectOutput(System.out)
				.destroyOnExit()
				.start().getProcess();

		registry.register(server);
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

	public void deregister() throws ZkException
	{
		registry.deregister(server);
	}
}
