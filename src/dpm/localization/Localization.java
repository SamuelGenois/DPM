package dpm.localization;

import dpm.repository.Repository;
import dpm.util.Motors;
import dpm.util.Sensors;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Localization {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int motorRotate = 100;
	private RegulatedMotor leftMotor, rightMotor;
	private SampleProvider usSensor;
	private float[] usData;
	private double wallDist = 35;
	boolean initialWall;
	private double angleA, angleB;
	private double distanceA, distanceB, correctedX, correctedY;
	private double sensorDist = 8.0;
	

	public Localization() {
		this.leftMotor = Motors.getMotor(Motors.LEFT);
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
		this.usSensor = Sensors.getSensor(Sensors.US_ACTIVE);
		this.usData = new float[usSensor.sampleSize()];
	}

	public void doLocalization() {
		double tolerance = 0.1;
		boolean startWithWall = getFilteredData() <= wallDist;
        angleA = angleB = 0;
		Sound.setVolume(20);
        
        // Begin Rotating in CCW
        ccwRotation();
        boolean seenAWall = false;
        
        // Let's start by getting the CCW
        while (true) {
        	double data = getFilteredData();
        	double angle = Repository.getAng();
        	
            if (data <= wallDist + tolerance) // maybe adjust tolerance?????
                seenAWall = true; // Keep rotating, like a brotato.
            
            if (seenAWall && data >= wallDist - tolerance) {
                Sound.beep();
                angleA = angle;
                break; // Break out of the loop, remove excessive boolean
            }
        }
        
        
        // Now, let's move on to the other side. HELLO FROM THE OTHER SIDEEEEEE ~
        if (startWithWall)
        	cwRotation(); 
        
        seenAWall = false;
        Delay.msDelay(1000);
        while (true) {
        	double data = getFilteredData();
        	double angle = Repository.getAng();
        	
            if (data <= wallDist + tolerance)
                seenAWall = true;
            
            if (seenAWall && data >= wallDist - tolerance) {
                Sound.beep();
                angleB = angle;
                break; // Again, break out of the infinite time loop (heh)
            }
        } // can improve this entire thing with a do-while tbh, but whatever, similar efficiency.
        
        double correctedTheta = 40 + Math.abs(angleA - angleB)/2;
        
        // Find the corrected angle and set it using odo.setPosition(x, y, theta)!
        double[] newPositions = { 0.0, 0.0, correctedTheta };
        boolean[] bools = { false, false, true };
        Repository.setPosition(newPositions, bools);

		// Now, let's turn 180 degrees facing the wall to find that distance, then find difference between
        // the wall and the tile, which will give us the correctedX
        
        Repository.turnTo(180);
        distanceA = getFilteredData();
        Sound.buzz();
        correctedX = distanceA + sensorDist - 30.48;
        
        newPositions = new double[] { correctedX, Repository.getY(), Repository.getAng() };
        bools = new boolean[] { true, false, false };
        Repository.setPosition(newPositions, bools);
        
        // Similarly, let's do the same with the Y!
        // Turn Rob (Yes, that's his name now) to 270 degrees (S) and measure the distance, which will give us the
        // correctedY when we subtract the distance from the tile distance

		Repository.turnTo(270);
		distanceB = getFilteredData();
		Sound.buzz();
		correctedY = distanceB + sensorDist - 30.48;
        newPositions = new double[] { Repository.getX(), correctedY, Repository.getAng() };
        bools = new boolean[] { false, true, false };
		Repository.setPosition(newPositions, bools);
        
        // Travel to the origin, and then face Canada, because the US is already fucked;
        // we need to look to the great white north for salvation.

		Repository.travelTo(0, 0);
		Repository.turnTo(90); // Rotate dat ass >_>?....
		Delay.msDelay(3000); // Delay because we're a slow backwards society unable to progress. #MURICA
	}

	public double getAngleA(){
		return angleA;
	}

	public double getAngleB(){	
		return angleB;
	}

	public float getFilteredData() {

		usSensor.fetchSample(usData, 0);
		float distance= usData[0]*100;		
		return distance;
	}

	private void cwRotation(){
		leftMotor.setSpeed(motorRotate);
		rightMotor.setSpeed(motorRotate);
		leftMotor.forward();
		rightMotor.backward();
	}

	private void ccwRotation(){
		leftMotor.setSpeed(motorRotate);
		rightMotor.setSpeed(motorRotate);
		leftMotor.backward();
		rightMotor.forward();
	}
}