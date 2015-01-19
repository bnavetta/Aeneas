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
