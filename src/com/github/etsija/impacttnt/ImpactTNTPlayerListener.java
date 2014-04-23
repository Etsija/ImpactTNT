package com.github.etsija.impacttnt;

import java.util.logging.Logger;

//import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import com.bergerkiller.bukkit.common.controller.EntityController;		// BKCommonLib is needed for this plugin to work!

import com.bergerkiller.bukkit.common.entity.CommonEntity;

public class ImpactTNTPlayerListener implements Listener {
	
	Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	
	// Handles throwing of the TNTs
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final World world = player.getWorld();
		double speedFactor = 1.5;
		final Location handLocation = player.getLocation();
		handLocation.setY(handLocation.getY() + 1);
		// Safety radius squared (less complicated to calculate when srqt() left out)
		final double squaredSafetyDistance = ImpactTNT.safetyRadius * ImpactTNT.safetyRadius;
		
		Vector direction = handLocation.getDirection();
		Entity entity = null;
		
		boolean perms = player.isOp() || player.hasPermission("impacttnt.throw");
		if(perms && 
		   event.getMaterial() == Material.TNT &&
		   passedNameCheck(event) &&
		   event.getAction() == Action.LEFT_CLICK_AIR) {
			entity = world.spawn(handLocation, TNTPrimed.class);
			((TNTPrimed) entity).setFuseTicks(ImpactTNT.fuseTicks);
			
			// If TNT configured to detonate on impact
			if (ImpactTNT.expOnImpact) {
				CommonEntity<Entity> tntEntity = CommonEntity.get(entity);
				// This is a controller from Bergerkiller's awesome BKCommonLib
				tntEntity.setController(new EntityController<CommonEntity<TNTPrimed>>() {
					public void onTick() {
						super.onTick();
						// (Squared) distance of the thrown TNT from the thrower
						double squaredDistance = handLocation.distanceSquared(entity.getLocation());
						// This is the explosion logic: TNT explodes
						// 1. If its movement is impaired (ie. it hits a wall or something)
						// 2. If it's not moving anymore
						// 3. If it gets to the ground (1. and 2. do not seem to detect coming back to the ground)
						if (entity.isMovementImpaired() || !entity.isMoving() || entity.isOnGround()) {
							if (squaredDistance > squaredSafetyDistance) {
								entity.getEntity().setFuseTicks(0);
								return;
							} else {
								//player.sendMessage(ChatColor.GREEN + "ImpactTNT: safety fuse applied, immediate detonation cancelled");
								return;
							}
						}
					}
				});
			}
			
			// Throw the TNT, using the speedFactor as a modifier
			entity.setVelocity(direction.multiply(speedFactor));
			if (player.getGameMode() != GameMode.CREATIVE){
				ItemStack is = player.getItemInHand();
				if (is.getAmount() > 1)
					is.setAmount(is.getAmount() - 1);
				else
					player.setItemInHand(null);
			}
		}		
	}
	
	// Small method to test whether we need named TNT ("ImpactTNT") and whether the TNT is then named correctly
	private boolean passedNameCheck(PlayerInteractEvent event) {
		if (ImpactTNT.reqNamedTNT) {
			if (event.getItem().getItemMeta().hasDisplayName()) {
				return event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("ImpactTNT");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
}
