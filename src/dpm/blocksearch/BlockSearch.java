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
	
	private static final long	BACKUP_TIME = 3000l;
	
	private static final int	SCAN_RANGE = 61,
								COLOR_SENSOR_RANGE = 3;
	
	private int currentRegion;
	
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
		
		regionOrder = new int[16];
		createRegionOrder();
		
		greenZoneSearchable = true;
		currentRegion = 0;
		currentOrientation = 90.0;
		
		if(Repository.getRole() == BUILDER){
			goodZoneRegions = getRegions(Repository.getGreenZone());
			badZoneRegions = getRegions(Repository.getRedZone());
		}
		else {
			goodZoneRegions = getRegions(Repository.getRedZone());
			badZoneRegions = getRegions(Repository.getGreenZone());
		}
	}
	
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
		searchRegion(0);
		
		//Final
		/*
		int i = currentRegion;
		
		while(!interrupted && i<regionOrder.length){
			if(!badZoneRegions.contains(regionOrder[i]) && !(goodZoneRegions.contains(regionOrder[i]) && !greenZoneSearchable))
				searchRegion(regionOrder[i]);
			i++;
			currentRegion = i;
		}*/
	}
	
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
	
	private void searchRegion(int region){
		double[] scanPoint = new double[2];
		scanPoint[0] = (region%4)* 3 * SQUARE_SIZE;
		scanPoint[1] = (region/4)* 3 * SQUARE_SIZE;
		
		Repository.travelTo(scanPoint[0], scanPoint[1]);
		
		Repository.turnTo(currentOrientation);
		
		leftMotor.setSpeed(150);
		rightMotor.setSpeed(-150);
		
		while(!interrupted && Repository.getAng() < 180){
			usSensor.fetchSample(usData, 0);
			if((int)(usData[0]*100) < SCAN_RANGE){
				Sound.beep();
				leftMotor.stop();
				rightMotor.stop();
				currentOrientation = Repository.getAng();
				checkObject(scanPoint);
				Repository.turnTo(currentOrientation);
				leftMotor.setSpeed(150);
				rightMotor.setSpeed(-150);
			}
		}
		
		leftMotor.stop();
		rightMotor.stop();
		
		Sound.beep();
		Sound.beep();
		
		currentOrientation = 90;
	}
	
	private void checkObject(double[] scanPoint){
		
		do{
			leftMotor.setSpeed(150);
			rightMotor.setSpeed(150);
			usSensor.fetchSample(usData, 0);
			
		} while((int)(usData[0]*100) > COLOR_SENSOR_RANGE);
		
		leftMotor.stop();
		rightMotor.stop();
		
		if(identify()){
			Sound.beep();
			Repository.grab();
			if(Repository.clawIsFull()){
				greenZoneSearchable = false;
				this.interrupt();
			}
		}
		else{
			Sound.buzz();
			currentOrientation -= 28.0;
			if(currentOrientation < 0.0)
				currentOrientation += 360.0;
		}
		
		leftMotor.setSpeed(-150);
		rightMotor.setSpeed(-150);
		try{Thread.sleep(BACKUP_TIME);} catch(InterruptedException e){}
		leftMotor.stop();
		rightMotor.stop();
		Repository.travelTo(scanPoint[0], scanPoint[1]);
		
	}
	
	private boolean identify(){
		colorSensor.fetchSample(colorData, 0);
		return colorData[0]<colorData[1];
	}
}