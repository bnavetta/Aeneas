package com.bennavetta.aeneas.zookeeper;

/**
 * A service for generating new ZooKeeper ids. A server should generate a new id whenever it has a new data directory
 * and reuse its existing id where possible.
 */
@FunctionalInterface
public interface IdGenerator
{
	/**
	 * Allocates a new ZooKeeper id. The id is guaranteed to be distinct from any other id values produced by this
	 * generator. Ids are not required to be sequential.
	 * @return a ZooKeeper id
	 * @throws ZkException if unable to generate the id
	 */
	public int generateId() throws ZkException;
}
