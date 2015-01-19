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

import io.airlift.airline.Command;
import io.airlift.airline.Option;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.bennavetta.aeneas.cli.AeneasCommand;
import com.google.common.io.ByteStreams;

@Command(name = "manager", description = "Run a ZooKeeper manager")
public class ZooKeeperManager extends AeneasCommand
{
	@Option(name = "-i", description = "Docker image to create container from")
	public String imageName = "zookeeper-manager";
	
	@Option(name = "-c", description = "Name of the manager container")
	public String containerName = "aeneas-zookeeper-manager";
	
	@Option(name = "-d", description = "Run manager in the background")
	public boolean background = false;
	
	@Override
	protected int execute()
	{
		String containerId = dockerClient.createContainerCmd(imageName).withName(containerName).exec().getId();
		dockerClient.startContainerCmd(containerId).withNetworkMode("host").exec();
		
		if(background)
		{
			System.out.println(containerId);
		}
		else
		{
//			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//				dockerClient.removeContainerCmd(containerId).withForce().exec();
//			}));
			
			try(InputStream log = dockerClient.logContainerCmd(containerId)
				.withFollowStream()
				.withStdErr()
				.withStdOut()
				.exec())
			{
				ByteStreams.copy(log, System.out);
			}
			catch(IOException e)
			{
				if(!(e instanceof EOFException))
				{
					System.err.println("Error reading container logs: " + e);
					return 1;
				}
			}
			finally
			{
				dockerClient.removeContainerCmd(containerId).withForce().exec();
			}
		}
		
		return 0;
	}

}
