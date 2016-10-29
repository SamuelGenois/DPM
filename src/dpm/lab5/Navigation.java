/*
 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Movement control class (turnTo, travelTo, flt, localize)
 */

package dpm.lab5;

import dpm.util.MotorControl;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation {
	final static int FAST = 200, SLOW = 100, ACCELERATION = 4000;
	final static double DEG_ERR = 3.5, CM_ERR = 1.0;
	private boolean isNavigating, isTurning, stopNavigating;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	public Navigation(Odometer odo) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
		
		stopNavigating = false;
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x, double y) {
		double minAng;
		isNavigating = true;
		stopNavigating = false;
		while ((Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) && !stopNavigating) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			this.turnTo(minAng);
			this.setSpeeds(FAST, FAST);
		}
		this.setSpeeds(0, 0);
		isNavigating = false;
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 *
	public void turnTo(double angle, boolean stop) {
		isTurning = true;
		double error = angle - this.odometer.getAng();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
		
		isTurning = false;
	}*/
	
	/*
	 * Go foward a set distance in cm
	 */
	
	public void turnTo(double theta){
		isTurning = true;
		
		double turnAngle = calculateMinTurnAngle(theta);
		MotorControl motorControl = MotorControl.getInstance();
		
		motorControl.rotateMotorsSetAngle(convertAngle(MotorControl.WHEEL_RADIUS, MotorControl.TRACK, turnAngle),
				-convertAngle(MotorControl.WHEEL_RADIUS, MotorControl.TRACK, turnAngle));
		
		motorControl.stopMotors();
		
		isTurning = false;
	}
	
	/*
	 * The following two methods have been taken from Lab 2's SquareDriver class.
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	
	public double calculateMinTurnAngle(double theta){
		
		double minTurnAngle = theta-odometer.getAng();
		
		if(minTurnAngle < -180)
			minTurnAngle += 360;
		if(minTurnAngle > 180)
			minTurnAngle -= 360;
		
		return minTurnAngle;
	}
	
	public void goForward(double distance) {
		this.travelTo(Math.cos(Math.toRadians(this.odometer.getAng())) * distance, Math.cos(Math.toRadians(this.odometer.getAng())) * distance);

	}
	
	public boolean isNavigating(){
		return isNavigating;
	}
	
	public boolean isTurning(){
		return isTurning;
	}
	
	public void stopNavigation(){
		stopNavigating = true;
	}
}
