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
package com.bennavetta.aeneas.mesos.slave.docker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

/**
 * Support for Mesos' Docker containerizer
 */
public final class Docker
{
	private static final Logger LOG = LoggerFactory.getLogger(Docker.class);

	private Docker() {}

	private static boolean socketExists()
	{
		return Files.exists(Paths.get("/var/run/docker.sock"));
	}

	/**
	 * If necessary, attempt to launch a Docker daemon.
	 * @return {@code true} if a Docker daemon exists or was started, {@code false} if no Docker daemon could be obtained
	 */
	public static boolean startDocker()
	{
		if(socketExists())
		{
			LOG.debug("Found existing Docker socket");
		}
		else
		{
			try
			{
				LOG.debug("Launching Docker daemon");
				Daemon.launchDaemon(ImmutableList.of());

				LOG.debug("Waiting for daemon to start");
				Thread.sleep(15000);
			}
			catch (IOException e)
			{
				LOG.error("Unable to start Docker daemon", e);
				return false;
			}
			catch (InterruptedException e)
			{
				throw Throwables.propagate(e);
			}
		}
		return true;
	}
}
