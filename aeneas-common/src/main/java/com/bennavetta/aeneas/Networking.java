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
package com.bennavetta.aeneas;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Networking utilities.
 * @author ben
 *
 */
public final class Networking
{
	private static final Logger LOG = LoggerFactory.getLogger(Networking.class);
	
	private Networking() {}
	
	/**
	 * Determine the local address of this host. Other hosts on the network can use the returned address
	 * to access this host. This should provide a useful address in Docker containers run with {@code --net=host}.
	 * @return an IP address
	 * @throws IOException if a network error occurs while resolving the address
	 */
	public static InetAddress getLocalAddress()  throws IOException
	{
		try(Socket socket = new Socket("8.8.8.8", 53))
		{
			InetAddress localAddress = socket.getLocalAddress();
			LOG.debug("Found local address {}", localAddress);
			return localAddress;
		}
	}
}
