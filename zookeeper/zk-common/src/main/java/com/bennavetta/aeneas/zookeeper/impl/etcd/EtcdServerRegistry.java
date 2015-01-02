package com.bennavetta.aeneas.zookeeper.impl.etcd;

import com.bennavetta.aeneas.zookeeper.ServerRegistry;
import com.bennavetta.aeneas.zookeeper.ZkException;
import com.bennavetta.aeneas.zookeeper.ZkServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Server registry implementation based on etcd.
 */
public class EtcdServerRegistry implements ServerRegistry
{
	public static final String REGISTRY_DIR = "aeneas/zookeeper/servers";
	
	private static final Logger LOG = LoggerFactory.getLogger(EtcdServerRegistry.class);

	private EtcdClient etcd;
	private ObjectMapper objectMapper;

	public EtcdServerRegistry(EtcdClient etcd)
	{
		this(etcd, new ObjectMapper());
	}

	public EtcdServerRegistry(EtcdClient etcd, ObjectMapper objectMapper)
	{
		this.etcd = Preconditions.checkNotNull(etcd);
		this.objectMapper = Preconditions.checkNotNull(objectMapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(ZkServer server) throws ZkException
	{
		try
		{
			LOG.debug("Registering server {}", server);
			String key = String.valueOf(server.getId());
			String value = objectMapper.writeValueAsString(server);

			etcd.put(REGISTRY_DIR + "/" + key, value).prevExist(false).send().get();
		}
		catch (JsonProcessingException e)
		{
			throw new ZkException("Unable to serialize server", e);
		}
		catch (IOException | TimeoutException e)
		{
			throw new ZkException("Unable to communicate with etcd", e);
		}
		catch (EtcdException e)
		{
			if(e.errorCode == 105) // node exists
			{
				throw new IllegalStateException("Server already registered", e);
			}
			else
			{
				throw new ZkException("Etcd error registering server", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deregister(ZkServer server) throws ZkException
	{
		try
		{
			LOG.debug("Deregistering server {}", server);
			String key = String.valueOf(server.getId());
			etcd.delete(REGISTRY_DIR + "/" + key).send().get();
		}
		catch (IOException | TimeoutException e)
		{
			throw new ZkException("Unable to communicate with etcd", e);
		}
		catch (EtcdException e)
		{
			if(e.errorCode == 100) // not found
			{
				throw new IllegalStateException("Server not registered", e);
			}
			else
			{
				throw new ZkException("Etcd error deregistering server", e);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImmutableList<ZkServer> getServers() throws ZkException
	{
		try
		{
			LOG.debug("Retrieving server listing");
			EtcdKeysResponse response = etcd.getDir(REGISTRY_DIR).send().get();

			if(response.node.nodes == null)
			{
				return ImmutableList.of();
			}

			ImmutableList.Builder<ZkServer> servers = ImmutableList.builder();

			for(EtcdNode node : response.node.nodes)
			{
				ZkServer server = objectMapper.readValue(node.value, ZkServer.class);
				LOG.debug("Retrieved server {}", server);
				servers.add(server);
			}

			return servers.build();
		}
		catch (EtcdException e)
		{
			if(e.errorCode == 100) // not found - no servers registered
			{
				return ImmutableList.of();
			}
			else
			{
				throw new ZkException("Etcd error obtaining server listing");
			}
		}
		catch (JsonProcessingException e)
		{
			throw new ZkException("Unable to deserialize server", e);
		}
		catch (TimeoutException | IOException e)
		{
			throw new ZkException("Unable to communicate with etcd");
		}
	}
}
