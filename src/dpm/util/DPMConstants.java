package dpm.util;

/**
 * Collection of constants used by multiple subsystems
 * 
 * @author Samuel
 */
public interface DPMConstants {
	
	public static final double SQUARE_SIZE = 30.48;
	
	//Roles
	public static final int BUILDER = 0;
	
	public static final int GARBAGE_COLLECTOR = 1;
	
	//Starting Zones
	public static final int LOWER_LEFT = 1;
	
	public static final int LOWER_RIGHT = 2;
	
	public static final int UPPER_LEFT = 4;
	
	public static final int UPPER_RIGHT = 3;
	
	//Motors
	/**
	 * The id of the motor operating the left wheel
	 */
	public static final int LEFT = 0;
	
	/**
	 * The id of the motor operating the right wheel
	 */
	public static final int RIGHT = 1;
	
	/**
	 * The id of the motor operating the lower claw
	 */
	public static final int LOWER_CLAW = 2;
	
	/**
	 * The id of the motor operating the upper claw
	 */
	public static final int UPPER_CLAW = 3;
	
	
	//Sensors
	/**
	 * The id of the ultrasonic sensor used to detected
	 * distance in front of the robot
	 */
	public static final int	US_ACTIVE = 0;
	
	/**
	 * The id of the ultrasonic sensor used to detect
	 * ultrasonic interference
	 */
	public static final int	US_PASSIVE = 1;
	
	/**
	 * The id of the color sensor used to identify blue
	 * styrofoam blocks
	 */
	public static final int	COLOR_BLOCK_ID = 2;
	
	/**
	 * The id of the color sensor used to detect grid lines
	 */
	public static final int	COLOR_ODO_CORR = 3;
}
