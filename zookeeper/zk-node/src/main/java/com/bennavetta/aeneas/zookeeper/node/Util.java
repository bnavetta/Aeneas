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
package com.bennavetta.aeneas.zookeeper.node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bennavetta.aeneas.zookeeper.IdGenerator;
import com.bennavetta.aeneas.zookeeper.ZkException;
import com.google.common.base.Charsets;

/**
 * Static helper methods for running a server.
 */
public class Util
{
	private static final Logger LOG = LoggerFactory.getLogger(Util.class);

	/**
	 * Get the node ID from the ZooKeeper data directory or allocate a new one
	 */
	public static int obtainId(IdGenerator idGenerator, Configuration configuration) throws IOException, ZkException
	{
		if(configuration.getStatic("dataDir") != null)
		{
			Path dataDir = Paths.get(configuration.getStatic("dataDir"));
			if(Files.isDirectory(dataDir))
			{
				Path idFile = dataDir.resolve("myid");
				if(Files.isReadable(idFile))
				{
					int id = Integer.parseInt(new String(Files.readAllBytes(idFile), Charsets.US_ASCII));
					LOG.debug("Read existing id: {}", id);
					return id;
				}
				else
				{
					throw new IllegalStateException("Data directory exists but cannot read 'myid' file");
				}
			}
		}

		int id = idGenerator.generateId();
		LOG.debug("Allocated new id: {}", id);
		return id;
	}
}
