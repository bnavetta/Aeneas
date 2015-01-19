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

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import com.bennavetta.aeneas.cli.AeneasCommand;
import com.github.dockerjava.api.model.ExposedPort;

@Command(name = "create-master", description = "Create a Mesos master container")
public class CreateMesosMaster extends AeneasCommand
{
	@Option(name = "-i", description = "Docker image to create container from")
	public String imageName = "mesos-master";
	
	@Option(name = "-q", description = "Minimum number of quorum members")
	public int quorum = 1;
	
	@Option(name = "-p", description = "Port to expose the master API and UI on")
	public int port = 5050;
	
	@Arguments(description = "Name of the created container", required = true, title = "container name")
	public String containerName = "aeneas-mesos-master";
	
	@Override
	protected int execute()
	{
		String id = dockerClient.createContainerCmd(imageName)
			.withName(containerName)
			.withEnv("MESOS_quorum=" + quorum, "MESOS_port=" + port)
			.withExposedPorts(ExposedPort.tcp(port))
			.exec().getId();
		System.out.println(id);
		
		return 0;
	}

}
