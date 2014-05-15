package com.github.etsija.impacttnt;

import java.util.logging.Logger;

//import org.bukkit.ChatColor;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import com.bergerkiller.bukkit.common.controller.EntityController;		// BKCommonLib is needed for this plugin to work!

import com.bergerkiller.bukkit.common.entity.CommonEntity;

public class ImpactTNTEvents implements Listener {
	
	Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	
	private enum Side {
		LEFT, RIGHT, FRONT, BACK
	}
	
	// Handles throwing of the TNTs
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final World world = player.getWorld();
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
			entity.setVelocity(direction.multiply(ImpactTNT.DEFAULT_POWER));
			if (player.getGameMode() != GameMode.CREATIVE){
				ItemStack is = player.getItemInHand();
				if (is.getAmount() > 1)
					is.setAmount(is.getAmount() - 1);
				else
					player.setItemInHand(null);
			}
		}		
	}
	
	@EventHandler
	public void onDispenserInteract(final PlayerInteractEvent event) {
		Block      b = event.getClickedBlock();
		Player     p = event.getPlayer();
		BlockFace bf = event.getBlockFace();
		
		if (b != null && b.getType() == Material.DISPENSER) {
			org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) b.getState();
			Inventory inv = dispenser.getInventory();
			ItemStack i = new ItemStack(Material.TNT, 1);
			ItemMeta im = i.getItemMeta();
			im.setDisplayName("ImpactTNT");
			i.setItemMeta(im);
			if (inv.containsAtLeast(i, 1)) {
				Location loc = dispenser.getLocation();
				CannonDispenser c = new CannonDispenser(0, 45);
				if (!ImpactTNT.cannons.containsKey(loc)) {
					ImpactTNT.cannons.put(loc, c);
				}
			}
			
			// Detect when player clicks the dispenser with a wooden stick, to change the direction/angle
			if ((event.getAction() == Action.LEFT_CLICK_BLOCK) &&
				(p.getItemInHand().getType() == Material.STICK)) {
				org.bukkit.material.Dispenser d = (org.bukkit.material.Dispenser) b.getState().getData();
				BlockFace face = d.getFacing();
				CannonDispenser c = ImpactTNT.cannons.get(dispenser.getLocation());
				boolean sneaking = p.isSneaking();
				if (bf == BlockFace.UP) {
					if (sneaking) {
						c.setAngle(c.getAngle() - 1);
					} else {
						c.setAngle(c.getAngle() + 1);
					}
				}
				if (calcSide(face, bf) == Side.LEFT) {
					if (sneaking) {
						c.setDirection(c.getDirection() - 5);
					} else {
						c.setDirection(c.getDirection() - 1);
					}
				} else if (calcSide(face, bf) == Side.RIGHT) {
					if (sneaking) {
						c.setDirection(c.getDirection() + 5);
					} else {
						c.setDirection(c.getDirection() + 1);
					}
				} else if (calcSide(face, bf) == Side.BACK) {
					if (sneaking) {
						c.setDirection(0);
						c.setAngle(45);
					} 
				}
				// Print out the current parameters of this cannon
				p.sendMessage(ChatColor.GREEN + "Direction: " 
							+ ChatColor.RED + String.format("%+03d", c.getDirection()) + "" 
							+ ChatColor.GREEN + " degrees, Angle: "
							+ ChatColor.RED + String.format("%02d", c.getAngle())     
							+ ChatColor.GREEN + " degrees");
			}
		}
	}
	
	// Remove this dispenser from the cannon list on block break
	@EventHandler
	public void onDispenserBreak(final BlockBreakEvent event) {
		Block      b = event.getBlock();
		Location loc = b.getLocation();
		
		if (b.getType() == Material.DISPENSER) {
			if (ImpactTNT.cannons.containsKey(loc)) {
				ImpactTNT.cannons.remove(loc);
			}
		}
	}
	
	// Handles the dispenser dispense event, when it has TNT to dispense
	@EventHandler
	public void onDispense(final BlockDispenseEvent event) {
		CannonDispenser c = null;
		org.bukkit.material.Dispenser d = (org.bukkit.material.Dispenser) event.getBlock().getState().getData();
		org.bukkit.block.Dispenser   d2 = (org.bukkit.block.Dispenser)    event.getBlock().getState();
		BlockFace face = d.getFacing();
		ItemStack i = event.getItem();
		Location loc = event.getBlock().getLocation();
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
			
			// Find the CannonDispenser entry from the list of cannons.  If this dispenser is not yet on the list, add it
			c = ImpactTNT.cannons.get(loc);
			if (c == null) {
				c = new CannonDispenser(0, 45);
				ImpactTNT.cannons.put(loc, c);
			}
			
			// Create the TNT entity and set up an entity controller for it
			Entity entity = createTNT(world, dispLocation, squaredSafetyDistance);
			
			// Shoot the TNT from the dispenser, using the speedFactor as a modifier
			// Also use this dispenser cannon's direction settings!
			Vector v = calcVelocityVector(face, c.getDirection(), c.getAngle());
			entity.setVelocity(v.multiply(speedFactor));
			d2.getInventory().removeItem(i);
		}
	}
	
	// Calculate the velocity vector for the projectile
	private Vector calcVelocityVector(BlockFace f, int dir, int angle) {
		double pitch = angle * Math.PI / 180;
		double yaw   = dir   * Math.PI / 180;
		double x = Math.sin(yaw) * Math.cos(pitch);
		double y = Math.cos(yaw) * Math.cos(pitch); 
		double z = Math.sin(pitch);
		
		if (f == BlockFace.NORTH) {
			Vector v = new Vector(x, z, -y);
			return v;
		} else if (f == BlockFace.EAST) {
			Vector v = new Vector(y, z, x);
			return v;
		} else if (f == BlockFace.SOUTH) {
			Vector v = new Vector(-x, z, y);
			return v;
		} else if (f == BlockFace.WEST) {
			Vector v = new Vector(-y, z, -x);
			return v;
		}
		return null;
	}
	
	// On which side the player clicks the dispenser, left or right?
	private Side calcSide(BlockFace facing, BlockFace bf) {
		Side side = null;
		if (facing == BlockFace.NORTH) {
			if (bf == BlockFace.WEST)
				side = Side.LEFT;
			else if (bf == BlockFace.EAST)
				side = Side.RIGHT;
			else if (bf == BlockFace.NORTH)
				side = Side.FRONT;
			else if (bf == BlockFace.SOUTH)
				side = Side.BACK;
		} else if (facing == BlockFace.EAST) {
			if (bf == BlockFace.NORTH)
				side = Side.LEFT;
			else if (bf == BlockFace.SOUTH)
				side = Side.RIGHT;
			else if (bf == BlockFace.EAST)
				side = Side.FRONT;
			else if (bf == BlockFace.WEST)
				side = Side.BACK;
		} else if (facing == BlockFace.SOUTH) {
			if (bf == BlockFace.EAST)
				side = Side.LEFT;
			else if (bf == BlockFace.WEST)
				side = Side.RIGHT;
			else if (bf == BlockFace.SOUTH)
				side = Side.FRONT;
			else if (bf == BlockFace.NORTH)
				side = Side.BACK;
		} else if (facing == BlockFace.WEST) {
			if (bf == BlockFace.SOUTH)
				side = Side.LEFT;
			else if (bf == BlockFace.NORTH)
				side = Side.RIGHT;
			else if (bf == BlockFace.WEST)
				side = Side.FRONT;
			else if (bf == BlockFace.EAST)
				side = Side.BACK;
		}
		return side;
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
