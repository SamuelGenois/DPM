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
    private static final int	CAPACITY = 2,
    							GRIP_STRENGTH = -450,
    							HEIGHT = -200;
    
    private final RegulatedMotor    gripclawMotor;
    private final RegulatedMotor    liftMotor;
    
    private int blockCount = 0; //number of block currently on robot
    
    
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
            gripclawMotor.rotate(GRIP_STRENGTH);
            Delay.msDelay(10);
            liftMotor.rotate(HEIGHT*3);
            
        }else if(blockCount == 1){
            gripclawMotor.rotate(GRIP_STRENGTH);
            Delay.msDelay(10);
            liftMotor.rotate(HEIGHT*2);
            
        }else if(blockCount == 2){
            gripclawMotor.rotate(GRIP_STRENGTH);
            Delay.msDelay(10);
            liftMotor.rotate(HEIGHT);
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
            gripclawMotor.rotate(-GRIP_STRENGTH);
            liftMotor.rotate(-HEIGHT*3);
            
        }else if(blockCount == 1){
            liftMotor.rotate(-HEIGHT*2);
            gripclawMotor.rotate(-GRIP_STRENGTH);
            liftMotor.rotate(-HEIGHT*1);
            
        }else if(blockCount == 2){
            liftMotor.rotate(-HEIGHT);
            gripclawMotor.rotate(-GRIP_STRENGTH);
            liftMotor.rotate(-HEIGHT);
            
        }else if(blockCount == 3){ //when it's fully loaded with 3 blocks and ready to drop it in the GZ.
            gripclawMotor.rotate(-GRIP_STRENGTH);
            blockCount = 0;
        }
        reset();
    }
    
    /*
     * Resets the claw back to the initial position
     */
    private void reset(){
    	 liftMotor.rotate(HEIGHT*3);
    	 gripclawMotor.rotate(GRIP_STRENGTH);
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