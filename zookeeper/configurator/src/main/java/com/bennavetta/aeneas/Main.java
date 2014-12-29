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
package com.bennavetta.aeneas;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class Main
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args)
	{
		/*
		Flow:

		1. Wait for `state=started`
		2. Initialize a client with the value of `nodes
		3. Watch for changes in `nodes` and reconfigure when any occur
		 */
		try(EtcdClient etcd = createClient(args))
		{
			LOG.info("Connected to etcd - {}", etcd.getVersion());

			StartupLatch latch = new StartupLatch(etcd);
			latch.waitForStartup();

			LOG.info("Waiting for ZooKeeper cluster");
			latch.getSignal().await();
			LOG.info("ZooKeeper cluster started");

			LOG.info("Connecting to ZooKeeper");
			ZooKeeperManager manager = new ZooKeeperManager(etcd);

			LOG.info("Watching etcd for updates");
			manager.watch();
		}
		catch(Exception e)
		{
			LOG.error("Error", e);
			System.exit(1);
		}
	}

	private static EtcdClient createClient(String[] args)
	{
		if(args.length > 0)
		{
			return new EtcdClient(Arrays.asList(args).stream().map(URI::create).toArray(URI[]::new));
		}

		return new EtcdClient();
	}
}
