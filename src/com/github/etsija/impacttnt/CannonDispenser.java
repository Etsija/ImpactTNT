package com.github.etsija.impacttnt;

import org.bukkit.Location;
import org.bukkit.material.Dispenser;

public class CannonDispenser extends Dispenser {
	Location location;
	int direction;
	int angle;
	
	public CannonDispenser(Location loc, int dir, int angle) {
		this.location = loc;
		this.direction = dir;
		this.angle = angle;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public void setDirection(int direction) {
		if ((direction >= -ImpactTNT.maxSector) && (direction <= ImpactTNT.maxSector))
			this.direction = direction;
	}
	
	public int getAngle() {
		return this.angle;
	}
	
	public void setAngle(int angle) {
		if ((angle >= 0) && (angle <= ImpactTNT.maxAngle))
			this.angle = angle;
	}
	
}
