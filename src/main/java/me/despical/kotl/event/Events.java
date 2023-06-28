/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
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

package me.despical.kotl.event;

import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.UpdateChecker;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.handler.ChatManager;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class Events extends ListenerAdapter {

	public Events(Main plugin) {
		super(plugin);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		var player = event.getPlayer();

		plugin.getUserManager().loadStatistics(player);

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}

		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) || !player.hasPermission("kotl.updatenotify")) {
			return;
		}

		UpdateChecker.init(plugin, 80686).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				player.sendMessage(chatManager.coloredRawMessage("&3[KOTL] &bFound an update: v" + result.getNewestVersion()));
				player.sendMessage(chatManager.coloredRawMessage("&3>> &bhttps://spigotmc.org/resources/80686"));
			}
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		var player = event.getPlayer();
		var arena = plugin.getArenaRegistry().getArena(player);

		if (arena != null) {
			chatManager.broadcastAction(arena, player, ChatManager.ActionType.LEAVE);

			arena.removePlayer(player);
		}

		plugin.getUserManager().removeUser(player);
	}

	@EventHandler
	public void onCommandExecute(PlayerCommandPreprocessEvent event) {
		var player = event.getPlayer();

		if (!plugin.getArenaRegistry().isInArena(player)) {
			return;
		}

		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BLOCK_COMMANDS)) {
			return;
		}

		String message = event.getMessage();

		if (plugin.getConfig().getStringList("Whitelisted-Commands").contains(message)) {
			return;
		}

		if (player.isOp() || player.hasPermission("kotl.command.override")) {
			return;
		}

		if (message.startsWith("/kotl") || message.startsWith("/kingoftheladder") || message.contains("top") || message.contains("stats")) {
			return;
		}

		event.setCancelled(true);
		player.sendMessage(chatManager.prefixedMessage("in_game.only_command_is_leave"));
	}
	
	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player victim)) {
			return;
		}

		if (!plugin.getArenaRegistry().isInArena(victim)) {
			return;
		}

		if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
			if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_FALL_DAMAGE)) {
				return;
			}

			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFireworkDamage(EntityDamageByEntityEvent event) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.FIREWORKS_ON_NEW_KING)) return;
		if (!(event.getEntity() instanceof Player player)) return;

		if (!plugin.getArenaRegistry().isInArena(player)) return;

		if (event.getDamager() instanceof Firework) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && plugin.getArenaRegistry().isInArena((Player) event.getEntity())) {
			event.setFoodLevel(20);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		final var player = event.getPlayer();

		if (plugin.getArenaRegistry().isInArena(player) && !player.isOp()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		final var player = event.getPlayer();

		if (plugin.getArenaRegistry().isInArena(player) && !player.isOp()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (plugin.getArenaRegistry().isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPickUpItem(PlayerPickupItemEvent event) {
		if (plugin.getArenaRegistry().isInArena(event.getPlayer())) {
			event.setCancelled(true);
			event.getItem().remove();
		}
	}
}