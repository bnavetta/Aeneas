package com.bennavetta.aeneas.zookeeper;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Static utility methods related to ZooKeeper servers.
 */
public final class Servers
{
	private Servers()
	{

	}

	/**
	 * Generates suitable ZooKeeper configuration from a collection of server instances. The produced map is the parsed
	 * equivalent of the {@code server.N} properties in ZooKeeper's dynamic configuration file.
	 * @param servers the servers to include
	 * @return a configuration chunk as a map
	 */
	public static Map<String, String> toServerSpecification(ZkServer... servers)
	{
		Preconditions.checkNotNull(servers);
		return Arrays.stream(servers).collect(Collectors.toMap(s -> "server." + s.getId(), ZkServer::toConnectionSpec));
	}

	/**
	 * Generates suitable ZooKeeper configuration from a collection of server instances. The produced map is the parsed
	 * equivalent of the {@code server.N} properties in ZooKeeper's dynamic configuration file.
	 * @param servers the servers to include
	 * @return a configuration chunk as a map
	 */
	public static Map<String, String> toServerSpecification(Collection<ZkServer> servers)
	{
		Preconditions.checkNotNull(servers);
		return servers.stream().collect(Collectors.toMap(s -> "server." + s.getId(), ZkServer::toConnectionSpec));
	}

	/**
	 * Builds a client connection string from several servers. The basic format is comma-separated host:port pairs.
	 * The {@code zk://} protocol specifier can also be added, since some non-ZooKeeper applications require it (the
	 * ZooKeeper client library does not). In addition, a chroot suffix can be added so that all paths on the client
	 * are relative to it.
	 * @param addProtocol {@code true} to add the {@code zk://} scheme
	 * @param servers the servers the client can connect to
	 * @param chroot the client's base path or {@code null}
	 * @return a connection string
	 */
	public static String toConnectionString(boolean addProtocol, Collection<ZkServer> servers, String chroot)
	{
		Preconditions.checkNotNull(servers);
		StringBuilder connection = new StringBuilder();

		if(addProtocol)
		{
			connection.append("zk://");
		}

		connection.append(servers.stream()
		                         .map(ZkServer::toClientConnection)
		                         .map(HostAndPort::toString)
		                         .collect(Collectors.joining(",")));

		if(chroot != null)
		{
			connection.append('/').append(chroot);
		}

		return  connection.toString();
	}

	/**
	 * Builds a client connection string from several servers. This is equivalent to
	 * {@code toConnectionString(false, servers, null)}.
	 * @param servers the servers the client can connect to
	 * @return a connection string
	 */
	public static String toConnectionString(Collection<ZkServer> servers)
	{
		return toConnectionString(false, servers, null);
	}
}
