package com.github.etsija.impacttnt;

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ImpactTNT extends JavaPlugin {
	
	public static ImpactTNT impactTNT;
	Logger _log = Logger.getLogger("Minecraft");
	private PluginManager pm;
	private final ImpactTNTEvents tntEvents = new ImpactTNTEvents();
	public static List<CannonDispenser> cannons = new ArrayList<CannonDispenser>();
	
	public static boolean expOnImpact;		// Does the TNT explode when hitting something other than air?
	public static int     fuseTicks;		// How many ticks until it explodes anyway
	public static int     expRadius;		// Explosion radius
	public static int     safetyRadius;		// It won't explode until when it has reached this distance from the player who threw it
	public static boolean reqNamedTNT;		// Does the plugin require special TNT renamed as "ImpactTNT" to work?
	public static boolean dispenserCannon;	// Do the dispensers work as cannons, shooting out ImpactTNT?
	
	@Override
	public void onEnable() {
		pm = this.getServer().getPluginManager();
		pm.registerEvents(tntEvents, this);
		processConfigFile();
		//processCannonFile();
		_log.info("[ImpactTNT] enabled.");
	}

	@Override
	public void onDisable() {
		_log.info("[ImpactTNT] disabled.");
	}

	// Handle reading & updating the config.yml
	public void processConfigFile() {

		final Map<String, Object> defParams = new HashMap<String, Object>();
		FileConfiguration config = this.getConfig();
		config.options().copyDefaults(true);
		
		// This is the default configuration
		defParams.put("general.explodeonimpact", true);
		defParams.put("general.fuseticks", 200);
		defParams.put("general.explosionradius", 5);
		defParams.put("general.safetyradius", 10);
		defParams.put("general.reqnamedtnt", false);
		defParams.put("general.dispensercannon", true);
		
		// If config does not include a default parameter, add it
		for (final Entry<String, Object> e : defParams.entrySet())
			if (!config.contains(e.getKey()))
				config.set(e.getKey(), e.getValue());
		
		// Save default values to config.yml in datadirectory
		this.saveConfig();
		
		// Read plugin config parameters from config.yml
		expOnImpact     = getConfig().getBoolean("general.explodeonimpact");
		fuseTicks       = getConfig().getInt("general.fuseticks");
		expRadius       = getConfig().getInt("general.explosionradius");
		safetyRadius    = getConfig().getInt("general.safetyradius");
		reqNamedTNT     = getConfig().getBoolean("general.reqnamedtnt");
		dispenserCannon = getConfig().getBoolean("general.dispensercannon");
	}		

	// Handle reading & updating the cannons.yml
	public void processCannonFile() {

		final Map<String, Object> defParams = new HashMap<String, Object>();
		FileConfiguration config = this.getConfig();
		config.options().copyDefaults(true);
		
		// This is the default configuration
		defParams.put("general.explodeonimpact", true);
		defParams.put("general.fuseticks", 200);
		defParams.put("general.explosionradius", 5);
		defParams.put("general.safetyradius", 10);
		defParams.put("general.reqnamedtnt", false);
		defParams.put("general.dispensercannon", true);
		
		// If config does not include a default parameter, add it
		for (final Entry<String, Object> e : defParams.entrySet())
			if (!config.contains(e.getKey()))
				config.set(e.getKey(), e.getValue());
		
		// Save default values to config.yml in datadirectory
		this.saveConfig();
		
		// Read plugin config parameters from config.yml
		expOnImpact     = getConfig().getBoolean("general.explodeonimpact");
		fuseTicks       = getConfig().getInt("general.fuseticks");
		expRadius       = getConfig().getInt("general.explosionradius");
		safetyRadius    = getConfig().getInt("general.safetyradius");
		reqNamedTNT     = getConfig().getBoolean("general.reqnamedtnt");
		dispenserCannon = getConfig().getBoolean("general.dispensercannon");
	}	
	
	// Plugin commands
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			return false;
		}
		
		// Hold TNT in hand -> /impact -> the TNT in your hand has been changed to "ImpactTNT"
		if (cmd.getName().equalsIgnoreCase("impact")) {
			if (reqNamedTNT) {
				if (args.length == 0) { 
					ItemStack i = player.getItemInHand();
					if (i.getType() == Material.TNT) {
						i = renameTNT(player, i);
					} else {
						return false;
					}
					player.sendMessage(ChatColor.GREEN + "One stack of TNT renamed");
					return true;
				} else if (args[0].equalsIgnoreCase("all")) {
					PlayerInventory pi = player.getInventory();
					for (ItemStack i : pi) {
						if (i != null) {
							i = renameTNT(player, i);
						}
					}
					player.sendMessage(ChatColor.GREEN + "All TNT in your inventory renamed");
					return true;
				}
			} else {
				player.sendMessage("ImpactTNT plugin not configured to need renamed TNT.");
				player.sendMessage("Set general.reqnamedtnt in config.yml to your needs.");
				return true;
			}
		}
		return false;
	}
	
	// Rename a stack of TNT into "ImpactTNT", or if it is already named as such, delete the name
	private ItemStack renameTNT(Player player, ItemStack i) {
		ItemStack iBack = i;
		ItemMeta im = iBack.getItemMeta();
		if (iBack.getType() == Material.TNT) {
			if (!im.hasDisplayName() ||
				!im.getDisplayName().equalsIgnoreCase("ImpactTNT")) {
				im.setDisplayName("ImpactTNT");
				iBack.setItemMeta(im);
			} else {
				im.setDisplayName(null);
				iBack.setItemMeta(im);
			}
		}
		return iBack;
	}
}