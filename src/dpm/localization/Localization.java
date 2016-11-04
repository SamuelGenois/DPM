package dpm.localization;

import dpm.navigation.Navigation;
import dpm.odometry.Odometer;
import dpm.util.Sensors;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Localization {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static final int MOTOR_ROTATE_SPEED = 100;

	private Odometer odo;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
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
	

	public Localization(Navigation navi) {
		this.usSensor = Sensors.getSensor(Sensors.US_LEFT);
		this.usData = new float[usSensor.sampleSize()];
		//this.navi = navi;
		this.odo = navi.getOdometer();
		this.leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		this.rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	}
	//most of the code below are re-used from previse lab with minimal value changes due to the modification of the robot.
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
				angleB = odo.getTheta(); //set it as angle B
				clockwise = false;
			}
		}
		
		//find the corrected Angle and set it using odo.setPosition(X,Y,Theta)
//		double correctedTheta = 80 + (Math.abs(angleA - angleB))/2;
		double correctedTheta = 40 + (Math.abs(angleA - angleB))/2;
		odo.setPosition(new double [] {0.0, 0.0, correctedTheta}, new boolean [] {true, true, true});
		leftMotor.stop();
		rightMotor.stop();
		
		//turn 180 degrees facing the wall to measure the distance, then by subtracting this distance 
		//	from the distance of a tile, we could determine the correctedX.
		navi.turnTo(180);
		Sound.buzz();
		distanceA = getFilteredData();
		correctedX = (distanceA + sensorDist + 2) - 30.48;
		odo.setPosition(new double [] {correctedX, odo.getY(), odo.getTheta()}, new boolean [] {true, true, true});

		//same methodology as above
		// by turning the robot to 270 (direct south), and measure the distance, we could determine the correctedY 
		// by subtracting this distance found from tile distance.
		navi.turnTo(270);
		Sound.buzz();
		distanceB = getFilteredData();
		correctedY = (distanceB + sensorDist) - 30.48;
		odo.setPosition(new double [] {odo.getX(), correctedY, odo.getTheta()}, new boolean [] {true, true, true});

		//travel to origin and face direct North (90 degrees)
		navi.travelTo(0.0, 0.0);
		Sound.buzz();
		navi.turnTo(93);
		Sound.buzz();
		Delay.msDelay(3000);
		
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
		leftMotor.setSpeed(MOTOR_ROTATE_SPEED);
		rightMotor.setSpeed(MOTOR_ROTATE_SPEED);
		leftMotor.forward();
		rightMotor.backward();
	}

	private void ccwRotation(){
		leftMotor.setSpeed(MOTOR_ROTATE_SPEED);
		rightMotor.setSpeed(MOTOR_ROTATE_SPEED);
		leftMotor.backward();
		rightMotor.forward();
	}
}
