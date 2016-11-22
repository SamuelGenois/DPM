package dpm.claw;

import dpm.util.Motors;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

/**
* The claw of the robot. Can grab and drop stacks of styrofoam blocks.
* <br>Grabbed blocks are kept at least a block's height above floor level.
* @author Will Liang
*/
public class Claw {
    private final int CAPACITY = 2;
    private final RegulatedMotor    gripclawMotor, alignclawMotor;
    private final RegulatedMotor    liftMotor;
    private int gripStrength = -450;
    private int alignment = 300;
    private int height = -200;
    private static int blockCount = 0; //number of block currently on robot
    
    
    /**
     * Constructor
     */
    public Claw(){
        
        gripclawMotor = Motors.getMotor(Motors.LOWER_CLAW);
        alignclawMotor = Motors.getMotor(Motors.UPPER_CLAW);
        liftMotor = Motors.getMotor(Motors.LIFT);
    }
    
    /**
     * Executes the following sequence of actions to acquire new block:
     * <br>
     * <br> Alignment:
     * <br> - close align claw
     * <br> - open align claw
     * <br> 
     * <br> Grab n Carry:
     * <br> - open the grip claw (slightly)
     * <br> - lower the grip claw
     * <br> - close grip claw
     * <br> - lift the grip claw to height x
     */
    
    public void grab(){
//        //do the alignment by closing the claw
//        alignclawMotor.rotate(-alignment);
//        //release/open the claw
//        alignclawMotor.rotate(alignment+100);
        
        if(blockCount == 0){
            gripclawMotor.rotate(gripStrength);
            Delay.msDelay(10);
            liftMotor.rotate(height*3);
            
        }else if(blockCount == 1){
            gripclawMotor.rotate(gripStrength);
            Delay.msDelay(10);
            liftMotor.rotate(height*2);
            
        }else if(blockCount == 2){
            gripclawMotor.rotate(gripStrength);
            Delay.msDelay(10);
            liftMotor.rotate(height);
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
        if(blockCount == 0){
            gripclawMotor.rotate(-gripStrength);
            liftMotor.rotate(-height*3);
            
        }else if(blockCount == 1){
            liftMotor.rotate(-height*2);
            gripclawMotor.rotate(-gripStrength);
            liftMotor.rotate(-height*1);
            
        }else if(blockCount == 2){
            liftMotor.rotate(-height);
            gripclawMotor.rotate(-gripStrength);
            liftMotor.rotate(-height);
            
        }else if(blockCount == 3){ //when it's fully loaded with 3 blocks and ready to drop it in the GZ.
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