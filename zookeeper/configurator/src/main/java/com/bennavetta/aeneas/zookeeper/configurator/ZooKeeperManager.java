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
package com.bennavetta.aeneas.zookeeper.configurator;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.requests.EtcdKeyGetRequest;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Handles connecting to and configuring ZooKeeper.
 */
public class ZooKeeperManager implements Watcher
{
	private static Logger LOG = LoggerFactory.getLogger(ZooKeeperManager.class);

	public static final String NODES_KEY = "aeneas/zookeeper/nodes";

	private final EtcdClient etcd;
	private final ZooKeeper zk;

	public ZooKeeperManager(EtcdClient etcd) throws IOException, TimeoutException, EtcdException
	{
		this.etcd = etcd;

		EtcdKeysResponse response = etcd.getDir(NODES_KEY).send().get();

		String connectString = toConnectString(response.node);

		this.zk = new ZooKeeper(connectString, 5000, this);
	}

	public ZooKeeper getZookeeper()
	{
		return zk;
	}

	/**
	 * Synchronously monitor etcd for ZooKeeper changes
	 */
	public void watch()
	{
		try
		{
			doWatch(-1);
		}
		catch(Exception e)
		{
			LOG.error("Error monitoring etcd", e);
			watch();
		}
	}

	private void doWatch(long index) throws IOException, TimeoutException, EtcdException, KeeperException, InterruptedException
	{
		EtcdKeyGetRequest request = etcd.getDir(NODES_KEY).recursive();
		if(index != -1)
		{
			request = request.waitForChange(index);
		}
		else
		{
			request = request.waitForChange();
		}

		EtcdKeysResponse response = request.send().get();

		switch(response.action)
		{
			case set:
			case create:
				addServer(response.node);
				break;
			case update:
				updateServer(response.node);
				break;
			case delete:
				removeServer(response.node);
				break;
			default:
				break;
		}

		doWatch(response.etcdIndex);
	}

	private void addServer(EtcdNode node) throws KeeperException, InterruptedException
	{
		String server = String.format("server.%s=%s", zookeeperId(node.key), node.value);
		LOG.info("Adding '{}'", server);

		byte[] newConfig = zk.reconfig(Lists.newArrayList(server), null, null, -1, null);
		LOG.debug("New ZooKeeper configuration: '{}'", new String(newConfig));
	}

	private void removeServer(EtcdNode node) throws KeeperException, InterruptedException
	{
		String serverId = zookeeperId(node.key);
		LOG.info("Removing server {}", serverId);

		byte[] newConfig = zk.reconfig(null, Lists.newArrayList(serverId), null, -1, null);
		LOG.debug("New ZooKeeper configuration: '{}'", new String(newConfig));
	}

	private void updateServer(EtcdNode node) throws KeeperException, InterruptedException
	{
		String serverId = zookeeperId(node.key);
		String server = String.format("server.%s=%s", serverId, node.value);

		LOG.info("Updating '{}'", server);

		byte[] newConfig = zk.reconfig(Lists.newArrayList(server), Lists.newArrayList(serverId), null, -1, null);
		LOG.debug("New ZooKeeper configuration: '{}'", new String(newConfig));
	}

	@Override
	public void process(WatchedEvent event)
	{
		LOG.debug("Received ZooKeeper event: {}", event);
	}

	private static String toConnectString(EtcdNode node)
	{
		Preconditions.checkArgument(node.dir, "Node must be a directory");
		return node.nodes.stream().map(n -> {
			String serverAddress = Splitter.on(':').split(n.value).iterator().next();
			String clientPart = Iterables.get(Splitter.on(';').split(n.value), 1);
			if (clientPart.indexOf(':') != -1)
			{
				return clientPart;
			}
			else
			{
				return serverAddress + ':' + clientPart;
			}
		}).collect(Collectors.joining(","));
	}

	private static String zookeeperId(String etcdKey)
	{
		return Iterables.getLast(Splitter.on('/').split(etcdKey));
	}
}
