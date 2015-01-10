package com.bennavetta.aeneas.mesos.master;

import com.bennavetta.aeneas.zookeeper.ServerRegistry;
import com.bennavetta.aeneas.zookeeper.impl.etcd.EtcdServerRegistry;
import mousio.etcd4j.EtcdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public final class Main
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws IOException
	{
		EtcdClient etcd = Optional.ofNullable(System.getenv("ETCD_SERVER"))
		                          .map(URI::create).map(EtcdClient::new)
		                          .orElseGet(EtcdClient::new);
		LOG.info("Connected to etcd - {}", etcd.getVersion());

		try
		{
			ServerRegistry registry = new EtcdServerRegistry(etcd);


		}
		finally
		{
			if(etcd != null)
			{
				etcd.close();
			}
		}
	}
}
