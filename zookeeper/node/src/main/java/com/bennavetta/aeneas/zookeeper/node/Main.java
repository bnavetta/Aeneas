/**
 * Copyright 2014 Benjamin Navetta
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
package com.bennavetta.aeneas.zookeeper.node;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Launch and register a ZooKeeper node
 */
public class Main
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args)
	{
		EtcdClient etcd = Optional.ofNullable(System.getenv("ETCD_SERVER"))
		                            .map(URI::create).map(EtcdClient::new)
									.orElseGet(EtcdClient::new);

		LOG.info("Connected to etcd - {}", etcd.getVersion());

		Registration registration = new Registration(etcd);
		ZooKeeper zookeeper = new ZooKeeper(Paths.get(System.getenv().getOrDefault("ZOO_DIR", "/opt/zookeeper")), registration);

		try
		{
			zookeeper.configure();
		}
		catch (IOException e)
		{
			LOG.error("Error configuring ZooKeeper", e);
			System.exit(1);
		}

		try
		{
			zookeeper.launch();
		}
		catch (IOException | TimeoutException | EtcdException e)
		{
			LOG.error("Error starting ZooKeeper", e);
			System.exit(1);
		}

		try
		{
			zookeeper.waitFor();
		}
		catch (InterruptedException e)
		{
			LOG.error("Error waiting for ZooKeeper to complete", e);
		}
		finally
		{
			try
			{
				// Do this in a shutdown hook (Runtime.getRuntime().addShutdownHook)
				zookeeper.unregister();
			}
			catch (EtcdException | TimeoutException | IOException e)
			{
				LOG.error("Error unregistering ZooKeeper node", e);
			}
		}

		try
		{
			etcd.close();
		}
		catch (IOException e)
		{
			LOG.error("Error closing etcd connection", e);
		}
	}
}
