/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.commands.admin.arena;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static me.despical.kotl.handlers.setup.SetupInventory.TUTORIAL_VIDEO;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class CreateCommand extends SubCommand {

	private final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

	public CreateCommand() {
		super("create");

		setPermission("kotl.admin.create");
	}

	@Override
	public String getPossibleArguments() {
		return "<ID>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		
		if(ArenaRegistry.getArena(args[0]) != null || config.contains("instances." + args[0])) {
			player.sendMessage(plugin.getChatManager().getPrefix() + ChatColor.RED + "Arena with that ID already contains!");
			player.sendMessage(plugin.getChatManager().getPrefix() + ChatColor.RED + "To check existing arenas use: /kotl list");
			return;
		}
		
		setupDefaultConfiguration(args[0]);

		player.sendMessage(ChatColor.BOLD + "--------------------------------------------");
        MiscUtils.sendCenteredMessage(player, ChatColor.YELLOW + "Instance " + args[0] + " created!");
        player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, ChatColor.GREEN + "Edit this arena via " + ChatColor.GOLD + "/kotl " + "edit " + args[0] + ChatColor.GREEN + "!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, ChatColor.GOLD + "Don't know where to start? Check out tutorial video:");
		MiscUtils.sendCenteredMessage(player, ChatColor.GRAY + TUTORIAL_VIDEO);
        player.sendMessage(ChatColor.BOLD + "--------------------------------------------");
	}
	
	private void setupDefaultConfiguration(String id) {
		String path = "instances." + id + ".", loc = LocationSerializer.SERIALIZED_LOCATION;

		config.set(path + "endlocation", loc);
		config.set(path + "areaMin", loc);
		config.set(path + "areaMax", loc);
		config.set(path + "isdone", false);
		config.set(path + "hologramLocation", loc);
		config.set(path + "plateLocation", loc);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		Arena arena = new Arena(id);
		
		arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
		arena.setPlateLocation(LocationSerializer.fromString(config.getString(path + "plateLocation")));
		arena.setHologramLocation(LocationSerializer.fromString(config.getString(path + "hologramLocation")));
		arena.setReady(false);
		
		ArenaRegistry.registerArena(arena);
	}

	@Override
	public List<String> getTutorial() {
		return Collections.singletonList("Creates a new arena with default configuration");
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}