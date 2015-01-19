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
package com.bennavetta.aeneas.cli.zookeeper;

public final class ZooKeeperNodes
{
	private ZooKeeperNodes() {}
	
	public static String containerName(int id)
	{
		return "aeneas-zookeeper-" + id;
	}
	
	public static int peerPort(int id)
	{
		return 2870 + id;
	}
	
	public static int electionPort(int id)
	{
		return 3870 + id;
	}
	
	public static int clientPort(int id)
	{
		return 2170 + id;
	}
}
