package dpm.util;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;

/**
 * A utility class that provides references to the robot's motor's interfaces.
 * The initialization of those interfaces is done internally.
 *
 * @author Samuel Genois
 */
public class Motors{
	
	public static final long ONE_SECOND = 1000;
	
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
	
	/**
	 * The id of the motor operating the lifting of the upper claw
	 */
	public static final int LIFT = 4;
	
	/**
	 * The id of the motor operating the motion of the
	 * dynamic ultrasonic sensor
	 */
	public static final int SENSOR = 5;
	
	/**
	 * The radius of the robot's wheels
	 */
	public static final double WHEEL_RADIUS = 2.1;
	
	/**
	 * The distance between the robot's left and right wheels.
	 */
	public static final double TRACK = 15.8;
	
	private static RegulatedMotor[] motors = new RegulatedMotor[6];
	
	/**
	 * Returns a reference to the motor corresponding to the
	 * provided id. If the motor (interface) is yet not initialized,
	 * initializes it.
	 * @param id the id of the desired motor reference
	 * @return the motor reference
	 */
	public static RegulatedMotor getMotor(int id){
		switch(id){
			case LEFT:
				if(motors[LEFT] == null)
					motors[LEFT] = new Motor(RemoteBrickManager.MASTER, 'L', "A");
				return motors[LEFT];
			case RIGHT:
				if(motors[RIGHT] == null)
					motors[RIGHT] = new Motor(RemoteBrickManager.MASTER, 'L', "D");
				return motors[RIGHT];
			case LOWER_CLAW:
				if(motors[LOWER_CLAW] == null)
					motors[LOWER_CLAW] = new Motor(RemoteBrickManager.SLAVE, 'M', "A");
				return motors[LOWER_CLAW];
			case UPPER_CLAW:
				if(motors[UPPER_CLAW] == null)
					motors[UPPER_CLAW] = new Motor(RemoteBrickManager.SLAVE, 'M', "B");
				return motors[UPPER_CLAW];
			case LIFT:
				if(motors[LIFT] == null)
					motors[LIFT] = new Motor(RemoteBrickManager.SLAVE, 'L', "C");
				return motors[LIFT];
			case SENSOR:
				if(motors[SENSOR] == null)
					motors[SENSOR] = new Motor(RemoteBrickManager.MASTER, 'L', "B");
				return motors[SENSOR];
			default:	
				return null;
		}
	}
	
	
	/**
	 * A decorator design pattern class that effectively synchronizes
	 * all of the method calls used in this project to operate a motor.();
	 * 
	 * @author Samuel
	 */
	private static class Motor implements RegulatedMotor{
		
		private RegulatedMotor motor;
		
		private Motor(int brick, char type, String portName){
			switch(brick){
			case RemoteBrickManager.MASTER:
				switch(type){
					case 'L':
						motor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort(portName));
						break;
					case 'M':
						motor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort(portName));
						break;
					default:
						motor = null;
				}
				break;
			case RemoteBrickManager.SLAVE:
				motor = RemoteBrickManager.getSlave().createRegulatedMotor(portName, type);
				break;
			default:	
				motor = null;
			}
		}
		
		@Override
		public void setSpeed(int speed){
			if(speed >= 0){
				motor.setSpeed(speed);
				forward();
			}
			else {
				motor.setSpeed(-speed);
				backward();
			}
		}
		
		@Override
		public void stop(){
			motor.stop();
		}
		
		@Override
		public void rotate(int angle, boolean immediateReturn){
			motor.rotate(angle, immediateReturn);
		}
		
		@Override
		public void rotateTo(int angle, boolean immediateReturn){
			motor.rotateTo(angle, immediateReturn);
		}


		@Override
		public void forward() {
			motor.forward();
		}


		@Override
		public void backward() {
			motor.backward();	
		}


		@Override
		public void flt() {
			motor.flt();
		}


		@Override
		public boolean isMoving() {
			return motor.isMoving();
		}


		@Override
		public int getRotationSpeed() {
			return motor.getRotationSpeed();
		}


		@Override
		public int getTachoCount() {
			return motor.getTachoCount();
		}


		@Override
		public void resetTachoCount() {
			motor.resetTachoCount();
		}


		@Override
		public void addListener(RegulatedMotorListener listener) {
			motor.addListener(listener);
		}


		@Override
		public RegulatedMotorListener removeListener() {
			return motor.removeListener();
		}


		@Override
		public void stop(boolean immediateReturn) {
			motor.stop(immediateReturn);
			
		}


		@Override
		public void flt(boolean immediateReturn) {
			motor.flt(immediateReturn);
			
		}


		@Override
		public void waitComplete() {
			motor.waitComplete();
			
		}


		@Override
		public void rotate(int angle) {
			motor.rotate(angle);
			
		}


		@Override
		public void rotateTo(int limitAngle) {
			motor.rotateTo(limitAngle);
			
		}


		@Override
		public int getLimitAngle() {
			return motor.getLimitAngle();
		}


		@Override
		public int getSpeed() {
			return motor.getSpeed();
		}


		@Override
		public float getMaxSpeed() {
			return motor.getMaxSpeed();
		}


		@Override
		public boolean isStalled() {
			motor.isStalled();
			return false;
		}


		@Override
		public void setStallThreshold(int error, int time) {
			motor.setStallThreshold(error, time);
			
		}


		@Override
		public void setAcceleration(int acceleration) {
			motor.setAcceleration(acceleration);
			
		}


		@Override
		public void synchronizeWith(RegulatedMotor[] syncList) {
			motor.synchronizeWith(syncList);
			
		}


		@Override
		public void startSynchronization() {
			motor.startSynchronization();
			
		}


		@Override
		public void endSynchronization() {
			motor.endSynchronization();
			
		}


		@Override
		public void close() {
			motor.close();
			
		}
	}
}