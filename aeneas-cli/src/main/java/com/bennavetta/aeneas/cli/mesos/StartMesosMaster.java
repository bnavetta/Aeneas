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
