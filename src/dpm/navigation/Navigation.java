package dpm.navigation;

import dpm.repository.Repository;
import dpm.util.Motors;
import dpm.util.Printer;
import dpm.util.Sensors;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

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
public class Navigation extends Thread{
	private final static int FAST = 200, SLOW = 100, ACCELERATION = 4000;	//Motor speed parameters
	private final static double DEG_ERR = 3.0, CM_ERR = 1.0;				//Tolerances for turnTo and travelTo
	private final static int AVOIDANCE_THRESHOLD = 20;						//Distance below which obstacle avoidance engaged
	private RegulatedMotor leftMotor, rightMotor;							//Motor objects
	private SampleProvider usSensor;										//Sensor object
	private float[] usData;													//Sensor data array	
	private boolean interrupted;											//Determines whether methods are interrupted or not
	private int distance;													//US sensor distance in cm
	private double travel_x, travel_y;										//Coordinates of current travel

	/**
	 * Constructor
	 */
	public Navigation() {

		this.leftMotor = Motors.getMotor(Motors.LEFT);
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
		this.usSensor = Sensors.getSensor(Sensors.US_ACTIVE);
		this.usData = new float[usSensor.sampleSize()];
		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}
	
	public void run(){
		while (!interrupted) {
			usSensor.fetchSample(usData,0);
			distance=(int)(usData[0]*100.0);
			if (distance < AVOIDANCE_THRESHOLD  && distance != 0){
				this.interrupt();
				Repository.doAvoidance(travel_x, travel_y);
			}
			try { Thread.sleep(50); } catch(Exception e){}
			Printer.getInstance().display("   "+(int)Repository.getX()+"   "+(int)Repository.getY());
		}
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	private void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
	}

	/**
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
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
	public void travelTo(double x, double y) {
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
		}
		this.setSpeeds(0, 0);
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
		if (angle < 0){
			angle+=360;
		}
		else if (angle > 360){
			angle-=360;
		}
		double error = angle - Repository.getAng();
		while (Math.abs(error) > DEG_ERR) {

			error = angle - Repository.getAng();

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
	 * Go forward a set distance in cm
	 * 
	 * @param distance the forward distance to travel
	 */
	public void goForward(double distance) {
		this.travelTo(Math.cos(Math.toRadians(Repository.getAng())) * distance, Math.cos(Math.toRadians(Repository.getAng())) * distance);

	}
	
	/**
	 * Calculates the euclidean distance between point (x,y) and the robot's current location
	 * @param x The x coordinate of the destination
	 * @param y The y coordinate of the destination
	 * @return The euclidean distance
	 */
	public double calculateDistance(double x, double y){
		return Math.sqrt(Math.pow(y-Repository.getY(), 2.0)+Math.pow(x-Repository.getX(), 2.0));
	}
}
