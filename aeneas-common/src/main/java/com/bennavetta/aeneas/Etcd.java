package com.bennavetta.aeneas;

import java.io.IOException;
import java.net.URI;

import mousio.etcd4j.EtcdClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for interacting with the etcd key-value store.
 * @author ben
 *
 */
public final class Etcd
{
	private static final Logger LOG = LoggerFactory.getLogger(Etcd.class);
	
	private Etcd() {}
	
	public static URI getEtcdHost()
	{
		if(System.getenv("ETCD_HOST") != null)
		{
			return URI.create(System.getenv("ETCD_HOST"));
		}
		else
		{
			String hostname = "127.0.0.1";
			try
			{
				hostname = Networking.getLocalAddress().getHostAddress();
			}
			catch(IOException e)
			{
				LOG.warn("Local address resolution failed. Falling back to localhost", e);
			}
			
			return URI.create("http://" + hostname + ":4001");
		}
	}
	
	public static EtcdClient createClient()
	{
		return new EtcdClient(getEtcdHost());
	}
}
