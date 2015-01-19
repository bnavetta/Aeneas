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

import java.util.List;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import com.bennavetta.aeneas.cli.AeneasCommand;
import com.github.dockerjava.api.NotFoundException;
import com.github.dockerjava.api.NotModifiedException;

@Command(name = "stop", description = "Stop running ZooKeeper node containers")
public class StopNodes extends AeneasCommand
{
	@Arguments(description = "Container nodes to stop")
	public List<String> nodes;
	
	@Override
	protected int execute()
	{
		for(String node : nodes)
		{
			try
			{
				stop(node);
			}
			catch(NotFoundException | NotModifiedException e)
			{
				int id = Integer.parseInt(node);
				try
				{
					stop(ZooKeeperNodes.containerName(id));
				}
				catch(Exception er)
				{
					System.err.println("Unable to stop container " + ZooKeeperNodes.containerName(id));
					e.printStackTrace();
					return 1;
				}
			}
			catch(Exception e)
			{
				System.err.println("Unable to stop container");
				e.printStackTrace();
				return 1;
			}
		}
		return 0;
	}

	private void stop(String node)
	{
		dockerClient.stopContainerCmd(node).exec();
	}

}
