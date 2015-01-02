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
