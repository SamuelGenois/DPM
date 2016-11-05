package dpm.claw;

import dpm.util.Motors;
import lejos.robotics.RegulatedMotor;

public class Claw {
	
	RegulatedMotor	lowerclawMotor;
	RegulatedMotor	upperclawMotor;
	RegulatedMotor	liftMotor;
	
	/**
	 * The claw of the robot. Can grab and drop stacks of styrofoam blocks.
	 * Grabbed blocks are kept at least a block's height above floor level.
	 */
	public Claw(){
		lowerclawMotor = Motors.getMotor(Motors.LOWER_CLAW);
		upperclawMotor = Motors.getMotor(Motors.UPPER_CLAW);
		liftMotor = Motors.getMotor(Motors.LIFT);
	}
	
	/**
	 * Executes the following sequence of actions to drop the currently held blocks
	 *  - open lower claw
	 *  - lower upper claw
	 *  - open upper claw
	 *  - move backward until blocks are no longer inside claws
	 *  - close upper claw
	 *  - raise upper claw
	 *  - close lower claw
	 */
	public void drop(){
		//TODO
	}
	
	/**
	 * Executes the following sequence of actions to grab a new block:
	 *  - open lower claw
	 *  - move forward until new block is inside lower claw
	 *  - close lower claw
	 *  - open upper claw
	 *  - lower upper claw
	 *  - close upper claw
	 *  - open lower claw
	 *  - raise upper claw
	 *  - close lower claw
	 */
	public void grab(){
		//TODO
	}

}
