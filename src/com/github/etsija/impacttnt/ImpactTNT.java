package com.github.etsija.impacttnt;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ImpactTNT extends JavaPlugin {
	
	public static ImpactTNT impactTNT;
	Logger _log = Logger.getLogger("Minecraft");
	private PluginManager pm;
	private final ImpactTNTPlayerListener playerListener = new ImpactTNTPlayerListener();
	
	public static boolean expOnImpact;		// Does the TNT explode when hitting something other than air?
	public static int fuseTicks;			// How many ticks until it explodes anyway
	public static int expRadius;			// Explosion radius
	public static int safetyRadius;			// It won't explode until when it has reached this distance from the player who threw it
	
	@Override
	public void onEnable() {
		pm = this.getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		processConfigFile();
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
		
		// If config does not include a default parameter, add it
		for (final Entry<String, Object> e : defParams.entrySet())
			if (!config.contains(e.getKey()))
				config.set(e.getKey(), e.getValue());
		
		// Save default values to config.yml in datadirectory
		this.saveConfig();
		
		expOnImpact = getConfig().getBoolean("general.explodeonimpact");
		fuseTicks = getConfig().getInt("general.fuseticks");
		expRadius = getConfig().getInt("general.explosionradius");
		safetyRadius = getConfig().getInt("general.safetyradius");
	}		

/* Not needed yet
	// Handle the player commands of this plugin
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("impact")) {
			if (args.length < 1) {
				return false;
			}
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
			} else {
				if (args[0].equalsIgnoreCase("on")) {
					player.sendMessage(ChatColor.GREEN + "All TNT you lay down will now be impact TNT!");
					player.sendMessage(ChatColor.GREEN + "Cancel with '/impact off'");
					return true;
				} else if (args[0].equalsIgnoreCase("off")) {
					player.sendMessage(ChatColor.GREEN + "Impact TNT canceled.");
					player.sendMessage(ChatColor.GREEN + "All TNT you lay down will again be normal TNT!");
					return true;
				}
			}
		}
		return true;
	}

	public class eListener implements Listener {
		
		@EventHandler
		public void onEntityExplode(EntityExplodeEvent event) {
			
			EntityType entityType = event.getEntity().getType();
			
			// Listen only for the primed TNT entity
			if (!entityType.equals(EntityType.PRIMED_TNT)) {
				_log.info("not a primed TNT explosion event");
				return;
			}
			event.setCancelled(true);
		}
	}
*/
}
