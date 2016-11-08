package dpm.localization;

import dpm.navigation.Navigation;
import dpm.odometry.Odometer;
import dpm.repository.Repository;
import dpm.util.Motors;
import dpm.util.Sensors;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Localization {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static final int MOTOR_ROTATE_SPEED = 100;

	private Odometer odo;
	private RegulatedMotor leftMotor, rightMotor;
	private Navigation navi;
	private SampleProvider usSensor;
	private float[] usData;
	private double wallDist = 31;
	private boolean clockwise = true;
	private boolean counterclockwise = true;
	boolean initialWall;
	public static double angleA, angleB;
	public static double distanceA, distanceB, correctedX, correctedY;
	private double sensorDist = 8.0;
	

	/**
	 * Constructor
	 */
	public Localization() {
		this.usSensor = Sensors.getSensor(Sensors.US_ACTIVE);
		this.usData = new float[usSensor.sampleSize()];
		this.navi = Repository.getNavigation();
		this.odo = Repository.getOdometer();
		this.leftMotor = Motors.getMotor(Motors.LEFT); 
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
	}
	
	//most of the code below are re-used from previse lab with minimal value changes due to the modification of the robot.
	/**
	 * Performs localization.
	 */
	public void doLocalization() {
		angleA = 0; 
		angleB = 0;
		Sound.setVolume(20);
		ccwRotation();	//Start by rotating counterclockwise

		// keep rotating until the robot sees a wall, then latch the angle
		while(counterclockwise){ 
			
			if(getFilteredData() <= wallDist){		//since we're using "rising edge", while it's facing the wall...
				ccwRotation();						//rotating counterclockwise

				if(getFilteredData() > wallDist){	// as it detects the first opening
					odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
					Sound.beep();
					angleA = 0.0;					//set angle A
					counterclockwise = false;
				}
			}
		}

		boolean reverse = false;
		while(clockwise){
			cwRotation();	//rotating clockwise

			if(getFilteredData() <= wallDist){ 
				reverse = true;
			}
			if(getFilteredData() >= wallDist && reverse == true){ //found the second opening,
				Sound.beep();
				angleB = odo.getAng(); //set it as angle B
				clockwise = false;
			}
		}
		
		//find the corrected Angle and set it using odo.setPosition(X,Y,Theta)
//		double correctedTheta = 80 + (Math.abs(angleA - angleB))/2;
		double correctedTheta = 40 + (Math.abs(angleA - angleB))/2;
		odo.setPosition(new double [] {0.0, 0.0, correctedTheta}, new boolean [] {true, true, true});
		leftMotor.stop();
		rightMotor.stop();
		
		navi.turnTo(0);
		Button.waitForAnyPress();
		
		//turn 180 degrees facing the wall to measure the distance, then by subtracting this distance 
		//	from the distance of a tile, we could determine the correctedX.
		navi.turnTo(Math.PI);
		Sound.buzz();
		distanceA = getFilteredData();
		correctedX = (distanceA + sensorDist + 2) - 30.48;
		odo.setPosition(new double [] {correctedX, odo.getY(), odo.getAng()}, new boolean [] {true, true, true});

		//same methodology as above
		// by turning the robot to 270 (direct south), and measure the distance, we could determine the correctedY 
		// by subtracting this distance found from tile distance.
		navi.turnTo(3*Math.PI/2);
		Sound.buzz();
		distanceB = getFilteredData();
		correctedY = (distanceB + sensorDist) - 30.48;
		odo.setPosition(new double [] {odo.getX(), correctedY, odo.getAng()}, new boolean [] {true, true, true});

		//travel to origin and face direct North (90 degrees)
		navi.travelTo(0.0, 0.0);
		Sound.buzz();
		navi.turnTo(Math.toRadians(93));
		Sound.buzz();
		Delay.msDelay(3000);
		
	}

	@SuppressWarnings("unused")
	private double getAngleaA(){
		return angleA;
	}

	@SuppressWarnings("unused")
	private double getAngleaB(){	
		return angleB;
	}

	private float getFilteredData() {

		usSensor.fetchSample(usData, 0);
		float distance= usData[0]*100;		
		return distance;
	}

	private void cwRotation(){
		leftMotor.setSpeed(MOTOR_ROTATE_SPEED);
		rightMotor.setSpeed(-MOTOR_ROTATE_SPEED);
	}

	private void ccwRotation(){
		leftMotor.setSpeed(-MOTOR_ROTATE_SPEED);
		rightMotor.setSpeed(MOTOR_ROTATE_SPEED);
	}
}
