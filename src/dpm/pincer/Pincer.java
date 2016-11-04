package dpm.pincer;

import dpm.util.Motors;
import lejos.robotics.RegulatedMotor;

public class Pincer {
	
	RegulatedMotor	lowerPincerMotor;
	RegulatedMotor	upperPincerMotor;
	RegulatedMotor	liftMotor;
	
	/**
	 * The pincer of the robot. Can grab and drop stacks of styrofoam blocks.
	 * Grabbed blocks are kept at least a block's height above floor level.
	 */
	public Pincer(){
		lowerPincerMotor = Motors.getMotor(Motors.LOWER_PINCER);
		upperPincerMotor = Motors.getMotor(Motors.UPPER_PINCER);
		liftMotor = Motors.getMotor(Motors.LIFT);
	}
	
	/**
	 * Executes the following sequence of actions to drop the currently held blocks
	 *  - open lower pincer
	 *  - lower upper pincer
	 *  - open upper pincer
	 *  - move backward until blocks are no longer inside pincers
	 *  - close upper pincer
	 *  - raise upper pincer
	 *  - close lower pincer
	 */
	public void drop(){
		//TODO
	}
	
	/**
	 * Executes the following sequence of actions to grab a new block:
	 *  - open lower pincer
	 *  - move forward until new block is inside lower pincer
	 *  - close lower pincer
	 *  - open upper pincer
	 *  - lower upper pincer
	 *  - close upper pincer
	 *  - open lower pincer
	 *  - raise upper pincer
	 *  - close lower pincer
	 */
	public void grab(){
		//TODO
	}

}
