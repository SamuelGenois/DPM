package dpm.navigation;

import dpm.odometry.Odometer;
import dpm.util.Interruptable;
import dpm.util.Motors;
import lejos.robotics.RegulatedMotor;

/**
 * A class containing methods related to navigation.
 * 
 * @author Samuel Genois
 */
public class Navigation implements Interruptable{

	public static final double ANGLE_ERROR_TOLERANCE = (Math.PI/96.0);
	public static final double DISTANCE_ERROR_TOLERANCE = 2.0;
	
	public static final long	RESONABLY_SHORT_PERIOD_OF_TIME = 50l,
								ONE_SECOND = 1000l;
	
	private Odometer odometer;
	private RegulatedMotor leftMotor, rightMotor;
	private boolean isNavigating, isTurning;
	private double[] position;
	
	/**
	 * Constructor.
	 * 
	 * @param odometer		The robot's odometer
	 * @param usSensor		The robot's ultrasonic sensor
	 * @param sensorMotor	The motor that rotates the ultrasonic sensor.
	 */
	public Navigation(Odometer odometer){
		this.odometer = odometer;
		this.isNavigating = false;
		this.leftMotor = Motors.getMotor(Motors.LEFT);
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
		this.position = new double[3];
	}
	
	/**
	 * Calculates the euclidean distance between point (x,y)
	 * and the robot's current location.
	 * 
	 * @param x	The x coordinate of the destination
	 * @param y	The y coordinate of the destination
	 * @return	The euclidean distance
	 */
	public double calculateDistance(double x, double y){
		odometer.getPosition(position, Odometer.ALL);
		return Math.sqrt(Math.pow(y-position[Odometer.Y], 2.0)+Math.pow(x-position[Odometer.X], 2.0));
	}
	
	/**
	 * Calculates the minimum angle the robot must turn
	 * to match his orientation with theta.
	 * 
	 * @param theta	The desired orientation.
	 * @return		The minimum turn angle.
	 */
	public double calculateMinTurnAngle(double theta){
		odometer.getPosition(position, Odometer.ALL);
		double minTurnAngle = theta-position[Odometer.THETA];
		
		if(minTurnAngle < -Math.PI)
			minTurnAngle += 2*Math.PI;
		if(minTurnAngle > Math.PI)
			minTurnAngle -= 2*Math.PI;
		
		//DebuggerPrinter.display(Double.toString(Math.toDegrees(minTurnAngle)), 2, false);
		return minTurnAngle;
	}
	
	/**
	 * Returns the angle of the vector going from the robot
	 * to its destination (counter clockwise stating from the y axis).
	 * 
	 * @param x	The x coordinate of the destination
	 * @param y	The y coordinate of the destination
	 * @return	The angle
	 */
	public double destinationAngle(double x, double y){
		odometer.getPosition(position, Odometer.ALL);
		return (Math.atan2(x-position[Odometer.X], y-position[Odometer.Y])%(2*Math.PI));
	}
	
	/**
	 * Returns true if the robot has reached its destination
	 * 
	 * @param x	The x coordinate of the destination
	 * @param y	The y coordinate of the destination
	 * @return	true if the robot has reached its destination
	 */
	private boolean hasReachedDestination(double x, double y){
		return calculateDistance(x, y) < DISTANCE_ERROR_TOLERANCE;
	}
	
	/**
	 * Returns true if either the travelTo of turnTo methods
	 * are currently being executed.
	 * 
	 * @return
	 */
	public boolean isNavigating(){
		return (isNavigating || isTurning);
	}
	
	/**
	 * Returns true if either the  turnTo method
	 * is currently being executed.
	 * 
	 * @return
	 */
	public boolean isTurning(){
		return isTurning;
	}
	
	/**
	 * Returns the odometer used by this navigation instance.
	 * 
	 * @return the odometer used by this navigation instance
	 */
	public Odometer getOdometer(){
		return odometer;
	}
	
	/**
	 * Makes the robot travel forward for a set distance
	 * 
	 * @param distance the robot travels
	 */
	public void goForward(double distance) {
		odometer.getPosition(position, Odometer.ALL);
		this.travelTo(Math.sin(position[Odometer.THETA]) * distance, Math.cos(position[Odometer.THETA]) * distance);

	}
	
	/**
	 * Interrupts currently running travelTo or turnTo methods.
	 */
	@Override
	public void interrupt(){
		//TODO
	}
	
	/**
	 * Makes the robot travel to the point (x,y), avoiding
	 * obstacles along the way.
	 * 
	 * @param x
	 * @param y
	 */
	public void travelTo(double x, double y){
		
		isNavigating = true;
		
		while(!hasReachedDestination(x, y)){
			
			/*
			 * If the robot is not facing its destination, it rotates
			 * to do so.
			 */
			double theta = destinationAngle(x, y);
			if(Math.abs(calculateMinTurnAngle(theta)) > ANGLE_ERROR_TOLERANCE)
				turnTo(theta);
			
			/*
			 * This prevents the robot from overshooting if it has already
			 * reached its destination.
			 */
			if(hasReachedDestination(x, y))
				break;
			
			/*
			 * This pause ensures that the robot is fully immobilized before
			 * proceeding. It also gives obstacleAvoidance time to detect obstacles.
			 */
			try {
				Thread.sleep(ONE_SECOND / 2);
			} catch (InterruptedException e) {}
			
			/*
			 * The robot moves forward for a short amount of time
			 */
			setSpeeds(150, 150);
			try {
				Thread.sleep(RESONABLY_SHORT_PERIOD_OF_TIME);
			} catch (InterruptedException e) {}
		}
		
		/*
		 * Stop both the motors
		 */
		stopMotors();
		
		isNavigating = false;
	}
	
	/**
	 * Rotates the robot to angle theta. The angle is in radians.
	 * Positive angles are counter clockwise from the positive y axis.
	 * Negative angles are counter clockwise form the positive y axis
	 * 
	 * @param theta	The angle to with the robot must rotate
	 */
	public void turnTo(double theta){
		isTurning = true;
		
		double turnAngle = calculateMinTurnAngle(theta);
		
		rotateMotorsSetAngle(convertAngle(Motors.WHEEL_RADIUS, Motors.TRACK, Math.toDegrees(turnAngle)),
				-convertAngle(Motors.WHEEL_RADIUS, Motors.TRACK, Math.toDegrees(turnAngle)));
		
		stopMotors();
		
		isTurning = false;
	}
	
	private void rotateMotorsSetAngle(int leftMotorRotationAngle, int rightMotorRotationAngle){
		leftMotor.rotate(leftMotorRotationAngle, true);
		rightMotor.rotate(rightMotorRotationAngle, false);
	}
	
	private void setSpeeds(int leftMotorSpeed, int rightMotorSpeed){
		leftMotor.setSpeed(leftMotorSpeed);
		rightMotor.setSpeed(rightMotorSpeed);
	}
	
	private void stopMotors(){
		leftMotor.stop();
		rightMotor.stop();
	}
	
	/*
	 * The following two methods have been taken from Lab 2's SquareDriver class.
	 */
	private static int convertAngle(double wheelRadius, double track, double angle) {
		return convertDistance(wheelRadius, Math.PI * track * angle / 360.0);
	}
	
	private static int convertDistance(double wheelRadius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * wheelRadius));
	}
}
