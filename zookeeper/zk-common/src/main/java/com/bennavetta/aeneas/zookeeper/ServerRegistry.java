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
package com.bennavetta.aeneas.zookeeper;

import com.google.common.collect.ImmutableList;

/**
 * Provides access to a shared registry of ZooKeeper servers in a cluster.
 */
public interface ServerRegistry
{
	/**
	 * Add a server to the list of available nodes. The server must be ready to accept traffic, as it will be added to
	 * the quorum some time after being registered.
	 * @param server the server to register
	 * @throws ZkException if unable to register the server
	 */
	public void register(ZkServer server) throws ZkException;

	/**
	 * Remove a server from the list of available nodes. The server need not be immediately terminated, but it will be
	 * removed from the quorum shortly after deregistration.
	 * @param server the server to deregister
	 * @throws ZkException if unable to deregister the server
	 */
	public void deregister(ZkServer server) throws ZkException;

	/**
	 * Returns the current server list. This is not necessarily obtained from the ZooKeeper quorum, so it may be briefly
	 * inconsistent with the actual cluster membership. However, in a properly functioning system, the quorum will be
	 * reconfigured to match the global registry whenever the registry changes.
	 * @return a list of servers
	 * @throws ZkException if unable to obtain a server listing
	 */
	public ImmutableList<ZkServer> getServers() throws ZkException;

	/**
	 * Create a new watcher that will monitor the ZooKeeper server registry for changes.
	 * @param listener the listener that will be called when the registry is modified
	 * @return an unstarted watcher
	 */
	public ServerRegistryWatcher watch(ServerRegistryWatcher.Listener listener);
}
