package dpm.navigation;


import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import dpm.repository.Repository;
import dpm.util.*;

/**
 * A class containing methods related to obstacle avoidance
 * 
 * @author Emile Traoré
 * 
 */

public class ObstacleAvoidance extends Thread{
	
	
	private final int bandCenter = 40;		//The nominal distance from the wall
	private final int bandwidth = 6;		//The maximum deviation from the nominal distance before adjustment
	private double propConstant = 8.5 ;		//The proportional constant by which the error is multiplied
	private final int MOTOR_STRAIGHT = 150;	//The default speed of a motor
	private final int MAX_VALUE = 100;		//The maximum offset that can be added or removed from a motor's speed/
	private int distance;					//Current distance from the wall
	
	private RegulatedMotor leftMotor, rightMotor, sensorMotor;	//Motor variables
	private SampleProvider usSensor;							//Sensor variable
	private float[] usData;										//Sensor return array
	private double x_init, y_init, x_fin, y_fin;				//Initial/final position in x/y coordinate
	private double a, b;										//Parameter for slope of the line in symmetric form
	
	
	private int filterControl = 0;			//Variable for the filter, when incremented past FILTER_MAX the actual distance is adjusted to sensor distance
	private final int FILTER_OUT = 25;		//Variable for the filter, sets the maximum number of samples before actual distance is adjusted to sensor distance
	private final int MIN_DISTANCE = 100;	//Minimum distance considered for filtering
	
	
	private boolean left_direction;			//Determines whether the wall is avoided from the left (true) or right (false)
	private boolean avoidanceStarted;		//Determines if wall avoidance has started (robot started maneuvering)
	private boolean avoiding;				//Determines if wall avoidance is currently running
	
	/**
	 * Constructor
	 * @param x_fin	The x coordinate of the destination
	 * @param y_fin	The y coordinate of the destination
	 */
	public ObstacleAvoidance(double x_fin, double y_fin) {
		//Default Constructor
		this.leftMotor = Motors.getMotor(Motors.LEFT);
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
		this.usSensor = Sensors.getSensor(Sensors.US_ACTIVE);
		this.x_fin = x_fin;
		this.y_fin = y_fin;
		this.a = x_fin-x_init;
		this.b = y_fin=y_init;
		this.avoiding = true;
		this.avoidanceStarted = false;
	}
	
	/**
	 * Run method
	 * <br>Polls the ultrasonic sensor every 50ms to get the distance as an integer
	 * <br>Also filters out the reported distance if it is very large compared to the current distance and a large distance has not been reported enough times
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
	
	/**
	 * Direction method
	 * <br>Determines whether the robot will avoid the wall from the left or the right depending on whether there is more space on the left or the right
	 */
	public void direction(){
		int left_dist, right_dist;
		//Check distance on the left
		sensorMotor.rotate(-90);
		Delay.msDelay(100);
		left_dist = distance;
		//Check distance on the right
		sensorMotor.rotate(180);
		Delay.msDelay(100);
		right_dist = distance;
		//If largest distance on the left, align robot to be on left of wall
		if (left_dist > right_dist){
			left_direction = true;
			Repository.turnTo(Repository.getAng()+90);
		}
		//If largest distance on the right, align robot to be on right of wall
		else{
			left_direction = false;
			Repository.turnTo(Repository.getAng()-90);
			sensorMotor.rotate(-180);
		}
	}
	
	/**
	 * Process ultrasonic sensor distance method
	 * <br>Set motor speeds based on distance to the wall, using PController algorithm
	 */
	public void processUSDistance() {
		int fwd_dist, side_dist, actual_dist;
		//Get distance from wall on the side of the robot, rotate sensor, and get distance in front of the robot
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
		//Consider the minimum of these two as the distance from wall
		actual_dist = fwd_dist > side_dist ? side_dist : fwd_dist;
		//Compute error, check if error within bandwidth tolerance
		int error = actual_dist-bandCenter;
		//If not within tolerance, calculate adjustment to each motor by multiplying error by proportionality constant and clamp adjustment if higher than maximum value
		if (Math.abs(error) >= bandwidth){
			int adjustment = (int)(error*propConstant);
			if (adjustment > MAX_VALUE){
				adjustment = MAX_VALUE;
			}
			else if (adjustment < -1*MAX_VALUE){
				adjustment = -1*MAX_VALUE;
			}
			//If robot on left of wall, add adjustment to left motor speed and remove from right motor speed
			if (left_direction){
				leftMotor.setSpeed(MOTOR_STRAIGHT+adjustment);
				rightMotor.setSpeed(MOTOR_STRAIGHT-adjustment);
			}
			//If robot on right of wall, add adjustment to right motor speed and remove from right motor speed
			else{
				leftMotor.setSpeed(MOTOR_STRAIGHT-adjustment);
				rightMotor.setSpeed(MOTOR_STRAIGHT+adjustment);
			}
		}
		//If within tolerance, set both motors to move forward (no adjustment)
		else{
			rightMotor.setSpeed(MOTOR_STRAIGHT);
			leftMotor.setSpeed(MOTOR_STRAIGHT);
		}
	}
	
	/**
	 * End travel method
	 * <br>Stops wall avoidance if avoidanceStarted is true (robot already moved away from its expected trajectory) and it has returned to the same trajectory behind the obstacle
	 */
	public void endTravel(){
		if (Math.abs((Repository.getX()-x_init)/a-(Repository.getY()-y_init)/b) > 2.0){
			avoidanceStarted = true;
		}
		if (Math.abs((Repository.getX()-x_init)/a-(Repository.getY()-y_init)/b) < 1.0 && avoidanceStarted){
			avoiding = false;
		}
	}
	
	/**
	 * Method that controls the wall avoidance process
	 * @return A boolean that determines whether robot needs to continue moving towards destination or it is close enough (for navigation to determine what to do)
	 */
	public boolean doWallAvoidance(){
		//Start polling ultrasonic sensor and check which side wall avoidance should be done, then start wall avoidance
		this.start();
		direction();
		//While wall avoidance running, run the PController algorithm and check if back on initial trajectory, if yes, end travel
		while (avoiding){
			processUSDistance();
			endTravel();
		}
		//If within 10cm of the original destination (from travelTo), cancel travelTo (keepMoving = false), else continue travelTo (keepMoving = true)
		boolean keepMoving;
		double dist_error = Repository.calculateDistance(x_fin, y_fin);
		if (dist_error < 10.0){
			keepMoving = false;
		}
		else{
			keepMoving = true;
		}
		return keepMoving;
	}
}
