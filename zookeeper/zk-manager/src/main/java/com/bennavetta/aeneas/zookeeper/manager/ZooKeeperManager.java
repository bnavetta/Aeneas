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

import com.bennavetta.aeneas.zookeeper.*;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages ZooKeeper configuration.
 */
public class ZooKeeperManager implements Watcher, ServerRegistryWatcher.Listener
{
	private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperManager.class);
	private final int CONNECTION_TIMEOUT = 5000;

	private final ServerRegistry registry;
	private final ZooKeeper zk;

	private ServerRegistryWatcher watcher;

	public ZooKeeperManager(ServerRegistry registry) throws ZkException, IOException
	{
		this.registry = registry;
		this.zk = initialCluster();
	}

	private ZooKeeper initialCluster() throws ZkException, IOException
	{
		ImmutableList<ZkServer> servers = registry.getServers();
		if(servers.isEmpty())
		{
			// TODO: wait for cluster members - https://github.com/coreos/etcd/issues/1516 would help
			throw new IllegalStateException("No cluster members");
		}
		else
		{
			return new ZooKeeper(Servers.toConnectionString(servers), CONNECTION_TIMEOUT, this);
		}
	}

	public void startMonitoring()
	{
		watcher = registry.watch(this);
		watcher.start();
	}

	public void stopMonitoring()
	{
		watcher.stop();
	}

	public void synchronizeMembers() throws ZkException
	{
		LOG.debug("Synchronizing ZooKeeper membership with registry");
		List<String> members = registry.getServers().stream().map(ZkServer::toServerSpec).collect(Collectors.toList());

		try
		{
			zk.reconfig(null, null, members, -1, null);
		}
		catch (KeeperException | InterruptedException e)
		{
			throw new ZkException("Unable to reconfigure quorum", e);
		}
	}

	@Override
	public void process(WatchedEvent event)
	{
		LOG.debug("Received ZooKeeper event: {}", event);
	}

	@Override
	public void serverAdded(ZkServer server)
	{
		LOG.info("Adding server {}", server);

		try
		{
			byte[] newConfig = zk.reconfig(Lists.newArrayList(server.toServerSpec()), null, null, -1, null);
			LOG.debug("New ZooKeeper configuration: '{}'", new String(newConfig, Charsets.US_ASCII));
		}
		catch (KeeperException | InterruptedException e)
		{
			LOG.error("Error reconfiguring ZooKeeper", e);
		}
	}

	@Override
	public void serverRemoved(int serverId)
	{
		LOG.info("Removing server {}", serverId);

		try
		{
			byte[] newConfig = zk.reconfig(null, Lists.newArrayList(String.valueOf(serverId)), null, -1, null);
			LOG.debug("New ZooKeeper configuration: '{}'", new String(newConfig, Charsets.US_ASCII));
		}
		catch (InterruptedException | KeeperException e)
		{
			LOG.error("Error reconfiguring ZooKeeper", e);
		}
	}
}
