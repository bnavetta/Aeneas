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

import com.bennavetta.aeneas.cli.AeneasCommand;
import com.bennavetta.aeneas.cli.Table;
import com.bennavetta.aeneas.zookeeper.Servers;
import com.bennavetta.aeneas.zookeeper.ZkException;
import com.bennavetta.aeneas.zookeeper.ZkServer;
import com.google.common.collect.ImmutableList;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

/**
 * List registered ZooKeeper nodes
 */
@Command(name="list", description = "List registered ZooKeeper nodes")
public class ListNodes extends AeneasCommand
{
	@Option(name = "-q", description = "Only display node ids")
	public boolean quiet;

	@Option(name = "-c", description = "Display as a connection specification")
	public boolean connectionSpec;

	@Option(name = "-s", description = "Display as a server configuration block")
	public boolean serverConfig;

	@Override
	protected void execute()
	{
		try
		{
			ImmutableList<ZkServer> servers = zookeeperRegistry.getServers();

			if(quiet)
			{
				for(ZkServer server : servers)
				{
					System.out.println(server.getId());
				}
			}
			else if(connectionSpec)
			{
				System.out.println(Servers.toConnectionString(servers));
			}
			else if(serverConfig)
			{
				Servers.toServerSpecification(servers)
				       .forEach((key, value) -> System.out.println(key + "=" + value));
			}
			else
			{
				Table table = new Table();
				table.addRow("ID", "ADDRESS", "ROLE", "PEER PORT", "ELECTION PORT", "CLIENT PORT");

				for(ZkServer server : servers)
				{
					table.addRow(String.valueOf(server.getId()),
					             server.getAddress().toString(),
					             server.getRole().toString(),
					             String.valueOf(server.getPeerPort()),
					             String.valueOf(server.getElectionPort()),
					             String.valueOf(server.getClientPort()));
				}
				System.out.println(table);
			}
		}
		catch (ZkException e)
		{
			System.err.println("Error retrieving ZooKeeper cluster: " + e.getLocalizedMessage());
			System.exit(1);
		}
	}
}
