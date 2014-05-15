package com.github.etsija.impacttnt;

public class CannonDispenser {
	int direction;
	int angle;
	float power;
	
	public CannonDispenser(int dir, int angle) {
		this.direction = dir;
		this.angle = angle;
		this.power = ImpactTNT.DEFAULT_POWER;
	}
	
	public CannonDispenser(int dir, int angle, float power) {
		this.direction = dir;
		this.angle = angle;
		this.power = power;
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
	
	public float getPower() {
		return this.power;
	}
	
	public void setPower(float power) {
		if ((power >= ImpactTNT.minPower) && (power <= ImpactTNT.maxPower))
			this.power = power;
	}
}
