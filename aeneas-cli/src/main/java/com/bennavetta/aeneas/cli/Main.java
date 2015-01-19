package com.bennavetta.aeneas.cli;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Sets;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig.DockerClientConfigBuilder;
import mousio.etcd4j.EtcdClient;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

/**
 * Entry point for Aeneas' command line utility.
 */
public class Main
{
	@Parameter(names = {"-H", "--docker-host"}, description = "Docker daemon host")
	private URI dockerHost;

	@Parameter(names = {"-E", "--etcd-host"}, description = "Etcd host")
	private URI etcdHost;

	private EtcdClient etcdClient;
	private DockerClient dockerClient;

	private Set<Command> commands = Sets.newHashSet();

	public static void main(String[] args)
	{
		Main main = new Main();
		JCommander jc = new JCommander(main);
		jc.addConverterFactory(new ConverterFactory());

		for(Command command : main.commands)
		{
			jc.addCommand(command.getName(), command);
		}

		jc.parse(args);
		main.initializeClients();
		main.run(jc);
	}

	private void run(JCommander jc)
	{
		Optional<Command> command = Optional.ofNullable(jc.getParsedCommand())
		                                    .flatMap(name -> commands.stream().filter(c -> c.getName().equals(name)).findFirst());
		if(command.isPresent())
		{
			command.get().run(this);
		}
		else
		{
			System.err.println("Invalid command: " + jc.getParsedCommand());
			System.exit(1);
		}
	}

	private void initializeClients()
	{
		if(etcdHost != null)
		{
			etcdClient = new EtcdClient(etcdHost);
		}
		else if(System.getenv("ETCD_HOST") != null)
		{
			etcdClient = new EtcdClient(URI.create(System.getenv("ETCD_HOST")));
		}
		else
		{
			etcdClient = new EtcdClient();
		}

		DockerClientConfigBuilder dockerConfigBuilder = DockerClientConfig.createDefaultConfigBuilder();
		if(dockerHost != null)
		{
			dockerConfigBuilder = dockerConfigBuilder.withUri(dockerHost.toString());
		}
		dockerClient = DockerClientBuilder.getInstance(dockerConfigBuilder.build()).build();
	}

	public EtcdClient getEtcdClient()
	{
		return etcdClient;
	}

	public DockerClient getDockerClient()
	{
		return dockerClient;
	}

	private static class URIConverter implements IStringConverter<URI>
	{
		@Override
		public URI convert(String value)
		{
			return URI.create(value);
		}
	}

	private static class ConverterFactory implements IStringConverterFactory
	{
		@Override
		public <T> Class<? extends IStringConverter<T>> getConverter(Class<T> forType)
		{
			if(forType.equals(URI.class))
				return (Class<? extends IStringConverter<T>>) URIConverter.class;

			return null;
		}
	}
}
