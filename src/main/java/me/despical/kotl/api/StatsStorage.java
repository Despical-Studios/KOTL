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

package me.despical.kotl.api;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.sorter.SortUtils;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.user.data.MysqlManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Despical
 * @since 1.0.0
 * <p>
 * Created at 20.06.2020
 */
public class StatsStorage {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	@NotNull
	public static Map<UUID, Integer> getStats(StatisticType stat) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
				final Statement statement = connection.createStatement();
				final ResultSet set = statement.executeQuery("SELECT UUID, " + stat.name + " FROM " + ((MysqlManager) plugin.getUserManager().getDatabase()).getTableName() + " ORDER BY " + stat.name);

				final Map<UUID, Integer> column = new HashMap<>();

				while(set.next()) {
					column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
				}

				return column;
			} catch(SQLException e) {
				plugin.getLogger().log(Level.WARNING, "SQLException occurred during getting statistics from database!");
				return new HashMap<>();
			}
		}

		final FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
		final Map<UUID, Integer> stats = new LinkedHashMap<>();

		for (String string : config.getKeys(false)) {
			stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
		}

		return SortUtils.sortByValue(stats);
	}

	public static int getUserStats(Player player, StatisticType statisticType) {
		return plugin.getUserManager().getUser(player).getStat(statisticType);
	}

	public enum StatisticType {
		TOURS_PLAYED("toursplayed"), SCORE("score");

		final String name;
		final boolean persistent;

		StatisticType(String name) {
			this (name, true);
		}

		StatisticType(String name, boolean persistent) {
			this.name = name;
			this.persistent = persistent;
		}

		public String getName() {
			return name;
		}

		public boolean isPersistent() {
			return persistent;
		}
	}
}