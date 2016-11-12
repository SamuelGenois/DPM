package dpm.finalization;

/**
 * This class holds the set of tasks the robot must execute
 * towards the end of a round.
 * 
 * @author Samuel Genois
 *
 */
public class Finalization{
	
	/**
	 * Constructor
	 */
	public Finalization(){}

	/**
	 * Executes the final tasks the robot must do
	 * before the end of the round.
	 * 
	 *  - Stop navigating
	 *  - Stop searching for blocks
	 *  - If the robot is currently holding one or more blocks:
	 *    -> navigate to the appropriate zone (green or red)
	 *    -> deposit the blocks
	 *  - Navigate to the starting corner
	 *  - Request the termination of the program
	 * 
	 */
	public void doFinalization(){
		//TODO
	}

}
