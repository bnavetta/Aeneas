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

import com.google.common.base.Charsets;
import mousio.etcd4j.responses.EtcdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * Configures a ZooKeeper instance
 */
public class Configuration
{
	private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

	private Properties staticConfiguration = new Properties();
	private Properties dynamicConfiguration = new Properties();
	private int myId;

	public void setStatic(String key, String value)
	{
		LOG.trace("Setting '{}' = '{}' for static configuration", key, value);
		staticConfiguration.setProperty(key, value);
	}

	public String getStatic(String key)
	{
		return staticConfiguration.getProperty(key);
	}

	public void setDynamic(String key, String value)
	{
		LOG.trace("Setting '{}' = '{}' for dynamic configuration", key, value);
		dynamicConfiguration.setProperty(key, value);
	}

	public String getDynamic(String key)
	{
		return dynamicConfiguration.getProperty(key);
	}

	public int getMyId()
	{
		return myId;
	}

	public void addServers(Registration registration) throws EtcdException, TimeoutException, IOException
	{
		registration.getNodes().forEach((id, spec) -> {
			setDynamic("server." + id, spec);
		});
	}

	public void addFromEnvironment()
	{
		System.getenv().forEach((key, value) -> {
			if(key.startsWith("ZK_"))
			{
				String actualKey = key.substring(4);
				LOG.debug("Setting '{}' = '{}' from environment", actualKey, value);
				// ZooKeeper will move over dynamic properties as needed
				setStatic(actualKey, value);
			}
		});

	}

	/**
	 * Add settings listed in the example configuration file or recommended in the ZooKeeper documentation.
	 * This currently includes:
	 *
	 * <ul>
	 *     <li>{@code tickTime=2000}</li>
	 *     <li>{@code initLimit=10}</li>
	 *     <li>{@code syncLimit=5}</li>
	 *     <li>{@code dataDir=/var/lib/zookeeper/data}</li>
	 *     <li>{@code dataLogDir=/var/lib/zookeeper/log}</li>
	 *     <li>{@code standaloneEnabled=false}</li>
	 * </ul>
	 *
	 * Note that {@code standaloneEnabled} must be set to {@code false} for a single server to start in replicated mode,
	 * which is what the first server in the cluster does.
	 */
	public void addDefaults()
	{
		setStatic("tickTime", "2000");
		setStatic("initLimit", "10");
		setStatic("syncLimit", "5");
		setStatic("dataDir", "/var/lib/zookeeper/data");
		setStatic("dataLogDir", "/var/lib/zookeeper/log");
		setStatic("standaloneEnabled", "false");
		LOG.debug("Set defaults: {}", staticConfiguration);
	}

	/**
	 * Get the node ID from ZooKeeper, allocating a new one if necessary
	 * @param registration
	 */
	public void obtainId(Registration registration) throws EtcdException, TimeoutException, IOException
	{
		OptionalInt existingId = registration.getId();
		if(existingId.isPresent())
		{
			myId = existingId.getAsInt();
		}
		else
		{
			myId = registration.allocateId();
		}
	}

	public void writeId(Path idPath) throws IOException
	{
		LOG.debug("Writing ID {} to {}", myId, idPath);
		Files.write(idPath, String.valueOf(myId).getBytes(Charsets.US_ASCII));
	}

	/**
	 * Write the node ID to the standard {@code myid} location. This requires the {@code dataDir} property to be set.
	 * @throws IOException
	 */
	public void writeId() throws IOException
	{
		Path dataDir = Paths.get(getStatic("dataDir"));
		Files.createDirectories(dataDir);
		writeId(dataDir.resolve("myid"));
	}

	public void writeConfiguration(Path staticPath, Path dynamicPath) throws IOException
	{
		Properties staticToWrite = new Properties();
		staticToWrite.putAll(staticConfiguration);
		staticToWrite.setProperty("dynamicConfigFile", dynamicPath.toAbsolutePath().toString());

		LOG.debug("Writing static configuration to {}: {}", staticPath, staticToWrite);

		try (OutputStream out = Files.newOutputStream(staticPath))
		{
			staticToWrite.store(out, "Static ZooKeeper configuration");
		}

		LOG.debug("Writing dynamic configuration to {}: {}", dynamicPath, dynamicConfiguration);

		try (OutputStream out = Files.newOutputStream(dynamicPath))
		{
			dynamicConfiguration.store(out, "Dynamic ZooKeeper configuration");
		}
	}
}
