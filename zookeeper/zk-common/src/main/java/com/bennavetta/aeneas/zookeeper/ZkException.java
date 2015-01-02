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
