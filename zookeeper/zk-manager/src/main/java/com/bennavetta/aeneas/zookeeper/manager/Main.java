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
package com.bennavetta.aeneas.zookeeper.manager;

import java.io.IOException;

import mousio.etcd4j.EtcdClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bennavetta.aeneas.Etcd;
import com.bennavetta.aeneas.zookeeper.ServerRegistry;
import com.bennavetta.aeneas.zookeeper.ZkException;
import com.bennavetta.aeneas.zookeeper.impl.etcd.EtcdServerRegistry;

public class Main
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args)
	{
		EtcdClient etcd = Etcd.createClient();

		LOG.info("Connected to etcd - {}", etcd.getVersion());

		ServerRegistry registry = new EtcdServerRegistry(etcd);

		try
		{
			ZooKeeperManager manager = new ZooKeeperManager(registry);
			manager.synchronizeMembers();
			manager.startMonitoring();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				manager.stopMonitoring();
				try
				{
					etcd.close();
				}
				catch (IOException e)
				{
					LOG.error("Error closing etcd client", e);
				}
			}));
		}
		catch (ZkException | IOException e)
		{
			LOG.error("Error connecting to cluster", e);
			System.exit(1);
		}
	}
}
