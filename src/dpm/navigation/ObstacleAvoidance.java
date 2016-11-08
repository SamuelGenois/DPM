package dpm.navigation;

import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import dpm.odometry.*;
import dpm.repository.Repository;
import dpm.util.*;

/**
 * A class containing methods related to obstacle avoidance
 * 
 * @author Emile Traoré
 * 
 */

public class ObstacleAvoidance extends Thread{
	
	/**The nominal distance from the wall*/
	private final int bandCenter = 40;
	
	/**The maximum deviation from the nominal distance before adjustment*/
	private final int bandwidth = 6;
	
	/**The proportional constant by which the error is multiplied*/
	private double propConstant = 8.5 ;
	
	/**The default speed of a motor*/
	private final int MOTOR_STRAIGHT = 150;
	
	/**The maximum offset that can be added or removed from a motor's speed*/
	private final int MAX_VALUE = 100;
	
	/**Current distance from the wall*/
	private int distance;
	
	/**Motor variable*/
	private RegulatedMotor leftMotor, rightMotor, sensorMotor;
	
	/**Sensor variable*/
	private SampleProvider usSensor;
	
	/**Sensor return array*/
	private float[] usData;
	
	/**Initial/final position in x/y coordinate*/
	private double x_init, y_init, x_fin, y_fin;
	
	/**Parameter for slope of the line in symmetric form*/
	private double a, b;
	
	/**Variable for the filter, when incremented past FILTER_MAX the actual distance is adjusted to sensor distance*/
	private int filterControl = 0;
	
	/**Variable for the filter, sets the maximum number of samples before actual distance is adjusted to sensor distance*/
	private final int FILTER_OUT = 25;
	
	/**Minimum distance considered for filtering*/
	private final int MIN_DISTANCE = 100; 
	
	/**Odometer object for using Odometer.java methods*/
	private Odometer odo;
	
	/**Navigation object for using Navigation.java methods*/
	private Navigation nav;
	
	/**Determines whether the wall is avoided from the left (true) or right (false)*/
	private boolean left_direction;
	
	/**Determines if wall avoidance has started (robot started maneuvering)*/
	private boolean avoidanceStarted;
	
	/**Determines if wall avoidance is currently running*/
	private boolean avoiding;
	
	/**Constructor
	 * 
	 * @param odo 	The robot's odometer object
	 * @param nav	The robot's navigation object
	 * @param x_fin	The x coordinate of the destination
	 * @param y_fin	The y coordinate of the destination
	 */
	public ObstacleAvoidance(Navigation nav, double x_fin, double y_fin) {
		//Default Constructor
		this.leftMotor = Motors.getMotor(Motors.LEFT);
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
		this.usSensor = Sensors.getSensor(Sensors.US_ACTIVE);
		this.odo = Repository.getOdometer();
		this.nav = nav;
		this.x_init = odo.getX();
		this.y_init = odo.getY();
		this.x_fin = x_fin;
		this.y_fin = y_fin;
		this.a = x_fin-x_init;
		this.b = y_fin=y_init;
		this.avoiding = true;
		this.avoidanceStarted = false;
	}
	
	/**Run method
	 * Polls the ultrasonic sensor every 50ms to get the distance as an integer
	 * Also filters out the reported distance if it is very large compared to the current distance and a large distance has not been reported enough times
	 */
	public void run(){
		while (true) {
			int distance;
			usSensor.fetchSample(usData,0);
			distance=(int)(usData[0]*100.0);
			try { Thread.sleep(50); } catch(Exception e){}
			if (distance < MIN_DISTANCE || this.distance > bandCenter+bandwidth || filterControl > FILTER_OUT){
				synchronized (this){
					this.distance = distance;
				}
				filterControl = 0;
			}
			else{
				filterControl++;
			}
		}
	}
	
	/**Direction method
	 * Determines whether the robot will avoid the wall from the left or the right depending on whether there is more space on the left or the right
	 * First scans the distance on both sides and records on which side there is more space
	 * Then aligns the robot and ultrasonic sensor with the wall on the correct side
	 */
	public void direction(){
		int left_dist, right_dist;
		sensorMotor.rotate(-90);
		left_dist = distance;
		sensorMotor.rotate(180);
		right_dist = distance;
		if (left_dist > right_dist){
			left_direction = true;
			nav.turnTo(odo.getAng()-90);
		}
		else{
			left_direction = false;
			nav.turnTo(odo.getAng()+90);
			sensorMotor.rotate(-180);
		}
	}
	
	/**Process ultrasonic sensor distance method
	 * Reads the distance reported by the ultrasonic sensor and adjusts motors in a proportional control way
	 * First scans both the distance in front and on the side of the wall and considers the minimum of these two
	 * Then checks whether the error (difference from the nominal distance) is smaller than the bandwidth (if not: sets both motors to forward speed)
	 * If it's bigger, compute adjustment proportionally to the error (with clipping if exceeds the maximum value)
	 * Then increase one motor's speed and decrease the other one's by the adjustment (based on which side of the wall robot is on)
	 */
	public void processUSDistance() {
		int fwd_dist, side_dist, actual_dist;
		if (left_direction){
			side_dist = distance;
			sensorMotor.rotate(-90);
			fwd_dist = distance;
			sensorMotor.rotate(90);
		}
		else{
			side_dist = distance;
			sensorMotor.rotate(90);
			fwd_dist = distance;
			sensorMotor.rotate(-90);
		}
		actual_dist = fwd_dist > side_dist ? side_dist : fwd_dist;
		
		int error = actual_dist-bandCenter;
		if (Math.abs(error) >= bandwidth){
			int adjustment = (int)(error*propConstant);
			if (adjustment > MAX_VALUE){
				adjustment = MAX_VALUE;
			}
			else if (adjustment < -1*MAX_VALUE){
				adjustment = -1*MAX_VALUE;
			}
			if (left_direction){
				leftMotor.setSpeed(MOTOR_STRAIGHT+adjustment);
				rightMotor.setSpeed(MOTOR_STRAIGHT-adjustment);
			}
			else{
				leftMotor.setSpeed(MOTOR_STRAIGHT-adjustment);
				rightMotor.setSpeed(MOTOR_STRAIGHT+adjustment);
			}
		}

		else{
			rightMotor.setSpeed(MOTOR_STRAIGHT);
			leftMotor.setSpeed(MOTOR_STRAIGHT);
		}
	}
	
	/**End travel method
	 * Stops wall avoidance if avoidanceStarted is true (robot already moved away from its expected trajectory) and it has returned to the same trajectory behind the obstacle
	 */
	public void endTravel(){
		if (Math.abs((odo.getX()-x_init)/a-(odo.getY()-y_init)/b) > 2.0){
			avoidanceStarted = true;
		}
		if (Math.abs((odo.getX()-x_init)/a-(odo.getY()-y_init)/b) < 1.0 && avoidanceStarted){
			avoiding = false;
		}
	}
	
	/**Do wall avoidance method
	 * Starts the ultrasonic sensor polling
	 * Determines the side of the wall on which to do wall following
	 * Runs wall avoidance until it ends (robot is back on initial trajectory)
	 * @return A boolean that determines whether robot needs to continue moving towards destination or it is close enough (for navigation to determine what to do)
	 */
	public boolean doWallAvoidance(){
		this.start();
		direction();
		while (avoiding){
			processUSDistance();
			endTravel();
		}
		boolean keepMoving;
		double dist_error = calculateDistance(x_fin, y_fin);
		if (dist_error < 10.0){
			keepMoving = false;
		}
		else{
			keepMoving = true;
		}
		return keepMoving;
	}
	
	private double calculateDistance(double x, double y){
		return Math.sqrt(Math.pow(y-odo.getY(), 2.0)+Math.pow(x-odo.getX(), 2.0));
	}

}
