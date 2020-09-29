package me.despical.kotl.handler.setup.components;

import com.github.despical.inventoryframework.GuiItem;
import com.github.despical.inventoryframework.pane.StaticPane;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.kotl.HookManager;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handler.setup.SetupInventory;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class MiscComponents implements SetupComponent {

	private SetupInventory setupInventory;

	@Override
	public void prepare(SetupInventory setupInventory) {
		this.setupInventory = setupInventory;
	}

	@Override
	public void injectComponents(StaticPane pane) {
		Player player = setupInventory.getPlayer();
		FileConfiguration config = setupInventory.getConfig();
		Arena arena = setupInventory.getArena();
		Main plugin = setupInventory.getPlugin();
		ItemStack hologramItem;

		if (plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.HOLOGRAPHIC_DISPLAYS)) {
			hologramItem = new ItemBuilder(XMaterial.ARMOR_STAND.parseItem())
				.name(plugin.getChatManager().colorRawMessage("&e&lSet King Hologram"))
				.lore(ChatColor.GRAY + "Click to set king's hologram location")
				.lore(ChatColor.GRAY + "on the place where you are standing.")
				.lore(ChatColor.DARK_GRAY + "(where the last king displays)")
				.lore("", setupInventory.getSetupUtilities()
				.isOptionDoneBool("instances." + arena.getId() + ".hologramLocation"))
				.build();
		} else {
			hologramItem = new ItemBuilder(XMaterial.BARRIER.parseItem())
				.name("&c&l&nSet King Hologram&n")
				.lore("This option requires HolographicDisplays")
				.lore("plugin. If you have other hologram plugin")
				.lore("you can use our placeholders in them too.")
				.lore("Check out our wiki page for placeholders.")
				.build();
		}

		pane.addItem(new GuiItem(hologramItem, e -> {
			if (!plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.HOLOGRAPHIC_DISPLAYS)) {
				return;
			}

			e.getWhoClicked().closeInventory();

			if(arena.getHologram() != null) {
				arena.getHologram().delete();
			}

			config.set("instances." + arena.getId() + ".hologramLocation", LocationSerializer.locationToString(player.getLocation()));
			player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aHologram location for arena " + arena.getId() + " set at your location!"));

			Hologram hologram = HologramsAPI.createHologram(plugin, player.getLocation());
			hologram.setAllowPlaceholders(true);
			hologram.appendTextLine(plugin.getChatManager().colorMessage("In-Game.Last-King-Hologram").replace("%king%", arena.getKing() == null ? plugin.getChatManager().colorMessage("In-Game.There-Is-No-King") : arena.getKing().getName()));

			arena.setHologram(hologram);
			arena.setHologramLocation(hologram.getLocation());
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 3, 0);		
		
		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.FILLED_MAP.parseItem())
			.name(plugin.getChatManager().colorRawMessage("&e&lView Wiki Page"))
			.lore(ChatColor.GRAY + "Having problems with setup or wanna know")
			.lore(ChatColor.GRAY + "some useful tips? Click to get wiki link!").build(), e -> {
			e.getWhoClicked().closeInventory();

			player.sendMessage(plugin.getChatManager().getPrefix()+ plugin.getChatManager().colorRawMessage("&aCheck out our wiki: https://github.com/Despical/KOTL/wiki"));
		}), 7, 0);
	}
}