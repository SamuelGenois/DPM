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
	
	private float[] usData;
	
	private boolean interrupted, greenZoneSearchable;
	
	private RegulatedMotor	leftMotor,
							rightMotor;
	
	private final SampleProvider	usSensor;
	
	/**
	 * Constructor
	 */
	public BlockSearch(){
		
		usSensor = Sensors.getSensor(Sensors.US_ACTIVE);
		usData = new float[usSensor.sampleSize()];
		
		leftMotor = Motors.getMotor(Motors.LEFT);
		rightMotor = Motors.getMotor(Motors.RIGHT);
		leftMotor.setAcceleration(WHEEL_MOTOR_ACCELERATION);
		rightMotor.setAcceleration(WHEEL_MOTOR_ACCELERATION);
		
		/*
		if(Repository.getRole() == BUILDER){
			goodZoneRegions = getRegions(Repository.getGreenZone());
			badZoneRegions = getRegions(Repository.getRedZone());
		}
		else {
			goodZoneRegions = getRegions(Repository.getRedZone());
			badZoneRegions = getRegions(Repository.getGreenZone());
		}*/
		
		regionOrder = new int[16];
		
		/*
		switch(Repository.getStartZone()){
			case LOWER_RIGHT:
				createRegionOrder(3);
				break;
			case UPPER_LEFT:
				createRegionOrder(12);
				break;
			case UPPER_RIGHT:
				createRegionOrder(15);
				break;
			case LOWER_LEFT:
			default:
				createRegionOrder(0);
		}*/
		
		greenZoneSearchable = true;
		currentRegion = 0;
		currentScanPoint = LOWER_LEFT;
		currentOrientation = 90.0;
	}
	
	/*
	 * Constructs an array containing the order in witch the regions must be scanned
	 */
	private void createRegionOrder(int startingCorner){
		
		for(int i=0; i<16; i++)
			regionOrder[i] = -1;
		
		regionOrder[0] = startingCorner;
		
		//Stub
		/*
		for(int i=1; i<16 ;i++)
			regionOrder[i] = (i+regionOrder[0])%16;
		return;
		*/
		
		ArrayList<Integer[]> edges = new ArrayList<>();
		
		//For every region i...
		for(int i=0; i<16; i++){
			
			//If i is not a bad zone region
			if(!badZoneRegions.contains(i)){
				
				//If i is not in the topmost row of regions
				if(i<12){
					
					//If i is not in the leftmost column of regions
					//and if the upper left adjacent region is not a bad zone region,
					//add the edge between it and i to edges.
					if((i%4)>0 && !badZoneRegions.contains(i+3))
						edges.add(new Integer[]{i, i+3});
					
					//If the upper adjacent region is not a bad zone region,
					//add the edge between it and i to edges.
					if(!badZoneRegions.contains(i+4))
						edges.add(new Integer[]{i, i+4});
					
					//If i is not in the rightmost column of regions
					//and if the upper right adjacent region is not a bad zone region,
					//add the edge between it and i to edges.
					if((i%4)<3 && !badZoneRegions.contains(i+5))
						edges.add(new Integer[]{i, i+5});

				}
				
				//If i is not in the rightmost column of regions
				//and if the right adjacent region is not a bad zone region,
				//add the edge between it and i to edges.
				if((i%4)<3 && !badZoneRegions.contains(i+1))
					edges.add(new Integer[]{i, i+1});
				
			}
		}
		
		ArrayList<Integer> pathToGoodZone;
		ArrayList<Integer[]> leftovers = new ArrayList<Integer[]>();
		
		if(!goodZoneRegions.isEmpty()){
		
			pathToGoodZone = getShortestPath(regionOrder[0], goodZoneRegions.get(0), edges);
			
			for(int i=0; i<16; i++)
				if(!(pathToGoodZone.contains(i) || badZoneRegions.contains(i)))
					leftovers.add(new Integer[]{i, getShortestPath(goodZoneRegions.get(0), i, edges).size()});
		}
		
		else{
			pathToGoodZone = new ArrayList<>();
			
			for(int i=0; i<16; i++)
				if(!badZoneRegions.contains(i))
					leftovers.add(new Integer[]{i, getShortestPath(startingCorner, i, edges).size()});
		}
		
		//Sort the leftovers in increasing order of shortest path from the green zone upper right region.
		for(int i=leftovers.size()-1; i>=0; i--){
			int maxIndex = 0;
			for(int j=0; j<i; j++)
				if(leftovers.get(j)[1] >= leftovers.get(maxIndex)[1])
					maxIndex = j;
			Integer[] temp = leftovers.get(i);
			leftovers.set(i, leftovers.get(maxIndex));
			leftovers.set(maxIndex, temp);
		}
		
		for(int i=1; i<pathToGoodZone.size(); i++)
			regionOrder[i] = pathToGoodZone.get(i);
		
		for(int i=0; i<leftovers.size(); i++){
			regionOrder[i+pathToGoodZone.size()] = leftovers.get(i)[0];
		}
	}
	
	private static ArrayList<Integer> getShortestPath(Integer start, Integer end, ArrayList<Integer[]> edges){
		ArrayList<ArrayList<Integer[]>> edgesUsed = new ArrayList<>();
		ArrayList<ArrayList<Integer>> nodesFound = new ArrayList<>();
		boolean[] discovered = new boolean[16];
		
		int i = 0;
		nodesFound.add(new ArrayList<Integer>());
		nodesFound.get(i).add(start);
		discovered[start] = true;
		
		while(!nodesFound.get(i).contains(end)){
			
			if(nodesFound.get(i).isEmpty())
				return new ArrayList<Integer>();
			
			edgesUsed.add(new ArrayList<Integer[]>());
			nodesFound.add(new ArrayList<Integer>());
			
			for(Integer node : nodesFound.get(i))
				for(Integer[] edge: edges){
					if(edge[0] == node && !discovered[edge[1]]){
						nodesFound.get(i+1).add(edge[1]);
						edgesUsed.get(i).add(edge);
						discovered[edge[1]] = true;
					}
					if(edge[1] == node && !discovered[edge[0]]){
						nodesFound.get(i+1).add(edge[0]);
						edgesUsed.get(i).add(edge);
						discovered[edge[0]] = true;
					}
				}
			i++;
		}
		
		ArrayList<Integer> path = new ArrayList<>();
		Integer node = start;
		path.add(node);
		
		for(int j=0; j<i; j++){
			
			for(Integer[] edge : edgesUsed.get(j)){
				if(edge[0] == node)
					node = edge[1];
				if(edge[1] == node)
					node = edge[0];
			}
			path.add(node);
		}
		
		return path;
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
		/*if (!interrupted)
			searchRegion(1);
		if (!interrupted)
			searchRegion(5);
		if (!interrupted)
			searchRegion(4);*/
		
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
			
			Repository.travelTo(scanPoint[0], scanPoint[1], AVOID_OR_PICKUP);
			
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
		
		Repository.travelTo(scanPoint[0], scanPoint[1], AVOID_OR_PICKUP);
		
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
	
	public boolean quickPickup(double distance){
		double x = Repository.getX();
		double y = Repository.getY();
		double angle = Repository.getAng();
		
		Repository.travelTo(x + (distance) * Math.cos(Math.toRadians(angle)), y + (distance) * Math.sin(Math.toRadians(angle)), NO_AVOIDANCE);
		
		//TODO
		boolean blockPickedUp = false;
		/*
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
			blockPickedUp = true;
		}
		else{
			leftMotor.setSpeed(-150);
			rightMotor.setSpeed(-150);
			try{Thread.sleep(BACKUP_TIME*4);} catch(InterruptedException e){}
			blockPickedUp = false;
		}
		*/
		
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
				leftMotor.setSpeed(-150);
				rightMotor.setSpeed(-150);
				Repository.turnTo(angle);
				try{Thread.sleep(BACKUP_TIME*4);} catch(InterruptedException e){}
				break;
			}
			//If no object has been identified, move in a cone of 90 degrees centered around the object and try to identify again
			else {
				if (i == 3){
					Repository.turnTo(Repository.getAng()+15*6);
				}
				else{
					Repository.turnTo(Repository.getAng()-15);
				}
				if(i==6){
					Sound.buzz();
					leftMotor.setSpeed(-150);
					rightMotor.setSpeed(-150);
					try{Thread.sleep(BACKUP_TIME*4);} catch(InterruptedException e){}
				}
			}
		}
		
		Repository.turnTo(angle);
		return blockPickedUp;
	}
	
	/*
	 * Approaches a detected object, does appropriate interactions with it (i.e if the object is a blue foam block, picks it up),
	 * and returns to the scan point with the appropriate orientation.
	 * Checks in a 90 degree cone in front of robot to avoid false positives due to sensor's wide cone 
	 */
	private void checkObject(double[] scanPoint){
		//Moving towards where object was seen
		Repository.travelTo((usData[0]*100-COLOR_SENSOR_RANGE)*Math.cos(Math.toRadians(currentOrientation))+scanPoint[0], 
				(usData[0]*100-COLOR_SENSOR_RANGE)*Math.sin(Math.toRadians(currentOrientation))+scanPoint[1], NO_AVOIDANCE);
		
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
		Repository.travelTo(scanPoint[0], scanPoint[1], NO_AVOIDANCE);
		Repository.turnTo(currentOrientation);	
	}
	
	/*
	 * Determines if the object under the light sensor is a blue foam block, a wooden block or nothing (the floor).
	 */
	private static int identify(){
		
		SampleProvider colorSensor = Sensors.getSensor(Sensors.COLOR_BLOCK_ID);
		float[] colorData = new float[colorSensor.sampleSize()];
		
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