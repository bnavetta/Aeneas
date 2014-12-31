package com.bennavetta.aeneas.zookeeper.node;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Interacts with etcd to obtain and modify registration information
 */
public class Registration
{
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
		etcd.put(NODES_KEY + "/" + id, serverSpec).prevExist(false).send().get();
	}

	/**
	 * Unregister a node from the {@code nodes} directory.
	 * @param id the ZooKeeper node ID
	 */
	public void unregister(int id) throws IOException, TimeoutException, EtcdException
	{
		etcd.delete(NODES_KEY + "/" + id).send().get();
	}

	/**
	 * Look up this node's ZooKeeper ID from the {@code ids} directory in etcd.
	 * @return the node ID, if found, or an empty optional
	 */
	public Optional<Integer> getId() throws IOException, TimeoutException, EtcdException
	{
		EtcdKeysResponse response = etcd.get(IDS_KEY + "/" + getLocalAddress()).send().get();

		if(response.node != null)
		{
			return Optional.of(Integer.parseInt(response.node.value));
		}
		else
		{
			return Optional.empty();
		}
	}

	/**
	 * Obtain a new ID and register it in the {@code ids} directory.
	 * @return the new ID
	 */
	public int allocateId() throws IOException, TimeoutException, EtcdException
	{
		EtcdKeysResponse existingData = etcd.getDir(IDS_KEY).send().get();

		int lastId = existingData.node.nodes.stream().map(n -> n.value).mapToInt(Integer::parseInt).max().getAsInt();

		int id = lastId + 1;
		InetAddress localAddress = getLocalAddress();

		etcd.put(IDS_KEY + "/" + localAddress, String.valueOf(id))
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
