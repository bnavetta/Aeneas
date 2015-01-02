package com.bennavetta.aeneas.zookeeper.impl.etcd;

import com.bennavetta.aeneas.zookeeper.IdGenerator;
import com.bennavetta.aeneas.zookeeper.ZkException;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Etcd-based implementation of the {@link IdGenerator} interface. It uses etcd's
 * <a href="https://coreos.com/docs/distributed-configuration/etcd-api/#atomically-creating-in-order-keys">atomic in-order key</a>
 * support and a scratch directory.
 */
public class EtcdIdGenerator implements IdGenerator
{
	public static final String IDGEN_DIR = "/aeneas/zookeeper/idgen";

	private final EtcdClient etcd;

	public EtcdIdGenerator(EtcdClient etcd)
	{
		this.etcd = etcd;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int generateId() throws ZkException
	{
		try
		{
			EtcdKeysResponse response = etcd.post(IDGEN_DIR, "").send().get();

			String key = response.node.key;
			return Integer.parseInt(key.substring(key.lastIndexOf('/') + 1));
		}
		catch (IOException | TimeoutException e)
		{
			throw new ZkException("Unable to communicate with etcd", e);
		}
		catch (EtcdException e)
		{
			if(e.errorCode == 100) // 100 = key not found
			{
				createDirectory();
				return generateId();
			}
		}
	}

	private void createDirectory() throws ZkException
	{
		try
		{
			etcd.putDir(IDGEN_DIR).send().get();
		}
		catch (IOException | EtcdException | TimeoutException e)
		{
			throw new ZkException("Unable to create id generation directory", e);
		}
	}
}
