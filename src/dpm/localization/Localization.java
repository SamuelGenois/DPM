package dpm.localization;

import dpm.repository.Repository;
import dpm.util.Motors;
import dpm.util.Sensors;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import dpm.util.DPMConstants;

/**
 * Class that contains methods and parameters used for ultrasonic sensor localization
 * @author Will Liang
 *
 */
public class Localization implements DPMConstants{
	private static int motorRotate = 100;
	private RegulatedMotor leftMotor, rightMotor;
	private SampleProvider usSensor;
	private float[] usData;
	private double wallDist = 55;
	boolean initialWall;
	private double angleA, angleB;
	private double distanceA, distanceB, correctedX, correctedY;
	private double sensorDist = 8.0;
	
	/**
	 * Constructor
	 */
	public Localization() {
		this.leftMotor = Motors.getMotor(Motors.LEFT);
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
		this.usSensor = Sensors.getSensor(Sensors.US_ACTIVE);
		this.usData = new float[usSensor.sampleSize()];
	}
	
	/**
	 * Performs ultrasonic sensor localization for both angle and distance
	 * First finds correct angle by detecting the rising edge of both walls
	 * Then rotates to 180 and 270 degrees to get the distance from walls to get correct location
	 * Finally, travels to (0,0) and turns facing north
	 */
	public void doLocalization() {
		Repository.setRT(2.14,12.9);
		boolean startWithWall = getFilteredData() <= wallDist;
        angleA = angleB = 0;
		Sound.setVolume(20);
        
        // Begin Rotating in CCW
        ccwRotation();
        boolean seenAWall = false;
        
        // Start by getting the CCW angle
        while (true) {
        	double data = getFilteredData();
        	double angle = Repository.getAng();
        	
        	if (startWithWall){
        		if (data <= wallDist)
        			seenAWall = true;
            
        		if (seenAWall && data >= wallDist) {
        			Sound.beep();
                	angleA = angle;
                	break;
        		}
        	}
        }
        
        
        // Move on to the other side
        if (startWithWall){
        	leftMotor.stop(true);
        	rightMotor.stop();
        	Delay.msDelay(100);
        	cwRotation(); 
        }
        
        seenAWall = false;
        Delay.msDelay(5000);
        
        
        while (true) {
        	double data = getFilteredData();
        	double angle = Repository.getAng();
        	
            if (data <= wallDist )
                seenAWall = true;
            
            if (seenAWall && data >= wallDist) {
                Sound.beep();
                angleB = angle;
                break;
            }
        }
        
        double correctedTheta = 0;
        if (startWithWall){
        	correctedTheta = 35 + Math.abs(angleA - angleB)/2;
        }
        else{
        	correctedTheta = 220 + Math.abs(angleA-angleB)/2;
        }
        
        // Find the corrected angle and set it using odo.setPosition(x, y, theta)!
        double[] newPositions = { 0.0, 0.0, correctedTheta };
        boolean[] bools = { false, false, true };
        Repository.setPosition(newPositions, bools);

		// Now, turn 180 degrees facing the wall to find that distance, then find difference between
        // the wall and the tile, which will give us the correctedX
        
        Repository.turnTo(180);
        leftMotor.stop(true);
        rightMotor.stop();
        Sound.buzz();
        distanceA = getFilteredData();
        correctedX = distanceA + sensorDist - 30.48;
        
        newPositions = new double[] { correctedX, Repository.getY(), Repository.getAng() };
        bools = new boolean[] { true, false, false };
        Repository.setPosition(newPositions, bools);
        
        // Similarly, let's do the same with the Y!
        // Turn to 270 degrees (S) and measure the distance, which will give us the
        // correctedY when we subtract the distance from the tile distance

		Repository.turnTo(270);
		leftMotor.stop(true);
        rightMotor.stop();
        Sound.buzz();
        distanceB = getFilteredData();
		correctedY = distanceB + sensorDist - 30.48;
        newPositions = new double[] { Repository.getX(), correctedY, Repository.getAng() };
        bools = new boolean[] { false, true, false };
		Repository.setPosition(newPositions, bools);
        
        // Travel to the origin, and then face North

		Repository.travelTo(0, 0, NO_AVOIDANCE);
		Repository.turnTo(90);
		Repository.setRT(2.1, 12.5); //Set the radius and track to navigation-expected values
		Delay.msDelay(3000); // Delay
		//Not required when testing localization alone
				/*
				switch(Repository.getStartZone()){
					case LOWER_RIGHT:
						Repository.setPosition(new double[] {10*SQUARE_SIZE, 0, 180}, new boolean[] {true, true, true});
						break;
					case UPPER_LEFT:
						Repository.setPosition(new double[] {0, 10*SQUARE_SIZE, 0}, new boolean[] {true, true, true});
						break;
					case UPPER_RIGHT:
						Repository.setPosition(new double[] {10*SQUARE_SIZE, 10*SQUARE_SIZE, 270}, new boolean[] {true, true, true});
						break;
					case LOWER_LEFT:
					default:
				}
				
			}
		}*/
	}
	
	/**
	 * Getter for the first angle
	 * @return The first angle
	 */
	public double getAngleA(){
		return angleA;
	}
	/**
	 * Getter for the second angle
	 * @return The second angle
	 */
	public double getAngleB(){	
		return angleB;
	}
	
	/**
	 * Get ultrasonic sensor distance reading
	 * @return US sensor distance reading normalized to cm
	 */
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