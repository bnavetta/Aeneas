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

import com.bennavetta.aeneas.zookeeper.impl.HostSpecifierSerializer;
import com.bennavetta.aeneas.zookeeper.impl.HostSpecifierDeserializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
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

	@JsonSerialize(using = HostSpecifierSerializer.class)
	@JsonDeserialize(using = HostSpecifierDeserializer.class)
	private final HostSpecifier address;
	private final Role role;
	private final int peerPort;
	private final int electionPort;
	private final int clientPort;

	@JsonCreator
	public ZkServer(@JsonProperty("id") int id,
	                @JsonProperty("address") HostSpecifier address,
	                @JsonProperty("role") Role role,
	                @JsonProperty("peerPort") int peerPort,
	                @JsonProperty("electionPort") int electionPort,
	                @JsonProperty("clientPort") int clientPort)
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

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("address", address)
				.add("role", role)
				.add("peerPort", peerPort)
				.add("electionPort", electionPort)
				.add("clientPort", clientPort)
				.toString();
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
			return PARTICIPANT;
		}
	}
}
