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
package com.bennavetta.aeneas.zookeeper;

/**
 * Monitors the server registry for changes.
 */
public interface ServerRegistryWatcher
{
	/**
	 * Start watching for changes in the background.
	 */
	public void start();

	/**
	 * Stop watching.
	 */
	public void stop();

	/**
	 * Returns {@code true} if the watcher is running.
	 */
	public boolean isWatching();

	public static interface Listener
	{
		public void serverAdded(ZkServer server);

		public void serverRemoved(int serverId);
	}
}
