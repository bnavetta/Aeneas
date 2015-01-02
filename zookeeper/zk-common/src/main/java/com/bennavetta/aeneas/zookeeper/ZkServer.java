package com.bennavetta.aeneas.zookeeper;

import com.google.common.net.HostAndPort;
import com.google.common.net.HostSpecifier;

/**
 * Represents a ZooKeeper server node. The information used to represent a server is essentially the same as that in a
 * ZooKeeper server configuration line. However, the client port address is currently unsupported and is assumed to be
 * the same as the server address. Also, the first and second ports specified are referred to as the peer and election
 * ports.
 */
public final class ZkServer
{
	private final int id;
	private final HostSpecifier address;
	private final Role role;
	private final int peerPort;
	private final int electionPort;
	private final int clientPort;

	public ZkServer(int id, HostSpecifier address, Role role, int peerPort, int electionPort, int clientPort)
	{
		this.id = id;
		this.address = address;
		this.role = role;
		this.peerPort = peerPort;
		this.electionPort = electionPort;
		this.clientPort = clientPort;
	}

	/**
	 * Returns this server's information as a ZooKeeper server keyword specification. Server keywords are specified as
	 * {@code server.<positive id> = <address1>:<port1>:<port2>[:role];<client port> }.
	 *
	 * @return the server information as a server keyword specification
	 */
	public String toServerSpec()
	{
		return String.format("server.%d=%s", id, toConnectionSpec());
	}

	/**
	 * Returns the connection information for this server. In effect, this is the part of the server specification to
	 * the right of the equals sign.
	 * @return connection information
	 * @see #toServerSpec()
	 */
	public String toConnectionSpec()
	{
		return String.format("%s:%d:%d:%s;%d", address, peerPort, electionPort, role, clientPort);
	}

	/**
	 * Returns the host/port combination used by clients to connect to this particular server. The host is the server
	 * address, and the port is the client connection port.
	 * @return the client connection information
	 */
	public HostAndPort toClientConnection()
	{
		return HostAndPort.fromParts(address.toString(), clientPort);
	}

	public int getId()
	{
		return id;
	}

	public HostSpecifier getAddress()
	{
		return address;
	}

	public Role getRole()
	{
		return role;
	}

	public int getPeerPort()
	{
		return peerPort;
	}

	public int getElectionPort()
	{
		return electionPort;
	}

	public int getClientPort()
	{
		return clientPort;
	}

	public static enum Role
	{
		OBSERVER,
		PARTICIPANT
		;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}

		public static Role defaultRole()
		{
			return OBSERVER;
		}
	}
}
