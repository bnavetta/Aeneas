package com.bennavetta.aeneas.zookeeper.node;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Runs and watches a ZooKeeper instance
 */
public class ZooKeeper
{
	private final Path installDir;

	private Properties configuration;
	private int myId;

	public ZooKeeper(Path installDir, int myId)
	{
		Preconditions.checkArgument(isValidLocation(), "Must specify a valid ZooKeeper installation");
		this.installDir = installDir;
		this.myId = myId;
	}

	public boolean isValidLocation()
	{
		Path serverBin = installDir.resolve("bin/zkServer.sh");
		return Files.isExecutable(serverBin);
	}

	public void applyConfiguration() throws IOException
	{
		Path configFile = installDir.resolve("conf/zoo.cfg");
		try(Writer out = Files.newBufferedWriter(configFile, Charsets.UTF_8))
		{
			configuration.store(out, "");
		}
	}
}
