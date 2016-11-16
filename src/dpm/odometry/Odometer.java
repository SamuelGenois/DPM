package dpm.odometry;

import lejos.utility.Timer;
import lejos.utility.TimerListener;
import dpm.util.Motors;
import lejos.robotics.RegulatedMotor;

/**
 * File: Odometer.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * Modified by: Samuel Genois
 * Fall 2016
 * 
 * Class which controls the odometer for the robot
 * 
 * Odometer defines cooridinate system as such...
 * 
 * 					90Deg:pos y-axis
 * 							|
 * 							|
 * 							|
 * 							|
 * 180Deg:neg x-axis------------------0Deg:pos x-axis
 * 							|
 * 							|
 * 							|
 * 							|
 * 					270Deg:neg y-axis
 * 
 * The odometer is initalized to 90 degrees, assuming the robot is facing up the positive y-axis
 * 
 */
public class Odometer implements TimerListener {

	private Timer timer;
	private RegulatedMotor leftMotor, rightMotor;
	private static final int	DEFAULT_TIMEOUT_PERIOD = 20,
								DEFAULT_INTERVAL = 30;
	private double leftRadius, rightRadius, width;
	private double x, y, theta;
	private double[] oldDH, dDH;
	
	/**
	 * Constructor
	 */
	public Odometer(){
		this(Motors.getMotor(Motors.LEFT), Motors.getMotor(Motors.RIGHT), DEFAULT_INTERVAL, true);
	}
	
	/**
	 * Alternative constructor
	 */
	public Odometer (RegulatedMotor leftMotor, RegulatedMotor rightMotor, int INTERVAL, boolean autostart) {
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		// default values, modify for your robot
		this.rightRadius = 2.1;
		this.leftRadius = 2.1;
		this.width = 15.8;
		
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 90.0;
		this.oldDH = new double[2];
		this.dDH = new double[2];

		if (autostart) {
			// if the timeout interval is given as <= 0, default to 20ms timeout 
			this.timer = new Timer((INTERVAL <= 0) ? INTERVAL : DEFAULT_TIMEOUT_PERIOD, this);
			this.timer.start();
		} else
			this.timer = null;
	}
	
	// functions to start/stop the timerlistener
	/**
	 * Stops the TimerListener
	 */
	public void stop() {
		if (this.timer != null)
			this.timer.stop();
	}
	
	/**
	 * Starts the TimerListener
	 */
	public void start() {
		if (this.timer != null)
			this.timer.start();
	}
	
	/**
	 * Calculates displacement and heading as title suggests
	 * 
	 * @param data
	 */
	private void getDisplacementAndHeading(double[] data) {
		int leftTacho, rightTacho;
		leftTacho = leftMotor.getTachoCount();
		rightTacho = rightMotor.getTachoCount();

		data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) * Math.PI / 360.0;
		data[1] = (rightTacho * rightRadius - leftTacho * leftRadius) / width;
	}
	
	/**
	 * Recompute the odometer values using the displacement and heading changes
	 */
	public void timedOut() {
		this.getDisplacementAndHeading(dDH);
		dDH[0] -= oldDH[0];
		dDH[1] -= oldDH[1];

		// update the position in a critical region
		synchronized (this) {
			theta += dDH[1];
			theta = fixDegAngle(theta);

			x += dDH[0] * Math.cos(Math.toRadians(theta));
			y += dDH[0] * Math.sin(Math.toRadians(theta));
		}

		oldDH[0] += dDH[0];
		oldDH[1] += dDH[1];
	}

	/** Returns the X value
	 * 
	 * @return X value
	 */
	public double getX() {
		synchronized (this) {
			return x;
		}
	}

	/** Returns the Y value
	 * 
	 * @return Y value
	 */
	public double getY() {
		synchronized (this) {
			return y;
		}
	}

	/** Returns the theta value
	 * 
	 * @return theta value
	 */
	public double getAng() {
		synchronized (this) {
			return theta;
		}
	}

	/** Sets x, y and theta
	 * 
	 * @param position new x, y and theta values
	 * @param update which values to update
	 */
	public void setPosition(double[] position, boolean[] update) {
		synchronized (this) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	/** Puts x, y and theta into a provided array
	 * 
	 * @param position array to put x, y and theta values
	 */
	public void getPosition(double[] position) {
		synchronized (this) {
			position[0] = x;
			position[1] = y;
			position[2] = theta;
		}
	}

	/** Returns x, y and theta as an array
	 * 
	 * @param position new x, y and theta values
	 */
	public double[] getPosition() {
		synchronized (this) {
			return new double[] { x, y, theta };
		}
	}

	// static 'helper' methods
	private static double fixDegAngle(double angle) {
		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);

		return angle % 360.0;
	}

	@SuppressWarnings("unused")
	private static double minimumAngleFromTo(double a, double b) {
		double d = fixDegAngle(b - a);

		if (d < 180.0)
			return d;
		else
			return d - 360.0;
	}
}
