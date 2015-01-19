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
package com.bennavetta.aeneas.cli.mesos;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import io.airlift.airline.Command;
import io.airlift.airline.Option;

import com.bennavetta.aeneas.cli.AeneasCommand;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.google.common.io.ByteStreams;

@Command(name = "slave", description = "Run a Mesos slave")
public class RunMesosSlave extends AeneasCommand
{
	@Option(name = "--no-docker", description = "Disable the Docker containerizer")
	public boolean disableDocker = false;
	
	@Option(name = {"-i", "--image"}, description = "Docker image to create container from")
	public String imageName = "mesos-slave";
	
	@Option(name = {"-p", "--port"}, description = "Port to run slave on")
	public int port = 5051;
	
	@Override
	protected int execute()
	{
		String containerId = dockerClient.createContainerCmd(imageName)
				.withEnv("MESOS_port=" + port)
				.exec().getId();
		
		StartContainerCmd start = dockerClient.startContainerCmd(containerId)
				.withNetworkMode("host");
		
		if(!disableDocker)
		{
			start = start.withBinds(
					new Bind("/var/run/docker.sock", new Volume("/var/run/docker.sock")),
					new Bind("/sys", new Volume("/sys")))
					.withPrivileged(true);
		}
		start.exec();
		
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
		
		return 0;
	}
}
