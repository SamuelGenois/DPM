package dpm.navigation;

import dpm.repository.Repository;
import dpm.util.Motors;
import dpm.util.Sensors;

public class ObstacleAvoidanceSam {
	
	private final static double	propConstant = 8.5,			//The proportional constant by which the error is multiplied
								MINIMUM_DETOUR_LENGTH = 10;

	private final static int	AVOIDANCE_THRESHOLD = 20,	//Distance below which obstacle avoidance engaged
								FORWARD = 0,
								LEFT = 1,
								RIGHT = 2,
								SENSOR_TURN_ANGLE = 60,
								MOTOR_STRAIGHT = 150,		//The default speed of a motor
								MAX_VALUE = 100,			//The maximum offset that can be added or removed from a motor's speed
								bandCenter = 22,			//The nominal distance from the wall
								bandwidth = 5;				//The maximum deviation from the nominal distance before adjustment
	
	Navigation navigation;
	
	private int direction;
	private double x_init, y_init, x_fin, y_fin, initialDistanceFromDestination;
	
	/**
	 * Constructor
	 * 
	 * @param x_fin	The x coordinate of the destination
	 * @param y_fin	The y coordinate of the destination
	 */
	public ObstacleAvoidanceSam(Navigation navigation, double x_fin, double y_fin) {
		this.navigation = navigation;
		this.x_init = this.navigation.getPosition()[0];
		this.y_init = this.navigation.getPosition()[1];
		this.x_fin = x_fin;
		this.y_fin = y_fin;
		this.initialDistanceFromDestination = navigation.calculateDistance(this.x_fin, this.y_fin);
	}
	
	private boolean avoiding(){
		return navigation.calculateDistance(x_init, y_init) < MINIMUM_DETOUR_LENGTH
				//This part of the logic was taken from Emile's original ObstacleAvoidance. I do not understand it yet.
				|| Math.abs((Repository.getX()-x_init)/(x_fin-x_init)-(Repository.getY()-y_init)/(y_fin-y_init)) < 2.0;
	}
	
	/*
	 * Direction method
	 * <br>Determines whether the robot will avoid the wall from the left or the right depending on whether there is more space on the left or the right
	 */
	private void direction(){
		int left_dist, right_dist;
		//Check distance on the left
		left_dist = look(LEFT);
		//Check distance on the right
		right_dist = look(RIGHT);
		//If largest distance on the left, align robot to be on left of wall
		if (left_dist > right_dist){
			direction = LEFT;
			navigation.turnTo(navigation.getPosition()[2]+90);
		}
		//If largest distance on the right, align robot to be on right of wall
		else{
			direction = RIGHT;
			navigation.turnTo(navigation.getPosition()[2]-90);
		}
	}
	
	private void processUSDistance(){
		int fwd_dist, side_dist, actual_dist;
		//Get distance from wall on the side of the robot, rotate sensor, and get distance in front of the robot
		side_dist = look(direction);
		fwd_dist = look(FORWARD);
			
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
			if (direction == LEFT){
				navigation.setSpeeds(MOTOR_STRAIGHT+adjustment, MOTOR_STRAIGHT-adjustment);
			}
			//If robot on right of wall, add adjustment to right motor speed and remove from right motor speed
			else{
				navigation.setSpeeds(MOTOR_STRAIGHT-adjustment, MOTOR_STRAIGHT+adjustment);
			}
		}
		//If within tolerance, set both motors to move forward (no adjustment)
		else{
			navigation.setSpeeds(MOTOR_STRAIGHT, MOTOR_STRAIGHT);
		}
	}
	
	public boolean avoid(){
		direction();
		while (avoiding() && !navigation.interrupted){
			processUSDistance();
		}
		
		return navigation.calculateDistance(x_init, y_init) > initialDistanceFromDestination;
	}
	
	public static boolean travelPathIsBlocked(){
		return look(FORWARD) < AVOIDANCE_THRESHOLD;
	}
	
	//Returns the distance read by the ultrasonic sensor when the sensor is facing the specified direction.
	private static int look(int direction){
		switch(direction){
		case LEFT:
			Motors.getMotor(Motors.SENSOR).rotateTo(-SENSOR_TURN_ANGLE);
			return getDistance();
		case RIGHT:
			Motors.getMotor(Motors.SENSOR).rotateTo(SENSOR_TURN_ANGLE);
			return getDistance();
		case FORWARD:
		default:
			Motors.getMotor(Motors.SENSOR).rotateTo(0);
			return getDistance();
		}
	}
	
	//Returns the distance read by the ultrasonic sensor.
	private static int getDistance(){
		float[] data = new float[Sensors.getSensor(Sensors.US_ACTIVE).sampleSize()];
		Sensors.getSensor(Sensors.US_ACTIVE).fetchSample(data, 0);
		return (int)(data[0]*100);
	}
}
