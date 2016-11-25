package dpm.navigation;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Motors;
import dpm.util.Printer;
import dpm.util.Sensors;

/**
 * This class handles the routines Navigation uses to avoid obstacles in its path.
 * 
 * @author Samuel Genois, Emile Traoré
 *
 */
public class ObstacleAvoidance implements DPMConstants{
	
	private final static double	PROB_CONSTANT = 4;					//The proportional constant by which the error is multiplied

	private final static int	FORWARD = 0,
								LEFT = 1,
								RIGHT = 2,
								SENSOR_TURN_ANGLE = 60,
								MOTOR_STRAIGHT = 150,				//The default speed of a motor
								MAX_VALUE = 50,						//The maximum offset that can be added or removed from a motor's speed
								BAND_CENTER = AVOIDANCE_THRESHOLD,	//The nominal distance from the wall
								BAND_WIDTH = 5;						//The maximum deviation from the nominal distance before adjustment
	
	private Navigation navigation;
	
	private int direction;
	private double x_init, y_init, a, b, initialDistanceFromDestination;
	private boolean startedAvoidance;
	
	/**
	 * Constructor
	 * 
	 * @param navigation the Navigation object using this ObstacleAvoidance
	 * @param x_fin	The x coordinate of the destination
	 * @param y_fin	The y coordinate of the destination
	 */
	public ObstacleAvoidance(Navigation navigation, double x_fin, double y_fin) {
		this.navigation = navigation;
		this.x_init = this.navigation.getPosition()[0];
		this.y_init = this.navigation.getPosition()[1];
		this.a = (x_fin-x_init)/Math.sqrt((x_fin-x_init)*(x_fin-x_init)+(y_fin-y_init)*(y_fin-y_init));
		this.b = (y_fin-y_init)/Math.sqrt((x_fin-x_init)*(x_fin-x_init)+(y_fin-y_init)*(y_fin-y_init));
		this.initialDistanceFromDestination = navigation.calculateDistance(x_fin, y_fin);
		this.startedAvoidance = false;
	}
	
	/*
	 * Calculates the error (deviation of robot from its initial path)
	 */
	private double calculateError(){
		if (a == 0){
			return Math.abs(navigation.getPosition()[1]-y_init)/b;
		}
		if (b == 0){
			return Math.abs(navigation.getPosition()[0]-x_init)/a;
		}
		return Math.abs((Repository.getX()-x_init)/a-(Repository.getY()-y_init)/b);
	}
	
	/*
	 * Returns true as long as the robot is not done avoiding the obstacle
	 */
	private boolean avoiding(double startOrientation){
		double endAngle = Repository.getAng()-startOrientation;
		if (endAngle > 180){
			endAngle-=360;
		}
		else if (endAngle <= -180){
			endAngle+=360;
		}
		if (Math.abs(endAngle) > 170){
			return false;
		}
		return true;
		/*
		Printer.getInstance().display(""+calculateError());
		if (calculateError() > 8.0){
			startedAvoidance = true;
		}
		
		if(startedAvoidance && calculateError() < 4.0){
			return false;
		}
		return true;*/
	}
	
	/*
	 * Direction method
	 * Determines whether the robot will avoid the wall from the left or the right depending on whether there is more space on the left or the right
	 */
	private void direction(){
		int left_dist, right_dist;
		//Check distance on the left
		left_dist = look(RIGHT);
		//Check distance on the right
		right_dist = look(LEFT);
		//If largest distance on the left, align robot to be on left of wall
		if (left_dist > right_dist){
			direction = LEFT;
			navigation.turnTo(navigation.getPosition()[2]+90);
		}
		//If largest distance on the right, align robot to be on right of wall
		else{
			direction = RIGHT;
			navigation.turnTo(navigation.getPosition()[2]+270);
		}
	}
	
	/*
	 * Process ultrasonic sensor distance method
	 * Adjusts motor speeds based on distance to the wall using PController algorithm
	 */
	private void processUSDistance(){
		int fwd_dist, side_dist, actual_dist;
		//Get distance from wall on the side of the robot, rotate sensor, and get distance in front of the robot
		side_dist = look(direction);
		fwd_dist = look(FORWARD);
			
		//Consider the minimum of these two as the distance from wall
		actual_dist = fwd_dist > side_dist ? side_dist : fwd_dist;
		//Compute error, check if error within bandwidth tolerance
		int error = actual_dist-BAND_CENTER;
		//If not within tolerance, calculate adjustment to each motor by multiplying error by proportionality constant and clamp adjustment if higher than maximum value
		if (Math.abs(error) >= BAND_WIDTH){
			int adjustment = (int)(error*PROB_CONSTANT);
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
	
	/**
	 * Executes the obstacle avoidance routine
	 * @return false if the obstacle avoided is occupying Navigation's destination
	 */
	public boolean avoid(){
		direction();
		double startOrientation = Repository.getAng();
		while (avoiding(startOrientation) && !navigation.interrupted){
			processUSDistance();
		}
		navigation.setSpeeds(0, 0);
		
		return navigation.calculateDistance(x_init, y_init) > initialDistanceFromDestination;
	}
	
	/**
	 * A convenience for Navigation. Returns the distance read by the ultrasonic sensor 
	 * when the sensor is facing forward.
	 * @return
	 */
	public static int look(){
		return look(FORWARD);
	}
	
	//Returns the distance read by the ultrasonic sensor when the sensor is facing the specified direction.
	private static int look(int direction){
		switch(direction){
		case LEFT:
			Motors.getMotor(Motors.SENSOR).rotateTo(-SENSOR_TURN_ANGLE,false);
			return getDistance();
		case RIGHT:
			Motors.getMotor(Motors.SENSOR).rotateTo(SENSOR_TURN_ANGLE,false);
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
