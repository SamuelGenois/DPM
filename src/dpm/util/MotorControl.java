package dpm.util;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class centralizes access to the motors actuating the
 * wheels of the robot. It provides methods to make the wheels
 * turn at a specific speed, rotate a specific angle, etc.
 * This class is a singleton.
 * 
 * @author Samuel Genois
 */
public class MotorControl{
	
	public static final long ONE_SECOND = 1000;
	
	public static final int L_MOTOR = 0,
							R_MOTOR = 1,
							BOTH_MOTORS = 2;
	
	private static final int DEFAULT_MOTOR_BASE_SPEED = 200,
							 DEFAULT_ROTATE_SPEED = 150;
	
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 15.3;
	
	private static MotorControl theInstance = null;
	
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	/*
	 * Private contructor
	 */
	private MotorControl(){
		this.leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		this.rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	}
	
	/**
	 * Gets the instance of MotorControl. If the instance is not initialized,
	 * initializes it.
	 * 
	 * @return the instance of MotorControl
	 */
	public static synchronized MotorControl getInstance(){
		if(theInstance == null)
			theInstance = new MotorControl();
		return theInstance;
	}
	
	/**
	 * Returns a reference to the left motor
	 * 
	 * @return a reference to the left motor
	 */
	public EV3LargeRegulatedMotor getLeftMotor(){
		return leftMotor;
	}
	
	/**
	 * Returns a reference to the right motor.
	 * 
	 * @return a reference to the right motor
	 */
	public EV3LargeRegulatedMotor getRightMotor(){
		return rightMotor;
	}
	
	/**
	 * Rotates both motors at the specified angle at default speed.
	 * 
	 * @param lMotorAngle The angle of rotation of the left motor
	 * @param rMotorAngle The angle of rotation of the left motor
	 */
	public synchronized void rotateMotorsSetAngle(int lMotorAngle, int rMotorAngle){
		
		this.leftMotor.setSpeed(DEFAULT_ROTATE_SPEED);
		this.leftMotor.setSpeed(DEFAULT_ROTATE_SPEED);
		this.leftMotor.rotate(lMotorAngle, true);
		this.rightMotor.rotate(rMotorAngle, false);
	}
	
	/**
	 * Sets the speed of the motors
	 * 
	 * @param motorSpeeds		The speeds the motors are to be set
	 * @param motorsToUpdate	Integer specifing which motors should have their speed changed.
	 */
	public synchronized void setMotorAbsoluteSpeeds(int[] motorSpeeds, int motorsToUpdate){
		
		switch(motorsToUpdate){
		case L_MOTOR:		leftMotor.setSpeed(Math.abs(motorSpeeds[0]));
							if(motorSpeeds[0]>=0)
								leftMotor.forward();
							else
								leftMotor.backward();
							break;
		case R_MOTOR:		rightMotor.setSpeed(Math.abs(motorSpeeds[1]));
							if(motorSpeeds[1]>=0)
								rightMotor.forward();
							else
								rightMotor.backward();
							break;
		case BOTH_MOTORS:	leftMotor.setSpeed(Math.abs(motorSpeeds[0]));
							if(motorSpeeds[0]>=0)
								leftMotor.forward();
							else
								leftMotor.backward();
							rightMotor.setSpeed(Math.abs(motorSpeeds[1]));
							if(motorSpeeds[1]>=0)
								rightMotor.forward();
							else
								rightMotor.backward();
							break;
		default:		break;
		}
	}
	
	/**
	 * Sets the speeds of the motors relative to a default speed value.
	 * 
	 * @param motorSpeeds		The relative speeds the motors to be set.
	 * @param motorsToUpdate	Integer specifying which motors should have their speed changed.
	 */
	public synchronized void setMotorRelativeSpeeds(double[] motorSpeeds, int motorsToUpdate){
		setMotorRelativeSpeeds(motorSpeeds, motorsToUpdate, DEFAULT_MOTOR_BASE_SPEED);
	}
	
	/**
	 * Sets the speeds of the motors relative to a base speed value.
	 * 
	 * @param motorSpeeds		The relative speeds the motors to be set.
	 * @param motorsToUpdate	Integer specifying which motors should have their speed changed.
	 * @param baseMotorSpeed	The reference speed to which the new motor speeds relate.
	 */
	public void setMotorRelativeSpeeds(double[] motorSpeeds, int motorsToUpdate, int baseMotorSpeed){
		switch(motorsToUpdate){
		case L_MOTOR:		setMotorAbsoluteSpeeds(new int[]{(int)(baseMotorSpeed * motorSpeeds[0]), 0}, L_MOTOR);
							break;
		case R_MOTOR:		setMotorAbsoluteSpeeds(new int[]{0,(int)(baseMotorSpeed * motorSpeeds[1])}, R_MOTOR);
							break;
		case BOTH_MOTORS:	setMotorAbsoluteSpeeds(new int[]{(int)(baseMotorSpeed * motorSpeeds[0]),
															(int)(baseMotorSpeed * motorSpeeds[1])}, BOTH_MOTORS);
							break;
		default:		break;
		}
	}
	
	/**
	 * Stops both motors.
	 */
	public void stopMotors(){
		setMotorAbsoluteSpeeds(new int[] {0,0}, BOTH_MOTORS);
	}
}