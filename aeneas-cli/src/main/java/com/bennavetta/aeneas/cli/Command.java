package com.bennavetta.aeneas.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Sets;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Base command interface since JCommander doesn't have one
 */
public interface Command
{
	public String getName();

	public void run(Main main);

	public static class Group implements Command
	{
		@Parameter
		private List<String> args = Lists.newArrayList();

		private final String name;
		private Command defaultCommand;
		private Set<Command> commands = Sets.newHashSet();

		public Group(String name)
		{
			this.name = name;
		}

		public Group setDefaultCommand(Command defaultCommand)
		{
			this.defaultCommand = defaultCommand;
			commands.add(defaultCommand);
			return this;
		}

		public Group addCommand(Command command)
		{
			commands.add(command);
			return this;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public void run(Main main)
		{
			JCommander jc = new JCommander();
			for(Command command : commands)
			{
				jc.addCommand(command.getName(), command);
			}

			jc.parse(args.toArray(new String[0]));
			if(jc.getParsedCommand() == null)
			{
				defaultCommand.run(main);
			}
			else
			{
				Optional<Command> command = commands.stream().filter(c -> c.getName().equals(jc.getParsedCommand())).findFirst();
				if(command.isPresent())
				{
					command.get().run(main);
				}
				else
				{
					throw new IllegalArgumentException("Unknown command: " + jc.getParsedCommand());
				}
			}
		}
	}
}
