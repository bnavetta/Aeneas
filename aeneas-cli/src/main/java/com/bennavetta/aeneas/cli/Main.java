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
package com.bennavetta.aeneas.cli;

import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.ParseException;
import io.airlift.airline.help.Help;

import java.io.IOException;
import java.util.logging.LogManager;

import jersey.repackaged.com.google.common.collect.Lists;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.bennavetta.aeneas.cli.mesos.CreateMesosMaster;
import com.bennavetta.aeneas.cli.mesos.RunMesosSlave;
import com.bennavetta.aeneas.cli.mesos.StartMesosMaster;
import com.bennavetta.aeneas.cli.zookeeper.CreateNodes;
import com.bennavetta.aeneas.cli.zookeeper.ListNodes;
import com.bennavetta.aeneas.cli.zookeeper.RemoveNodes;
import com.bennavetta.aeneas.cli.zookeeper.StartNodes;
import com.bennavetta.aeneas.cli.zookeeper.StopNodes;
import com.bennavetta.aeneas.cli.zookeeper.ZooKeeperManager;

/**
 * Entry point for Aeneas' command line utility.
 */
public class Main
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		System.setProperty("org.slf4j.simpleLogger.log.mousio.etcd4j.transport.EtcdNettyClient", "error");
		System.setProperty("org.slf4j.simpleLogger.log.org.glassfish.jersey.filter.LoggingFilter", "error");
		System.setProperty("org.slf4j.simpleLogger.log.com.github.dockerjava.jaxrs.DockerCmdExecFactoryImpl", "error");
		
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		
		CliBuilder<Runnable> builder = Cli.<Runnable>builder("aeneas")
				.withDescription("A dynamic, distributed cloud orchestration system")
				.withDefaultCommand(Help.class)
				.withCommands(Help.class);
		
		builder.withGroup("zookeeper")
			.withDescription("Manage ZooKeeper clusters")
			.withDefaultCommand(ListNodes.class)
			.withCommands(ZooKeeperManager.class, ListNodes.class,
					CreateNodes.class, StartNodes.class,
					StopNodes.class, RemoveNodes.class);
		
		builder.withGroup("mesos")
			.withDescription("Manage Mesos clusters")
			.withCommands(CreateMesosMaster.class, StartMesosMaster.class, RunMesosSlave.class);

		Cli<Runnable> parser = builder.build();
		try
		{
			parser.parse(args).run();
		}
		catch(ParseException e)
		{
			System.err.println(e.getMessage());
			try
			{
				Help.help(parser.getMetadata(), Lists.newArrayList());
			}
			catch (IOException e1)
			{
				System.err.println("Error generating usage documentation: " + e1.getMessage());
			}
			System.exit(1);
		}
	}
}
