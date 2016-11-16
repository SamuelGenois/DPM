package dpm.blocksearch;

import dpm.repository.Repository;
import dpm.util.Motors;
import dpm.util.Sensors;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/**
 * This class holds the set of routines the robot uses
 * to search and approach blue styrofoam blocks as well as
 * the routines used to navigate to the appropriate zone
 * (green or red) to deposit the blocks.
 * 
 * @author Samuel Genois, Emile Traoré
 *
 */
public class BlockSearch extends Thread{
	private final RegulatedMotor leftMotor, rightMotor; //Motor objects
	private final SampleProvider lightSensor, usSensor;	//Sensor objects
	private float[] usData, lightData;					//Sensor return arrays
	
	private final int THRESHOLD = 10;		//Threshold for number of ultrasonic sensor samples before detecing object
	private final int SCAN_DISTANCE = 3;	//Distance at which robot scans an object
	private final int ZONE_DIMENSION = 60;	//The dimension of a zone
	
	
	private int distance;		//Ultrasonic sensor distance
	private double objectAngle;	//Angle at which an object has been detected
	private boolean scanning;	//Determines whether the ultrasonic sensor should be updating the distance (only when scanning)

	

	private final int[] positions = {0, 90, 180, 270};	//Possible x/y coordinates of the positions for scanning
	private int[] xPositions, yPositions;				//Actual x and y positions for scanning
	private int[] targetZoneX, targetZoneY;				//The x and y coordinates of the target zon
	private int[] forbidZoneX, forbidZoneY;				//The x and y coordinates of the forbidden zone
	
	
	private boolean[] scanZone, scanAgain;	//Determines whether a zone is scanned and if scanned, whether it is scanned once or multiple times
	private int current_zone;				//Determines what the current zone to scan is
	
	/**
	 * Run method
	 * <br>Polls the ultrasonic sensor every 50ms to get the distance as an integer, clamped to the dimension of a zone
	 */
	public void run(){
		while (scanning) {
			try { Thread.sleep(50); } catch(Exception e){}
			int distance;
			usSensor.fetchSample(usData,0);
			//Clamping
			distance=(int)(usData[0]*100.0);
			if (distance > ZONE_DIMENSION){
				distance = ZONE_DIMENSION;
			}
			this.distance = distance;
		}
	}
	
	/**
	 * Constructor
	 * @param targetZoneX	Left and right x coordinates of target zone
	 * @param targetZoneY	Bottom and top y coordinates of forbidden zone
	 * @param forbidZoneX	Left and right x coordinates of target zone
	 * @param forbidZoneY	Bottom and top y coordinates of forbidden zone
	 */
	public BlockSearch(int[] targetZoneX, int[] targetZoneY, int[] forbidZoneX, int[] forbidZoneY ){
		this.leftMotor = Motors.getMotor(Motors.LEFT);
		this.rightMotor = Motors.getMotor(Motors.RIGHT);
		this.usSensor = Sensors.getSensor(Sensors.US_ACTIVE);
		this.usData = new float[usSensor.sampleSize()];
		this.lightSensor = Sensors.getSensor(Sensors.COLOR_BLOCK_ID);
		this.lightData = new float[usSensor.sampleSize()];
		this.current_zone = 0;
		this.xPositions = new int[positions.length*positions.length];
		this.yPositions = new int[positions.length*positions.length];
		this.objectAngle = 90;
		//Generate pairs of (x,y) coordinates for scan positions from the possible x and y coordinates
		for (int i=0; i<positions.length; i++){
			for (int j=0; j<positions.length; j++){
				xPositions[i*positions.length+j] = positions[i%positions.length];
				yPositions[i*positions.length+j] = positions[positions.length-1-j%positions.length];
			}
		}
	}
	
	/**
	 * Calculate trajectory method
	 * <br>Calculates which zones the robot will move across while scanning
	 */
	public void calculateTrajectory(){
		for (int i=0; i<xPositions.length; i++){
			//Sets zones overlapping destination area to be scanned only once (to avoid disrupting their content)
			if ((xPositions[i]-ZONE_DIMENSION > targetZoneX[0] || xPositions[i]+ZONE_DIMENSION < targetZoneX[1]) 
					&& (yPositions[i]-ZONE_DIMENSION > targetZoneY[0] || yPositions[i]+ZONE_DIMENSION < targetZoneX[1])){
				scanZone[i] = true;
				scanAgain[i] = false;
			}
			//Sets zones overlapping forbidden area to not be scanned
			if ((xPositions[i]-ZONE_DIMENSION > forbidZoneX[0] || xPositions[i]+ZONE_DIMENSION < forbidZoneX[1]) 
					&& (yPositions[i]-ZONE_DIMENSION > forbidZoneY[0] || yPositions[i]+ZONE_DIMENSION < forbidZoneY[1])){
				scanZone[i] = false;
				scanAgain[i] = false;
			}
			else{
				scanZone[i] = true;
				scanAgain[i] = true;
			}
		}
		//Also determines the closest zone to start at, i.e. the one closest to destination
		double current_closest_distance = Math.pow((xPositions[0]-(targetZoneX[0]+targetZoneX[1])/2),2)+Math.pow((yPositions[0]-(targetZoneY[0]+targetZoneY[1])/2),2);
		for (int i=0; i<xPositions.length; i++){
			if (Math.pow((xPositions[i]-(targetZoneX[0]+targetZoneX[1])/2),2)+Math.pow((yPositions[i]-(targetZoneY[0]+targetZoneY[1])/2),2) < current_closest_distance){
				current_closest_distance = Math.pow((xPositions[i]-(targetZoneX[0]+targetZoneX[1])/2),2)+Math.pow((yPositions[i]-(targetZoneY[0]+targetZoneY[1])/2),2);
				current_zone = i;
			}
		}
	}
	
	/**
	 * Travel to zone method, travels to next zone once a scan is done
	 */
	public void travelToZone(){
		//Wrap around to go back to first zone after scanning last
		current_zone = current_zone%xPositions.length;
		//If allowed to scan current zone (scanZone = true)
		if (scanZone[current_zone]){
			//If allowed to scan current zone only once, set scanZone to false for next iteration
			if (!scanAgain[current_zone]){
				scanZone[current_zone] = false;
			}
			//Travel to current zone and turn back to angle it was at when scan was paused to retrieve block
			Repository.travelTo(xPositions[current_zone], yPositions[current_zone]);
			Repository.turnTo(objectAngle);
		}
	}
	
	/**
	 * Scan for objects method, sweeps a zone in a 90 degree angle to find objects
	 */
	public void scanForObjects(){
		int counter = 0;
		//Sweep zone until 90 degrees
		while(Repository.getAng() > 0){
			//Keep scanning: set motor speeds to scanning speeds, set scanning variable to true
			leftMotor.setSpeed(30);
			rightMotor.setSpeed(-30);
			scanning = true;
			//If distance drops below dimension of a zone
			if (distance < ZONE_DIMENSION){
				//Increment the counter for object detection
				counter++;
				//If the counter exceeds a threshold, object was seen, pause scanning
				if (counter >= THRESHOLD){
					//Stop motors
					leftMotor.setSpeed(0);
					rightMotor.setSpeed(0);
					counter = 0;						//Reset the counter
					objectAngle = Repository.getAng();	//Latch the angle at which object was seen
					scanning = false;					//Set scanning variable to false
					identifyObject();					//Perform object identification
				}
			}
		}
		//If angle reaches 0 while scanning, stop scanning
		if (Repository.getAng() <= 0){
			//Stop motors
			leftMotor.setSpeed(0);
			rightMotor.setSpeed(0);
			objectAngle = 90;		//Reset angle at which object was seen
			scanning = false;		//Set scanning variable to false
			current_zone++;			//Increment current zone variable to move to next zone 
		}
	}
	
	
	/**
	 * Identify object method, moves towards an object and checks if it is a block, if it is, issues grab command
	 */
	public void identifyObject(){
		//Travel in direction of the object and read its RGB color values with light sensor
		Repository.travelTo(Repository.getX()+(distance-SCAN_DISTANCE)*Math.cos(Math.toRadians(objectAngle)), Repository.getY()+(distance-SCAN_DISTANCE)*Math.sin(Math.toRadians(objectAngle)));
		lightSensor.fetchSample(lightData, 0);
		//Compare red, green and blue values to determine if object is a block, if it is, turn to grab it with the claw
		if (lightData[1] > lightData[0] && 1000*(lightData[0]+lightData[1]+lightData[2]) > 5){
			//Sketchy way to move back a given distance, needs to change
			leftMotor.setSpeed(-150);
			rightMotor.setSpeed(-150);
			Delay.msDelay(2000);
			//Turn around and grab it
			Repository.turnTo(-180);
			Repository.grab();
		}
	}

	/**
	 * Interrupts the block searching algorithm
	 */
	public void interrupt() {
		// TODO
	}

}
