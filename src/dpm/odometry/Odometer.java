/*
 * Odometer.java
 */

package dpm.odometry;

import dpm.util.Motors;
import lejos.robotics.RegulatedMotor;

public class Odometer extends Thread {
	
	// Constants
	public static final int	X = 0,
							Y = 1,
							THETA = 2;
	
	public static final boolean[] ALL = {true, true, true};
	
	// robot position
	private double x, y, theta;
	private int leftMotorTachoCount, rightMotorTachoCount;
	private RegulatedMotor leftMotor, rightMotor;
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private Object lock;
	
	private int lastTachoL, nowTachoL, lastTachoR, nowTachoR;

	// default constructor
	public Odometer() {
		this.leftMotor = Motors.getMotor(Motors.LEFT);
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		this.lastTachoL = 0;
		this.lastTachoR = 0;
		lock = new Object();
		
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		
		this.start();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();
			
			double distL, distR, deltaD, deltaT;
			
			//The following calculations determine deltaD, the change in
			//displacement of the robot, and deltaT, the change in orientation
			//of the robot.
			nowTachoL = leftMotor.getTachoCount();
			nowTachoR = rightMotor.getTachoCount();
			distL = Math.PI*Motors.WHEEL_RADIUS*(nowTachoL-lastTachoL)/180;
			distR = Math.PI*Motors.WHEEL_RADIUS*(nowTachoR-lastTachoR)/180;
			lastTachoL=nowTachoL;
			lastTachoR=nowTachoR;
			deltaD = 0.5*(distL+distR);
			deltaT = (distL-distR)/Motors.TRACK;	

			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. 
				 * Do not perform complex math
				 * 
				 */
				//theta is incremented by the change in orientation
				theta += deltaT;
				
				//the angle theta is kept positive.
				theta %= (2*Math.PI);
				while(theta < 0.0) {
					theta += 2*Math.PI;
				}
				
				//x is incremented by the change in displacement times cos of the orientation.
				x += deltaD * Math.sin(theta);
				
				//y is incremented by the change in displacement times sin of the orientation.
				y += deltaD * Math.cos(theta);
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}
	
	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}
	
	public void getPosition(double[] position){
		getPosition(position, ALL);
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
}