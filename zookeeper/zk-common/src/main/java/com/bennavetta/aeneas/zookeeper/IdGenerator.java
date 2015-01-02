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
