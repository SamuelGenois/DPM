package dpm.util;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;

/**
 * This class centralizes access to the motors actuating the
 * wheels of the robot. It provides methods to make the wheels
 * turn at a specific speed, rotate a specific angle, etc.
 * This class is a singleton.
 * 
 * @author Samuel Genois
 */
public class Motors{
	
	public static final long ONE_SECOND = 1000;
	
	public static final int LEFT = 0,
							RIGHT = LEFT+1,
							LOWER_PINCER = RIGHT+1,
							UPPER_PINCER = LOWER_PINCER+1,
							LIFT = UPPER_PINCER+1;
	
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 15.8;
	
	private static RegulatedMotor	leftMotor,
									rightMotor,
									lowerPincerMotor,
									upperPincerMotor,
									liftMotor;
	
	public static RegulatedMotor getMotor(int id){
		switch(id){
			case LEFT:
				if(leftMotor == null)
					leftMotor = new Motor(RemoteBrickManager.MASTER, 'L', "A");
				return leftMotor;
			case RIGHT:
				if(rightMotor == null)
					rightMotor = new Motor(RemoteBrickManager.MASTER, 'L', "D");
				return rightMotor;
			case LOWER_PINCER:
				if(lowerPincerMotor == null)
					lowerPincerMotor = new Motor(RemoteBrickManager.SLAVE, 'M', "A");
				return lowerPincerMotor;
			case UPPER_PINCER:
				if(upperPincerMotor == null)
					upperPincerMotor = new Motor(RemoteBrickManager.SLAVE, 'M', "B");
				return upperPincerMotor;
			case LIFT:
				if(liftMotor == null)
					liftMotor = new Motor(RemoteBrickManager.SLAVE, 'L', "C");
				return liftMotor;
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
				motor.setSpeed(Math.abs(speed));
				forward();
			}
			else {
				motor.setSpeed(Math.abs(speed));
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
			motor.forward();	
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