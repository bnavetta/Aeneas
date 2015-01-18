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
package com.bennavetta.aeneas.mesos.master;

import com.bennavetta.aeneas.zookeeper.ServerRegistry;
import com.bennavetta.aeneas.zookeeper.Servers;
import com.bennavetta.aeneas.zookeeper.ZkException;
import com.bennavetta.aeneas.zookeeper.impl.etcd.EtcdServerRegistry;
import mousio.etcd4j.EtcdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class Main
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws IOException
	{
		EtcdClient etcd = Optional.ofNullable(System.getenv("ETCD_SERVER"))
		                          .map(URI::create).map(EtcdClient::new)
		                          .orElseGet(EtcdClient::new);
		LOG.info("Connected to etcd - {}", etcd.getVersion());

		try
		{
			ServerRegistry registry = new EtcdServerRegistry(etcd);

			Path mesosPrefix = Paths.get(System.getenv().getOrDefault("MESOS_PREFIX", "/usr/local"));
			Path masterExecutable = mesosPrefix.resolve("sbin/mesos-master");

			MesosMaster master = new MesosMaster(masterExecutable);

			master.setIp(getLocalAddress().getHostAddress());
			master.setPort(5050);
			master.setWorkDir("/var/lib/mesos/master");
			master.setZk(Servers.toConnectionString(true, registry.getServers(), "aeneas/mesos"));
			master.configureFromEnvironment();

			LOG.info("Starting Mesos master");
			master.launch();
			int result = master.waitFor();
			LOG.info("Mesos exited with value {}", result);

		}
		catch (ZkException e)
		{
			LOG.error("Error obtaining ZooKeeper node list", e);
		}
		catch (InterruptedException e)
		{
			LOG.error("Error waiting for Mesos to complete", e);
		}
		finally
		{
			if(etcd != null)
			{
				etcd.close();
			}
		}
	}

	private static InetAddress getLocalAddress()  throws IOException
	{
		try(Socket socket = new Socket("8.8.8.8", 53))
		{
			return socket.getLocalAddress();
		}
	}
}
