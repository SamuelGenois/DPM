package dpm.blocksearch;

import java.util.ArrayList;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Motors;
import dpm.util.Sensors;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * This class holds the set of routines the robot uses
 * to search and approach blue styrofoam blocks as well as
 * the routines used to navigate to the appropriate zone
 * (green or red) to deposit the blocks.
 * 
 * @author Samuel Genois
 *
 */
public class BlockSearch implements DPMConstants{
	
	private static final long	BACKUP_TIME = 800l;
	
	private static final int MOTOR_SCAN_SPEED = 90;
	
	private static final int	SCAN_RANGE = 61,
								COLOR_SENSOR_RANGE = 2,
								BLUE_BLOCK = 0,
								WOODEN_BLOCK = 1,
								FLOOR = 2;
	
	private int currentRegion;
	private int currentScanPoint;
	
	private int[] regionOrder;
	
	private ArrayList<Integer> goodZoneRegions, badZoneRegions;
	
	private double currentOrientation;
	
	private float[] usData,
					colorData;
	
	private boolean interrupted, greenZoneSearchable;
	
	private RegulatedMotor	leftMotor,
							rightMotor;
	
	private final SampleProvider	usSensor,
									colorSensor;
	
	/**
	 * Constructor
	 */
	public BlockSearch(){
		
		usSensor = Sensors.getSensor(Sensors.US_ACTIVE);
		usData = new float[usSensor.sampleSize()];
		
		colorSensor = Sensors.getSensor(Sensors.COLOR_BLOCK_ID);
		colorData = new float[colorSensor.sampleSize()];
		
		leftMotor = Motors.getMotor(Motors.LEFT);
		rightMotor = Motors.getMotor(Motors.RIGHT);
		leftMotor.setAcceleration(WHEEL_MOTOR_ACCELERATION);
		rightMotor.setAcceleration(WHEEL_MOTOR_ACCELERATION);
		
		regionOrder = new int[16];
		createRegionOrder();
		
		greenZoneSearchable = true;
		currentRegion = 0;
		currentScanPoint = LOWER_LEFT;
		currentOrientation = 90.0;
		
		/*if(Repository.getRole() == BUILDER){
			goodZoneRegions = getRegions(Repository.getGreenZone());
			badZoneRegions = getRegions(Repository.getRedZone());
		}
		else {
			goodZoneRegions = getRegions(Repository.getRedZone());
			badZoneRegions = getRegions(Repository.getGreenZone());
		}*/
	}
	
	/*
	 * Constructs an array containing the order in witch the regions must be scanned
	 */
	private void createRegionOrder(){
		//Stub
		for(int i=1; i<16 ;i++)
			regionOrder[i] = (i+regionOrder[0])%16;
	}
	
	/**
	 * Interrupts the block searching algorithm
	 */
	public void interrupt() {
		interrupted = true;
	}
	
	/**
	 * Searches and approaches styrofoam blocks until interrupted or until
	 * the entire field has been searched.
	 */
	public void search(){
		interrupted = false;
		
		//For testing
		if (!interrupted)
			searchRegion(0);
		if (!interrupted)
			searchRegion(1);
		if (!interrupted)
			searchRegion(5);
		if (!interrupted)
			searchRegion(4);
		
		//Final
		/*
		while(!interrupted && i<regionOrder.length){
			if(!badZoneRegions.contains(regionOrder[currentRegion])
				&& !(goodZoneRegions.contains(regionOrder[currentRegion]) && !greenZoneSearchable))
				searchRegion(regionOrder[currentRegion]);
				
			currentRegion++;
		}*/
	}
	
	/*
	 * returns an ArrayList of all of the 3 square x 3 square regions that overlap with the specified zone
	 */
	private static ArrayList<Integer> getRegions(int[] zone){
		ArrayList<Integer> regions = new ArrayList<>();
		
		regions.add(zone[0]/3 + 4*(zone[1]/3));
		regions.add(zone[2]/3 + 4*(zone[3]/3));
		
		if(regions.get(0)-3 == regions.get(1)){
			regions.add(regions.get(0)+1);
			regions.add(regions.get(1)-1);
		}
		
		return regions;
	}
	
	/*
	 * Scans a 3 square x 3 square region, picking up any blue blocks it finds in the process.
	 */
	private void searchRegion(int region){
		double[] scanPoint = new double[2];
		
		if(currentScanPoint == LOWER_LEFT){
			scanPoint[0] = (region%4)* 3 * SQUARE_SIZE;
			scanPoint[1] = (region/4)* 3 * SQUARE_SIZE;
			
			Repository.travelTo(scanPoint[0], scanPoint[1]);
			
			Repository.turnTo(currentOrientation);
			
			while(!interrupted && Repository.getAng() < 180){
				leftMotor.setSpeed(MOTOR_SCAN_SPEED);
				rightMotor.setSpeed(-MOTOR_SCAN_SPEED);
				usSensor.fetchSample(usData, 0);
				if((int)(usData[0]*100) < SCAN_RANGE){
					Sound.beep();
					leftMotor.stop(true);
					rightMotor.stop();
					currentOrientation = Repository.getAng();
					checkObject(scanPoint);
				}
			}
			
			leftMotor.stop(true);
			rightMotor.stop();
			
			Sound.beep();
			Sound.beep();
			
			currentOrientation = 270;
			currentRegion = UPPER_RIGHT;
		}
		
		scanPoint[0] = ((region%4)* 3 + 2) * SQUARE_SIZE;
		scanPoint[1] = ((region/4)* 3 + 2) * SQUARE_SIZE;
		
		Repository.travelTo(scanPoint[0], scanPoint[1]);
		
		Repository.turnTo(currentOrientation);
		
		while(!interrupted && Repository.getAng() < 180){
			leftMotor.setSpeed(MOTOR_SCAN_SPEED);
			rightMotor.setSpeed(-MOTOR_SCAN_SPEED);
			usSensor.fetchSample(usData, 0);
			if((int)(usData[0]*100) < SCAN_RANGE){
				Sound.beep();
				leftMotor.stop(true);
				rightMotor.stop();
				currentOrientation = Repository.getAng();
				checkObject(scanPoint);
			}
		}
		
		leftMotor.stop(true);
		rightMotor.stop();
		
		Sound.beep();
		Sound.beep();
		
		currentOrientation = 90;
		currentRegion = LOWER_LEFT;
		
	}
	
	/*
	 * Approaches a detected object, does appropriate interactions with it (i.e if the object is a blue foam block, picks it up),
	 * and returns to the scan point with the appropriate orientation.
	 * Checks in a 90 degree cone in front of robot to avoid false positives due to sensor's wide cone 
	 */
	private void checkObject(double[] scanPoint){
		//Moving towards where object was seen
		Repository.travelTo((usData[0]*100-COLOR_SENSOR_RANGE)*Math.cos(Math.toRadians(currentOrientation))+scanPoint[0], 
				(usData[0]*100-COLOR_SENSOR_RANGE)*Math.sin(Math.toRadians(currentOrientation))+scanPoint[1]);
		//Loop to check multiple points around object
		for(int i=0; i<7; i++){
			//Check if object is block, if it is: back up, turn around, grab block, then exit object identification
			if(identify() == BLUE_BLOCK){
				Sound.beep();
				leftMotor.setSpeed(-150);
				rightMotor.setSpeed(-150);
				try{Thread.sleep(BACKUP_TIME);} catch(InterruptedException e){}
				leftMotor.stop(true);
				rightMotor.stop();
				Repository.turnTo(Repository.getAng()+180);
				Repository.drop();
				Repository.grab();
				if(Repository.clawIsFull()){
					greenZoneSearchable = false;
					this.interrupt();
				}
				break;
			}
			//Check if object is obstacle, if it is: back up, then exit object identification
			//Also do not scan the next 30 degrees to avoid seeing block again
			else if (identify() == WOODEN_BLOCK){
				Sound.twoBeeps();
				currentOrientation -= 30.0;
				if(currentOrientation < 0.0)
					currentOrientation += 360.0;
				leftMotor.setSpeed(-150);
				rightMotor.setSpeed(-150);
				try{Thread.sleep(BACKUP_TIME);} catch(InterruptedException e){}
				break;
			}
			//If no object has been identified, move in a cone of 90 degrees centered around the object and try to identify again
			else if(i<6){
				if (i == 3){
					Repository.turnTo(Repository.getAng()+15*6);
				}
				else{
					Repository.turnTo(Repository.getAng()-15);
				}
			}
			//If no object identified for the whole 90 degree sweep, definite false positive: back up, then exit object identification
			//Also do not scan the next 10 degrees to avoid seeing the same false positive again
			else{
				Sound.buzz();
				currentOrientation -= 10.0;
				if(currentOrientation < 0.0)
					currentOrientation += 360.0;
				leftMotor.setSpeed(-150);
				rightMotor.setSpeed(-150);
				try{Thread.sleep(BACKUP_TIME);} catch(InterruptedException e){}
			}
		}
		//Return to scanning point and keep scanning
		leftMotor.stop(true);
		rightMotor.stop();
		Repository.travelTo(scanPoint[0], scanPoint[1]);
		Repository.turnTo(currentOrientation);	
	}
	
	/*
	 * Determines if the object under the light sensor is a blue foam block, a wooden block or nothing (the floor).
	 */
	private int identify(){
		colorSensor.fetchSample(colorData, 0);
		if (colorData[1] > colorData[0] && 1000*(colorData[0]+colorData[1]+colorData[2]) > 10){
			return BLUE_BLOCK;
		}
		else if (colorData[0] > colorData[1] && colorData[1] > 2*colorData[2] && 1000*(colorData[0]+colorData[1]+colorData[2]) > 100){
			return WOODEN_BLOCK;
		}
		else{
			return FLOOR;
		}
	}
}