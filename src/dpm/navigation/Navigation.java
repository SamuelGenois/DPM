package dpm.navigation;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Motors;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;

/**
 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * Modified by: Samuel Genois and Emile Traoré
 * Fall 2016
 * 
 * Movement control class (turnTo, travelTo, flt, setSpeeds...)
 */
public class Navigation implements DPMConstants{
	private final static int FAST = 300, SLOW = 100;						//Motor speed parameters
	private final static double DEG_ERR = 3.0, CM_ERR = 1.0;				//Tolerances for turnTo and travelTo
	private RegulatedMotor leftMotor, rightMotor;							//Motor objects
	boolean interrupted;													//Determines whether methods are interrupted or not
	private double travel_x, travel_y;										//Coordinates of current travel
	private double[] badZone;
	
	/**
	 * Constructor
	 */
	public Navigation() {

		this.leftMotor = Motors.getMotor(Motors.LEFT);
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
		// set acceleration
		this.leftMotor.setAcceleration(WHEEL_MOTOR_ACCELERATION);
		this.rightMotor.setAcceleration(WHEEL_MOTOR_ACCELERATION);
		
		//Get absolute coordinates of the zone to avoid
		int[] badZoneSquareCoordinates;
		if(Repository.getRole() == BUILDER)
			badZoneSquareCoordinates = Repository.getGreenZone();
		else
			badZoneSquareCoordinates = Repository.getRedZone();
		
		badZone = new double[badZoneSquareCoordinates.length];
		for(int i=0; i<badZone.length; i++)
			badZone[i] = badZoneSquareCoordinates[i] * SQUARE_SIZE;
			
			
	}

	/**
	 * Function to set the motor speeds jointly
	 * @param lSpd the speed of the left motor
	 * @param rSpd the speed of the right motor
	 */
	void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
	}

	/**
	 * Interrupts currently running travelTo or turnTo methods.
	 */
	public void interrupt(){
		interrupted = true;
	}

	/**
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 * 
	 * @param x x coordinate of destination
	 * @param y y coordinate of destination
	 */
	public boolean travelTo(double x, double y, int avoidanceSetting) {
		travel_x = x;
		travel_y = y;
		double minAng;
		interrupted = false;
		while (!interrupted && (Math.abs(x - Repository.getX()) > CM_ERR || Math.abs(y - Repository.getY()) > CM_ERR)) {
			minAng = (Math.atan2(y - Repository.getY(), x - Repository.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			this.turnTo(minAng, false);
			this.setSpeeds(FAST, FAST);
			
			if(avoidanceSetting != NO_AVOIDANCE){
				ObstacleAvoidance avoidance = new ObstacleAvoidance(this, travel_x, travel_y, badZone);
				int obstacleDistance = avoidance.look();
				if(obstacleDistance < calculateDistance(x, y) && obstacleDistance < AVOIDANCE_THRESHOLD){
					this.setSpeeds(0, 0);
					if(avoidanceSetting == AVOID_ALL){
						if(!avoidance.avoid()){
							return false;
						}
					}
					if(avoidanceSetting == AVOID_OR_PICKUP){
						if(!Repository.quickPickup(obstacleDistance))
							if(!avoidance.avoid()){
								return false;
							}
					}
				}
			}
			
		}
		this.setSpeeds(0, 0);
		return true;
	}

	/**
	 * TurnTo function which takes an angle and boolean as arguments. The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 * 
	 * @param angle argument
	 * @param whether or not to stop the motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {
		interrupted = true;
		double error = angle - Repository.getAng();
		if (error > 180){
			error-=360;
		}
		if (error <= -180){
			error +=360;
		}
		if (Math.abs(error) > DEG_ERR){
			while (Math.abs(error) > DEG_ERR) {
				error = angle - Repository.getAng();
				if (error > 180){
					error-=360;
				}
				if (error <= -180){
					error +=360;
				}
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
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
		interrupted = false;
	}
	
	/**
	 * TurnTo function which takes an angle as argument.

	 * @param angle argument
	 */
	public void turnTo(double angle){
		turnTo(angle, true);
	}
	
	/**
	 * Calculates the euclidean distance between point (x,y) and the robot's current location
	 * 
	 * @param x The x coordinate of the destination
	 * @param y The y coordinate of the destination
	 * @return The euclidean distance
	 */
	double calculateDistance(double x, double y){
		return Math.sqrt(Math.pow(y-Repository.getY(), 2.0)+Math.pow(x-Repository.getX(), 2.0));
	}
	
	/**
	 * Returns the position of the robot.
	 * 
	 * @return the position of the robot
	 */
	double[] getPosition(){
		return Repository.getPosition();
	}
}
