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
package com.bennavetta.aeneas.zookeeper.node;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Interacts with etcd to obtain and modify registration information
 */
public class Registration
{
	private static final Logger LOG = LoggerFactory.getLogger(Registration.class);

	public static final String STATE_KEY = "aeneas/zookeeper/state";
	public static final String IDS_KEY = "aeneas/zookeeper/ids";
	public static final String NODES_KEY = "aeneas/zookeeper/nodes";

	private final EtcdClient etcd;

	public Registration(EtcdClient etcd)
	{
		this.etcd = etcd;
	}

	/**
	 * Register a node in the {@code nodes} directory. There must not already be a node registered with the given ID.
	 * @param id the ZooKeeper node ID
	 * @param serverSpec the server specification ({@code <address1>:<port1>:<port2>[:role];[<client port address>:]<client port>})
	 */
	public void register(int id, String serverSpec) throws IOException, TimeoutException, EtcdException
	{
		LOG.info("Registering server {} as {}", id, serverSpec);
		etcd.put(NODES_KEY + "/" + id, serverSpec).prevExist(false).send().get();
	}

	/**
	 * Unregister a node from the {@code nodes} directory.
	 * @param id the ZooKeeper node ID
	 */
	public void unregister(int id) throws IOException, TimeoutException, EtcdException
	{
		LOG.info("Unregistering server {}", id);
		etcd.delete(NODES_KEY + "/" + id).send().get();
	}

	/**
	 * Look up the nodes registered in etcd.
	 * @return a map of node IDs to server specifications
	 */
	public Map<Integer, String> getNodes() throws IOException, TimeoutException, EtcdException
	{
		EtcdKeysResponse response = etcd.getDir(NODES_KEY).recursive().send().get();
		if(response.node.nodes != null)
		{
			return etcd.getDir(NODES_KEY)
			           .recursive()
			           .send()
			           .get()
					.node.nodes.stream()
			                   .collect(Collectors.toMap(
					                   n -> Integer.parseInt(n.key.substring(n.key.lastIndexOf('/') + 1)),
					                   n -> n.value));
		}
		else
		{
			return Maps.newHashMap();
		}
	}

	public void startCluster() throws IOException, TimeoutException, EtcdException
	{
		LOG.info("Starting ZooKeeper cluster");
		etcd.put(STATE_KEY, "started").send().get();
	}

	/**
	 * Look up this node's ZooKeeper ID from the {@code ids} directory in etcd.
	 * @return the node ID, if found, or an empty optional
	 */
	public OptionalInt getId() throws IOException, TimeoutException, EtcdException
	{
		InetAddress localAddress = getLocalAddress();
		EtcdKeysResponse idsResponse = etcd.get(IDS_KEY).send().get();

		// etcd returns no value (instead of, say, an empty array) for an empty directory
		if(idsResponse.node.nodes == null)
		{
			return OptionalInt.empty();
		}

		Optional<String> existingId = idsResponse.node.nodes.stream()
		                      .filter(n -> n.key.endsWith(localAddress.getHostAddress()))
		                      .map(n -> n.value)
		                      .findFirst();

		// Can't map an Optional to an OptionalInt :(
		if(existingId.isPresent())
		{
			LOG.info("Found existing ID {} for {}", existingId.get(), localAddress);
			return OptionalInt.of(Integer.parseInt(existingId.get()));
		}
		else
		{
			return OptionalInt.empty();
		}
	}

	/**
	 * Obtain a new ID and register it in the {@code ids} directory.
	 * @return the new ID
	 */
	public int allocateId() throws IOException, TimeoutException, EtcdException
	{
		EtcdKeysResponse existingData = etcd.getDir(IDS_KEY).recursive().send().get();

		int id;

		if(existingData.node.nodes ==  null) // empty directory
		{
			id = 1;
		}
		else
		{

			int lastId = existingData.node.nodes.stream().map(n -> n.value).mapToInt(Integer::parseInt).max().getAsInt();
			id = lastId + 1;
		}

		InetAddress localAddress = getLocalAddress();

		LOG.info("Registering ID {} for {}", id, localAddress);

		etcd.put(IDS_KEY + "/" + localAddress.getHostAddress(), String.valueOf(id))
		    .prevExist(false).prevIndex(existingData.etcdIndex)
		    .send().get();

		return id;
	}

	public static InetAddress getLocalAddress()  throws IOException
	{
		try(Socket socket = new Socket("8.8.8.8", 53))
		{
			return socket.getLocalAddress();
		}
	}
}
