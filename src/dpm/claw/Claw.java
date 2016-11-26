package dpm.claw;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Motors;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

/**
* The claw of the robot. Can grab and drop stacks of styrofoam blocks.
* <br>Grabbed blocks are kept at least a block's height above floor level.
* @author Will Liang
*/
public class Claw implements DPMConstants{
    private final int CAPACITY = 4;
    private final RegulatedMotor    gripclawMotor;
    private final RegulatedMotor    liftMotor;
    private int gripStrength = -450;
    private int height = -170;
    private static int blockCount = 0; //number of block currently on robot
    
    /**
     * Constructor
     */
    public Claw(){
        gripclawMotor = Motors.getMotor(Motors.LOWER_CLAW);
        liftMotor = Motors.getMotor(Motors.LIFT);
    }
    
    /**
     * Executes the following sequence of actions to acquire new block:
     * <br>
     * <br> Grab n Carry:
     * <br> - open the grip claw (slightly)
     * <br> - lower the grip claw
     * <br> - close grip claw
     * <br> - lift the grip claw to height x
     */
    
    public void grab(){
		if(blockCount == 0){
			gripclawMotor.rotate(gripStrength);
			Delay.msDelay(10);
			liftMotor.rotate(height*3);
			
		}else if(blockCount < CAPACITY){
			gripclawMotor.rotate(gripStrength);
			Delay.msDelay(10);
			liftMotor.rotate(height*3);
			
		}
		blockCount++;
	}
	
    /**
	 *  Executes the following sequence of actions to acquire new block:
 	 * <br>Drop n Stack:
     * <br> - lower the grip claw
     * <br> - open the grip claw
     * <br> - low the grip claw to ground level
     */
    
	public void drop(){
		//alignment
		Repository.turnTo(Repository.getAng()+15);
		
		if(blockCount == 0){
			gripclawMotor.rotate(-gripStrength);
			liftMotor.rotate(-height*3);
			
		}else if(blockCount < CAPACITY && blockCount != 0){
			liftMotor.rotate(-height*4/3);
			gripclawMotor.rotate(-gripStrength);
			liftMotor.rotate(-height*3/2);
			
		}else{ //when it's fully loaded with 3 blocks and ready to drop it in the GZ.
			liftMotor.rotate(-height*5/3);
			gripclawMotor.rotate(-gripStrength);
			blockCount = 0;
		}
	}

    
    /**
     * Resets the claw back to the initial position
     */
    public void reset(){
    	 liftMotor.rotate(height*3);
    	 gripclawMotor.rotate(gripStrength);
    }
    
    /**
     * Returns true if the claw contains no blocks
     * @return True if the claw contains no blocks, false otherwise
     */
	public boolean clawIsEmpty() {
		return blockCount == 0;
	}
	
	/**
	 * Returns true if the claw is at full capacity
	 * @return True if the claw is at full capacity, false otherwise
	 */
	public boolean clawIsFull() {
		return blockCount == CAPACITY;
	}
    
    

}