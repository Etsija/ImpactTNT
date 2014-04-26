package com.github.etsija.impacttnt;

import java.util.logging.Logger;

//import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import com.bergerkiller.bukkit.common.controller.EntityController;		// BKCommonLib is needed for this plugin to work!

import com.bergerkiller.bukkit.common.entity.CommonEntity;

public class ImpactTNTEvents implements Listener {
	
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
		
		boolean perms = player.isOp() || player.hasPermission("impacttnt.throw");
		if(perms && 
		   event.getMaterial() == Material.TNT &&
		   passedNameCheckForPlayer(event) &&
		   event.getAction() == Action.LEFT_CLICK_AIR) {
			
			// Create the TNT entity and set up an entity controller for it
			Entity entity = createTNT(world, handLocation, squaredSafetyDistance);
			
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
	
	// Handles the dispenser dispense event, when it has TNT to dispense
	@EventHandler
	public void onDispense(final BlockDispenseEvent event) {
		org.bukkit.material.Dispenser d = (org.bukkit.material.Dispenser) event.getBlock().getState().getData();
		org.bukkit.block.Dispenser   d2 = (org.bukkit.block.Dispenser)    event.getBlock().getState();
		BlockFace face = d.getFacing();
		ItemStack i = event.getItem();
		final World world = event.getBlock().getWorld();
		double speedFactor = 1.5;
		final Location dispLocation = event.getBlock().getRelative(face).getLocation();
		dispLocation.setY(dispLocation.getY() + 1);
		// Safety radius squared (less complicated to calculate when srqt() left out)
		final double squaredSafetyDistance = ImpactTNT.safetyRadius * ImpactTNT.safetyRadius;
		
		// The item to dispense is valid TNT and the dispensers work as cannons
		if (ImpactTNT.dispenserCannon &&
			(i.getType() == Material.TNT) &&
			passedNameCheckForDispenser(event)) {
			event.setCancelled(true);
			
			// Create the TNT entity and set up an entity controller for it
			Entity entity = createTNT(world, dispLocation, squaredSafetyDistance);
			
			// Shoot the TNT from the dispenser, using the speedFactor as a modifier
			Vector v = new Vector (entity.getLocation().getX() - event.getBlock().getLocation().getX(), 
								   entity.getLocation().getY() - event.getBlock().getLocation().getY(), 
								   entity.getLocation().getZ() - event.getBlock().getLocation().getZ());
			
			entity.setVelocity(v.normalize().multiply(speedFactor));
			d2.getInventory().removeItem(i);
		}
	}
	
	// Create the ImpactTNT and set an entity controller to control it
	private Entity createTNT(final World world, final Location loc, final double sqSafetyDist) {
		Entity entity = world.spawn(loc, TNTPrimed.class);
		((TNTPrimed) entity).setFuseTicks(ImpactTNT.fuseTicks);
		
		// If TNT configured to detonate on impact
		if (ImpactTNT.expOnImpact) {
			CommonEntity<Entity> tntEntity = CommonEntity.get(entity);
			// This is a controller from Bergerkiller's awesome BKCommonLib
			tntEntity.setController(new EntityController<CommonEntity<TNTPrimed>>() {
				public void onTick() {
					super.onTick();
					// (Squared) distance of the thrown TNT from the thrower
					double squaredDistance = loc.distanceSquared(entity.getLocation());
					// This is the explosion logic: TNT explodes
					// 1. If its movement is impaired (ie. it hits a wall or something)
					// 2. If it's not moving anymore
					// 3. If it gets to the ground (1. and 2. do not seem to detect coming back to the ground)
					if (entity.isMovementImpaired() || !entity.isMoving() || entity.isOnGround()) {
						if (squaredDistance > sqSafetyDist) {
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
		return entity;
	}
	
	// Small method to test whether we need named TNT ("ImpactTNT") and whether the TNT is then named correctly
	private boolean passedNameCheckForPlayer(PlayerInteractEvent event) {
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
	
	// Small method to test whether we need named TNT ("ImpactTNT") and whether the TNT is then named correctly
	// This is the dispenser variant
	private boolean passedNameCheckForDispenser(BlockDispenseEvent event) {
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
