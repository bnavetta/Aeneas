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

import com.bennavetta.aeneas.cli.zookeeper.ListNodes;

import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.help.Help;

/**
 * Entry point for Aeneas' command line utility.
 */
public class Main
{
	public static void main(String[] args)
	{
		System.setProperty("org.slf4j.simpleLogger.log.mousio.etcd4j.transport.EtcdNettyClient", "error");
		
		CliBuilder<Runnable> builder = Cli.<Runnable>builder("aeneas")
				.withDescription("A dynamic, distributed cloud orchestration system")
				.withDefaultCommand(Help.class)
				.withCommand(Help.class);
		
		builder.withGroup("zookeeper")
			.withDefaultCommand(ListNodes.class)
			.withCommands(ListNodes.class);

		Cli<Runnable> parser = builder.build();
		parser.parse(args).run();
	}
}
