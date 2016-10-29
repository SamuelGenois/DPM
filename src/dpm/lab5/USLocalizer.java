package dpm.lab5;

import dpm.util.MotorControl;
import dpm.util.Sensors;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public static int	ROTATION_SPEED = 60,
						BIG_ANGLE_CORRECTION = 200,
						SMALL_ANGLE_CORRECTION = 70,
						WALL_DETECTION = 50;

	private Odometer odo;
	private SampleProvider usSensor;
	private MotorControl motorControl;
	private float[] usData;
	
	public USLocalizer(Odometer odo) {
		this.odo = odo;
		this.usSensor = Sensors.getInstance().getUSSensor();
		this.motorControl = MotorControl.getInstance();
		this.usData = new float[this.usSensor.sampleSize()];
	}
	
	public void doLocalization() {
		double angleA, angleB;
	
		/*
		 * The robot should turn until it sees the wall, then look for the
		 * "rising edges:" the points where it no longer sees the wall.
		 * This is very similar to the FALLING_EDGE routine, but the robot
		 * will face toward the wall for most of it.
		 */
		motorControl.setMotorAbsoluteSpeeds(new int[] {ROTATION_SPEED, -ROTATION_SPEED}, MotorControl.BOTH_MOTORS);
		while(!wallFound());
		while(wallFound());
		motorControl.stopMotors();
		angleB = odo.getAng();
		Sound.beep();
		
		motorControl.setMotorAbsoluteSpeeds(new int[] {-ROTATION_SPEED, ROTATION_SPEED}, MotorControl.BOTH_MOTORS);
		while(!wallFound());
		try {Thread.sleep(MotorControl.ONE_SECOND);} catch (InterruptedException e) {}
		while(wallFound());
		motorControl.stopMotors();
		angleA = odo.getAng();
		Sound.beep();
		
		double trueAngle, x, y;
		trueAngle = (Math.abs(angleA-angleB)/2 + BIG_ANGLE_CORRECTION)%360;
		
		odo.setPosition(new double [] {0.0, 0.0, trueAngle}, new boolean [] {false, false, true});
		Navigation nav = new Navigation(odo);
		nav.turnTo(0);
		motorControl.stopMotors();
		
		Sound.beep();
		Sound.beep();
		
		//New code: X-Y localization
		
		//Make the robot face the left side wall
		nav.turnTo(180);
		motorControl.stopMotors();
		
		//Calculate the robot's current x coordinate
		//out of its distance from the wall
		x = getFilteredData() - Lab5.SQUARE_SIZE;
		
		//Make the robot face the back wall
		nav.turnTo(-90);
		motorControl.stopMotors();
		
		//Calculate the robot's current y coordinate
		//out of its distance from the wall
		y = getFilteredData() - Lab5.SQUARE_SIZE;
		
		//Set the odometer's real x and y coordinates
		odo.setPosition(new double [] {x, y, 0.0}, new boolean [] {true, true, false});
				
		//Travel to origin and face the positive y direction
		nav.travelTo(0, 0);
		motorControl.stopMotors();
		nav.turnTo(90);
		motorControl.stopMotors();
		
		Sound.beep();
		Sound.beep();
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		int distance = (int)(usData[0]*100);
		
		if(distance > WALL_DETECTION)
			distance = WALL_DETECTION;
				
		return distance;
	}

	private boolean wallFound(){
		return getFilteredData() < WALL_DETECTION;
	}
}
