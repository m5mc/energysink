package net.quantium.energysink.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.quantium.energysink.EnergyLeaderboards;

import java.util.Comparator;

public class LeaderboardsCommand extends CommandBase {

    public String getName()
    {
        return "lb";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getUsage(ICommandSender sender)
    {
        return "qenergysink.command.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args[0].equalsIgnoreCase("set")) {
            String name = args[1];
            long energy = parseLong(args[2]);

            sender.sendMessage(new TextComponentString(String.format("Energy value for %s has been set to %d EU", name, EnergyLeaderboards.get().set(name, energy))));
        } else if (args[0].equalsIgnoreCase("get")) {
            String name = args[1];
            EnergyLeaderboards.TeamInfo team = EnergyLeaderboards.get().get(name);
            if(team != null) sender.sendMessage(new TextComponentString(String.format("Entry %s has %d EU", name, team.getEnergy())));
            else sender.sendMessage(new TextComponentString(String.format("Entry %s does not exist", name)));
        }  else if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(new TextComponentString(String.format("-- Total Energy: %d EU --", EnergyLeaderboards.get().getTotal())));

            EnergyLeaderboards.get().sorted().forEach(team -> {
                sender.sendMessage(new TextComponentString(String.format("[%s]: %d", team.getName(), team.getEnergy())));
            });
        } else if (args[0].equalsIgnoreCase("add")) {
            String name = args[1];
            long energy = parseLong(args[2]);

            sender.sendMessage(new TextComponentString(String.format("Energy value for %s has been set to %d EU", name, EnergyLeaderboards.get().add(name, energy))));
        } else if (args[0].equalsIgnoreCase("move")) {
            String name1 = args[1];
            String name2 = args[2];

            EnergyLeaderboards.TeamInfo team = EnergyLeaderboards.get().get(name1);
            long amount = team == null ? 0 : team.getEnergy();
            if (args.length > 3) {
                amount = parseLong(args[3]);
            }

            EnergyLeaderboards.get().add(name1, -amount);
            EnergyLeaderboards.get().add(name2, +amount);

            sender.sendMessage(new TextComponentString(String.format("Energy value has been moved from %s to %s (%d EU)", name1, name2, amount)));
        } else if (args[0].equalsIgnoreCase("clear")) {
            String name = args[1];
            EnergyLeaderboards.get().set(name, 0);

            sender.sendMessage(new TextComponentString(String.format("Energy value for %s has been cleared", name)));
        }
    }
}
