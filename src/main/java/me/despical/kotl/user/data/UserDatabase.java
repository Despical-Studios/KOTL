package me.despical.kotl.user.data;

import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public interface UserDatabase {

	/**
	 * Saves player statistic into yaml or MySQL storage based on user choice
	 *
	 * @param user user to retrieve statistic from
	 * @param stat stat to save to storage
	 */
	void saveStatistic(User user, StatsStorage.StatisticType stat);

	/**
	 * Saves player statistic into yaml or MySQL storage based on user choice
	 *
	 * @param user user to retrieve statistic from
	 */
	void saveAllStatistic(User user);

	/**
	 * Loads player statistic from yaml or MySQL storage based on user choice
	 *
	 * @param user user to load statistic for
	 */
	void loadStatistics(User user);
}