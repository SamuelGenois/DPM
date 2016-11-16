package dpm.finalization;

import dpm.repository.Repository;

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
	 * <br> So far: temporary version for beta demo (only travels to green zone after picking up one block)
	 */
	
	public void doFinalization(double targetX, double targetY){
		Repository.interruptBlockSearch();
		Repository.interruptNavigation();
		Repository.travelTo(targetX, targetY);
		Repository.turnTo(Repository.getAng()+180);
		Repository.drop();
	}
	
	/**
	 * Executes the final tasks the robot must do
	 * before the end of the round.
	 * <br>
	 * <br> - Stop navigating
	 * <br> - Stop searching for blocks
	 * <br> - If the robot is currently holding one or more blocks:
	 * <br> 	-> navigate to the appropriate zone (green or red)
	 * <br> 	-> deposit the blocks
	 * <br> - Navigate to the starting corner
	 * <br> - Request the termination of the program
	 * 
	 */
	public void doFinalization(){
		//TODO
	}

}
