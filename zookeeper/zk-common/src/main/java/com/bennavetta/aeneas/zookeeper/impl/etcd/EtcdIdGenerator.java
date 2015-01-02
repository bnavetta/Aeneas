package com.bennavetta.aeneas.zookeeper.impl.etcd;

import com.bennavetta.aeneas.zookeeper.IdGenerator;
import com.bennavetta.aeneas.zookeeper.ZkException;
import com.google.common.base.Preconditions;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOG = LoggerFactory.getLogger(EtcdIdGenerator.class);

	private final EtcdClient etcd;

	public EtcdIdGenerator(EtcdClient etcd)
	{
		this.etcd = Preconditions.checkNotNull(etcd);
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
			String id = key.substring(key.lastIndexOf('/') + 1);
			LOG.debug("Allocated id {} from etcd", id);
			return Integer.parseInt(id);
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
			throw new ZkException("Etcd error generating id", e);
		}
	}

	private void createDirectory() throws ZkException
	{
		try
		{
			LOG.debug("Creating id generation directory");
			etcd.putDir(IDGEN_DIR).send().get();
		}
		catch (IOException | EtcdException | TimeoutException e)
		{
			throw new ZkException("Unable to create id generation directory", e);
		}
	}
}
