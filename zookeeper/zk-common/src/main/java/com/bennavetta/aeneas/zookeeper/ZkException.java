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
 * Exception type for errors produced by Aeneas' ZooKeeper clustering system.
 */
public class ZkException extends Exception
{
	// TODO: add some indication of whether or not the operation can be retried

	/**
	 * {@inheritDoc}
	 */
	public ZkException(String message)
	{
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public ZkException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * {@inheritDoc}
	 */
	public ZkException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * {@inheritDoc}
	 */
	public ZkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
