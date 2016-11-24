package dpm.launcher;

/**
 * This interface defines the interaction between the class that launches the program
 * (the class that carries the main method and the information provided via wifi) and Repository 
 * @author Samuel Genois
 *
 */
public interface Launcher {
	
	public static int TEAM_NUMBER = 8;
	
	/**
	 * Returns the coordinates of the top left and bottom right corners of the green zone.
	 * @return the coordinates of the top left and bottom right corners of the green zone
	 */
	abstract public int[] getGreenZone();
	
	/**
	 * Returns the coordinates of the top left and bottom right corners of the green zone.
	 * @return the coordinates of the top left and bottom right corners of the green zone
	 */
	abstract public int[] getRedZone();
	
	/**
	 * Returns the starting corner of the robot.
	 * @return the starting corner of the robot
	 */
	abstract public int getStartZone();
	
	/**
	 * Returns the role of the robot.
	 * @return the role of the robot
	 */
	abstract public int getRole();
}