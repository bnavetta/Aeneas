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
package com.bennavetta.aeneas.cli.zookeeper;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import java.util.List;

import jersey.repackaged.com.google.common.collect.Iterables;

import com.bennavetta.aeneas.cli.AeneasCommand;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.google.common.base.Splitter;

@Command(name="create", description = "Create a ZooKeeper node container")
public class CreateNodes extends AeneasCommand
{
	@Option(name = "-i", description = "Docker image to create containers from")
	public String imageName = "zookeeper-node";
	
	@Arguments(description = "Nodes to create. Nodes can be specified as integers, "
			+ "which will be expanded into full nodes, or can be of the form '<container-name>=<peer-port>;<election-port>;<client-port>")
	public List<String> nodes;
	
	@Override
	protected int execute()
	{
		for(String nodeSpec : nodes)
		{
			if(nodeSpec.contains("="))
			{
				Iterable<String> parts = Splitter.on('=').split(nodeSpec);
				if(Iterables.size(parts) != 2)
				{
					System.err.println("Invalid node specification: " + nodeSpec);
					return 1;
				}
				
				String containerName = Iterables.get(parts, 0);
				Iterable<String> ports = Splitter.on(';').split(Iterables.get(parts, 1));
				if(Iterables.size(ports) != 3)
				{
					System.err.println("Invalid port specification: " + nodeSpec);
				}
				
				int peerPort = Integer.parseInt(Iterables.get(ports, 0));
				int electionPort = Integer.parseInt(Iterables.get(ports, 1));
				int clientPort = Integer.parseInt(Iterables.get(ports, 2));
				
				System.out.println(createNode(containerName, peerPort, electionPort, clientPort));
			}
			else
			{
				int id = Integer.parseInt(nodeSpec);
				String containerName = ZooKeeperNodes.containerName(id);
				int peerPort = ZooKeeperNodes.peerPort(id);
				int electionPort = ZooKeeperNodes.electionPort(id);
				int clientPort = ZooKeeperNodes.clientPort(id);
				
				System.out.println(createNode(containerName, peerPort, electionPort, clientPort));
			}
		}
		
		return 0;
	}
	
	private String createNode(String containerName, int peerPort, int electionPort, int clientPort)
	{
		CreateContainerResponse response = dockerClient.createContainerCmd(imageName)
			.withName(containerName)
			.withEnv("PEER_PORT=" + peerPort, "ELECTION_PORT=" + electionPort, "CLIENT_PORT=" + clientPort)
			.withExposedPorts(ExposedPort.tcp(clientPort), ExposedPort.tcp(electionPort), ExposedPort.tcp(clientPort))
			.exec();
		
		return response.getId();
	}
}
