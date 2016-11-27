package dpm.finalization;

import dpm.repository.Repository;
import dpm.util.DPMConstants;

/**
 * This class holds the set of tasks the robot must execute
 * towards the end of a round.
 * 
 * @author Samuel Genois
 *
 */
public class Finalization implements DPMConstants{
	
	private static final double FINAL_POSITON_OFFSET = 10;
	
	/**
	 * Constructor
	 */
	public Finalization(){}
	
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
		
		Repository.interruptBlockSearch();
		Repository.interruptNavigation();
		if(!Repository.clawIsEmpty()){
			Repository.travelToDest();
			Repository.drop();
		}
		
		switch(Repository.getStartZone()){
			case LOWER_LEFT:
				Repository.travelTo(-FINAL_POSITON_OFFSET, -FINAL_POSITON_OFFSET, AVOID_ALL);
			case LOWER_RIGHT:
				Repository.travelTo(10*SQUARE_SIZE + FINAL_POSITON_OFFSET, -FINAL_POSITON_OFFSET, AVOID_ALL);
			case UPPER_LEFT:
				Repository.travelTo(-FINAL_POSITON_OFFSET, 10*SQUARE_SIZE + FINAL_POSITON_OFFSET, AVOID_ALL);
			case UPPER_RIGHT:
				Repository.travelTo(10*SQUARE_SIZE + FINAL_POSITON_OFFSET, 10*SQUARE_SIZE + FINAL_POSITON_OFFSET, AVOID_ALL);
			default:
		}
		System.exit(0);
	}

}