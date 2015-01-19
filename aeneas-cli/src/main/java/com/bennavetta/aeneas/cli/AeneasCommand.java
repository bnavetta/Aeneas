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
package com.bennavetta.aeneas.cli;

import io.airlift.airline.Option;
import io.airlift.airline.OptionType;

import java.io.IOException;
import java.net.URI;

import mousio.etcd4j.EtcdClient;

import com.bennavetta.aeneas.zookeeper.ServerRegistry;
import com.bennavetta.aeneas.zookeeper.impl.etcd.EtcdServerRegistry;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig.DockerClientConfigBuilder;

/**
 * Base class for Aeneas commands
 */
public abstract class AeneasCommand implements Runnable
{
	@Option(type = OptionType.GLOBAL, name = "-d", description = "Docker daemon host")
	public URI dockerHost;

	@Option(type = OptionType.GLOBAL, name = "-e", description = "Etcd host")
	public URI etcdHost;

	protected EtcdClient etcdClient;
	protected DockerClient dockerClient;
	protected ServerRegistry zookeeperRegistry;

	public final void run()
	{
		int exitStatus = 0;
		try
		{
			initializeClients();
			exitStatus = execute();
		}
		finally
		{
			destroyClients();
		}
		System.exit(exitStatus);
	}

	private void initializeClients()
	{
		if(etcdHost != null)
		{
			etcdClient = new EtcdClient(etcdHost);
		}
		else if(System.getenv("ETCD_HOST") != null)
		{
			etcdClient = new EtcdClient(URI.create(System.getenv("ETCD_HOST")));
		}
		else
		{
			etcdClient = new EtcdClient();
		}

		DockerClientConfigBuilder dockerConfigBuilder = DockerClientConfig.createDefaultConfigBuilder();
		if(dockerHost != null)
		{
			dockerConfigBuilder = dockerConfigBuilder.withUri(dockerHost.toString());
		}
		dockerClient = DockerClientBuilder.getInstance(dockerConfigBuilder.build()).build();

		zookeeperRegistry = new EtcdServerRegistry(etcdClient);
	}
	
	private void destroyClients()
	{
		if(dockerClient != null)
		{
			try
			{
				dockerClient.close();
			}
			catch(IOException e) {}
		}
		
		if(etcdClient != null)
		{
			try
			{
				etcdClient.close();
			}
			catch(IOException e) {}
		}
	}

	protected abstract int execute();
}
