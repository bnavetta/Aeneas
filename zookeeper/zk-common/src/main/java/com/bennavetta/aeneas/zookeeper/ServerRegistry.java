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
}
