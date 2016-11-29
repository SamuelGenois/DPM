package dpm.navigation;

import java.util.Timer;
import java.util.TimerTask;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Motors;
import dpm.util.Printer;
import dpm.util.Sensors;
import lejos.hardware.Sound;

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
	
	private boolean timedOut;
	
	private int direction;
	private double x_init, y_init, a, b, initialDistanceFromDestination;
	private double[] badZone;
	private boolean startedAvoidance;
	
	/**
	 * Constructor
	 * 
	 * @param navigation the Navigation object using this ObstacleAvoidance
	 * @param x_fin	The x coordinate of the destination
	 * @param y_fin	The y coordinate of the destination
	 */
	public ObstacleAvoidance(Navigation navigation, double x_fin, double y_fin, double[] badZone) {
		this.navigation = navigation;
		this.x_init = this.navigation.getPosition()[0];
		this.y_init = this.navigation.getPosition()[1];
		this.a = (x_fin-x_init)/Math.sqrt((x_fin-x_init)*(x_fin-x_init)+(y_fin-y_init)*(y_fin-y_init));
		this.b = (y_fin-y_init)/Math.sqrt((x_fin-x_init)*(x_fin-x_init)+(y_fin-y_init)*(y_fin-y_init));
		this.badZone = badZone;
		this.initialDistanceFromDestination = navigation.calculateDistance(x_fin, y_fin);
		this.startedAvoidance = false;
	}
	
	/*
	 * Calculates the error (deviation of robot from its initial path)
	 */
	private double calculateError(){
		if (a < 0.1){
			return Math.abs(navigation.getPosition()[0]-x_init)/b;
		}
		if (b < 0.1){
			return Math.abs(navigation.getPosition()[1]-y_init)/a;
		}
		return Math.abs((navigation.getPosition()[0]-x_init)/a-(navigation.getPosition()[1]-y_init)/b);
	}
	
	/*
	 * Returns true as long as the robot is not done avoiding the obstacle
	 */
	private boolean avoiding(){
		Printer.getInstance().display(""+calculateError());
		if (calculateError() > 8.0){
			startedAvoidance = true;
		}
		
		if(startedAvoidance && calculateError() < 4.0){
			return false;
		}
		return true;
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
		
		timedOut = false;
		
		(new Timer()).schedule(new ObstacleAvoidanceTimer(this), 20000l);
		
		while (avoiding() && !navigation.interrupted && !timedOut){
			processUSDistance();
		}
		navigation.setSpeeds(0, 0);
		
		return navigation.calculateDistance(x_init, y_init) < initialDistanceFromDestination;
	}
	
	/**
	 * A convenience for Navigation. Returns the distance read by the ultrasonic sensor 
	 * when the sensor is facing forward.
	 * @return
	 */
	public int look(){
		return look(FORWARD);
	}
	
	//Returns the distance read by the ultrasonic sensor when the sensor is facing the specified direction.
	private int look(int direction){
		switch(direction){
		case LEFT:
			Motors.getMotor(Motors.SENSOR).rotateTo(-SENSOR_TURN_ANGLE);
			return getDistance(direction);
		case RIGHT:
			Motors.getMotor(Motors.SENSOR).rotateTo(SENSOR_TURN_ANGLE);
			return getDistance(direction);
		case FORWARD:
		default:
			Motors.getMotor(Motors.SENSOR).rotateTo(0);
			return getDistance(direction);
		}
	}
	
	//Returns the distance read by the ultrasonic sensor.
	private int getDistance(int direction){
		float[] data = new float[Sensors.getSensor(Sensors.US_ACTIVE).sampleSize()];
		Sensors.getSensor(Sensors.US_ACTIVE).fetchSample(data, 0);
		int distance = (int)(data[0]*100);
		
		//Code to treat RedZone as a physical obstacle. Does not work.
		/*
		int distanceFromEdge = distance;
		
		double	x = navigation.getPosition()[0],
				y = navigation.getPosition()[1],
				angle = navigation.getPosition()[2];
		
		if(direction == LEFT){
			angle += SENSOR_TURN_ANGLE;
			if(angle >=360)
				angle -= 360; 
		}
		if(direction == RIGHT){
			angle -= SENSOR_TURN_ANGLE;
			if(angle < 0)
				angle += 360; 
		}
		
		//If the robot is facing right and the left side of the bad zone is at the robot's right
		if((angle<90 || angle>270) && badZone[0]-x >= 0){
			//Calculate the y value of the intersection of the robot's direction and the vertical line
			double yIntercept = Math.tan(Math.toRadians(angle))*(badZone[0]-x)+y;
			//If the intersection point is within the left edge of the badZone
			if(yIntercept <= badZone[1] && yIntercept >= badZone[3])
				//Calculate the distance from the left badZone as seen by the us sensor
				distanceFromEdge = Math.min(distanceFromEdge, (int)navigation.calculateDistance(badZone[0], yIntercept));
		}
		
		//If the robot is facing up and the bottom side of the bad zone is above the robot 
		if((angle<180 && angle>0) && badZone[3]-y >= 0){
			//Calculate the x value of the intersection of the robot's direction and the horizontal line
			double xIntercept = (badZone[3]-y)/Math.tan(Math.toRadians(angle))+x;
			//If the intersection point is within the bottom edge of the badZone
			if(xIntercept <= badZone[2] && xIntercept >= badZone[0])
				//Calculate the distance from the left badZone as seen by the us sensor
				distanceFromEdge = Math.min(distanceFromEdge, (int)navigation.calculateDistance(xIntercept, badZone[3]));
		}
		
		//If the robot is facing left and the right side of the bad zone is at the robot's left
		if((angle<270 && angle>90) && badZone[2]-x <= 0){
			//Calculate the y value of the intersection of the robot's direction and the vertical line
			double yIntercept = Math.tan(Math.toRadians(angle))*(badZone[2]-x)+y;
			//If the intersection point is within the right edge of the badZone
			if(yIntercept <= badZone[1] && yIntercept >= badZone[3])
				//Calculate the distance from the left badZone as seen by the us sensor
				distanceFromEdge = Math.min(distanceFromEdge, (int)navigation.calculateDistance(badZone[2], yIntercept));
		}
		
		//If the robot is facing down and the top side of the bad zone is below the robots 
		if((angle<360 && angle>180) && badZone[1]-y <= 0){
			//Calculate the x value of the intersection of the robot's direction and the horizontal line
			double xIntercept = (badZone[1]-y)/Math.tan(Math.toRadians(angle))+x;
			//If the intersection point is within the top edge of the badZone
			if(xIntercept <= badZone[2] && xIntercept >= badZone[0])
				//Calculate the distance from the left badZone as seen by the us sensor
				distanceFromEdge = Math.min(distanceFromEdge, (int)navigation.calculateDistance(xIntercept, badZone[1]));
		}
		
		//If that calculated distance is lesser than the current output, set the current output to the calculated distance
		if(distanceFromEdge < distance){
			distance = distanceFromEdge;
		}
		
		System.out.println(distance);
		*/
		
		return distance;
	}
	
	private class ObstacleAvoidanceTimer extends TimerTask{

		private final ObstacleAvoidance obstacleAvoidance;
		
		public ObstacleAvoidanceTimer(ObstacleAvoidance obstacleAvoidance){
			this.obstacleAvoidance = obstacleAvoidance;
		}
		
		@Override
		public void run() {
			obstacleAvoidance.timedOut = true;
		}
	}
}
