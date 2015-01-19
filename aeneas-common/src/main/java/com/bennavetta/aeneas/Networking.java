package com.bennavetta.aeneas;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Networking utilities.
 * @author ben
 *
 */
public final class Networking
{
	private Networking() {}
	
	/**
	 * Determine the local address of this host. Other hosts on the network can use the returned address
	 * to access this host. This should provide a useful address even in Docker containers.
	 * @return an IP address
	 * @throws IOException if a network error occurs while resolving the address
	 */
	public static InetAddress getLocalAddress()  throws IOException
	{
		try(Socket socket = new Socket("8.8.8.8", 53))
		{
			return socket.getLocalAddress();
		}
	}
}
