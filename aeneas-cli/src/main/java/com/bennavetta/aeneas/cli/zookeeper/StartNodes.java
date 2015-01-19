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

import java.util.List;

import com.bennavetta.aeneas.cli.AeneasCommand;
import com.github.dockerjava.api.NotFoundException;
import com.github.dockerjava.api.NotModifiedException;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;

@Command(name = "start", description = "Start existing ZooKeeper node containers")
public class StartNodes extends AeneasCommand
{
	/*
	 * Spawning a cluster:
	 * 
	 * 1. Start three nodes, one after another, allowing time for each to register itself before starting the next. Otherwise, they
	 *    won't connect to each other and configuring the quorum will fail.
	 * 2. Start the manager
	 * 3. Start any number of other nodes
	 * 
	 * TODO: create a command for this
	 */
	
	@Arguments(description = "Container nodes to start")
	public List<String> nodes;
	
	@Override
	protected int execute()
	{		
		for(String node : nodes)
		{
			try
			{
				start(node);
			}
			catch(NotFoundException | NotModifiedException e)
			{
				int id = Integer.parseInt(node);
				try
				{
					start(ZooKeeperNodes.containerName(id));
				}
				catch(Exception er)
				{
					System.err.println("Unable to start container " + ZooKeeperNodes.containerName(id));
					e.printStackTrace();
					return 1;
				}
			}
			catch(Exception e)
			{
				System.err.println("Unable to start container");
				e.printStackTrace();
				return 1;
			}
		}
		return 0;
	}
	
	private void start(String node)
	{
		InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(node).exec();
		
		Ports ports = new Ports();
		
		for(ExposedPort port : containerInfo.getConfig().getExposedPorts())
		{
			ports.bind(port, new Ports.Binding(port.getPort()));
		}
		
		dockerClient.startContainerCmd(node)
			.withNetworkMode("host")
			.withPortBindings(ports)
			.exec();
	}

}
