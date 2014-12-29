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
package com.bennavetta.aeneas.zookeeper.configurator;

import com.google.common.base.Throwables;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.requests.EtcdKeyGetRequest;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * A synchronization latch that waits for the ZooKeeper cluster to start up.
 */
public class StartupLatch
{
	private static final Logger LOG = LoggerFactory.getLogger(StartupLatch.class);

	public static final String STATE_KEY = "aeneas/zookeeper/state";
	public static final String STARTED = "started";

	private final EtcdClient etcd;
	private final CountDownLatch signal;

	public StartupLatch(EtcdClient etcd)
	{
		this.etcd = etcd;
		this.signal = new CountDownLatch(1);
	}

	public CountDownLatch getSignal()
	{
		return signal;
	}

	/**
	 * Wait asynchronously for the ZooKeeper cluster to register as started.
	 * The signal latch will be triggered upon completion.
	 * @throws IOException
	 */
	public void waitForStartup() throws IOException
	{
		waitForStartup(-1);
	}

	/**
	 * Helper to repeatedly wait for the {@code started} state.
	 * @param index the last etcd index or {@code -1}
	 * @throws IOException if etcd does
	 */
	private void waitForStartup(long index) throws IOException
	{
		EtcdKeyGetRequest request = etcd.get(STATE_KEY);

		if(index != -1)
		{
			request = request.waitForChange(index);
		}

		EtcdResponsePromise<EtcdKeysResponse> promise = request.send();
		promise.addListener(p -> {
			EtcdKeysResponse response = p.getNow();

			if(response == null || response.node == null)
			{
				LOG.debug("State key does not yet exist");
				try
				{
					Thread.sleep(5000);
					waitForStartup(-1);
				}
				catch (InterruptedException | IOException e)
				{
					throw Throwables.propagate(e);
				}
			}
			else if(STARTED.equals(response.node.value))
			{
				signal.countDown();
			}
			else
			{
				try
				{
					waitForStartup(response.etcdIndex);
				}
				catch (IOException e)
				{
					throw Throwables.propagate(e);
				}
			}
		});
	}
}
