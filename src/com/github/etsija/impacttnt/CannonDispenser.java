package com.github.etsija.impacttnt;

import org.bukkit.material.Dispenser;

public class CannonDispenser extends Dispenser {
	int direction;
	int angle;
	
	public CannonDispenser(int dir, int angle) {
		this.direction = dir;
		this.angle = angle;
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
