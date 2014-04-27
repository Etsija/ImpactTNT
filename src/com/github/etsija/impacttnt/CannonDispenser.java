package com.github.etsija.impacttnt;

import org.bukkit.Location;
import org.bukkit.material.Dispenser;

public class CannonDispenser extends Dispenser {
	//org.bukkit.block.Dispenser dispenser;
	Location location;
	int direction;
	int angle;
	
	//public CannonDispenser(org.bukkit.block.Dispenser disp, int dir, int angle) {
	public CannonDispenser(Location loc, int dir, int angle) {
		//this.dispenser = disp;
		this.location = loc;
		this.direction = dir;
		this.angle = angle;
		//this.setLocation();
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void setLocation(Location location) {
		//this.location = this.dispenser.getLocation();
		this.location = location;
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public void setDirection(int direction) {
		if ((direction >= -45) && (direction <= 45))
			this.direction = direction;
	}
	
	public int getAngle() {
		return this.angle;
	}
	
	public void setAngle(int angle) {
		if ((angle >= 0) && (angle <= 60))
			this.angle = angle;
	}
	
}
