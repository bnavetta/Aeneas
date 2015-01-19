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

import com.bennavetta.aeneas.cli.AeneasCommand;

@Command(name = "start-master", description = "Start an existing Mesos master container")
public class StartMesosMaster extends AeneasCommand
{
	@Arguments(description = "Name of container to start", title = "container name", required = true)
	public String master;
	
	@Override
	protected int execute()
	{
		dockerClient.startContainerCmd(master)
			.withNetworkMode("host")
			.exec();
		return 0;
	}

}
