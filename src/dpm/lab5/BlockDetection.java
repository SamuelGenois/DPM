package dpm.lab5;

import dpm.util.Sensors;
import lejos.robotics.SampleProvider;

public class BlockDetection extends Thread{
	
	private static final int	DISTANCE_CAP = 60,
								DETECTION_DISTANCE = 15;
	
	private Data data;
	private final SampleProvider usSensor, cSensor;
	private float[] usData, color;
	
	//Constructor
	BlockDetection(){
		data = new Data(false, false, Integer.MAX_VALUE);
		usSensor = Sensors.getInstance().getUSSensor();
		cSensor = Sensors.getInstance().getColorSensor();
		usData = new float[3];
		color = new float[3];
		this.start();
	}
	
	//Returns the most recent Data object
	public synchronized Data getData(){
		return data;
	}
	
	//The run method of the Thread
	public void run(){
		while(true){
			usSensor.fetchSample(usData, 1);
			updateData();
		}
	}
	
	//Updates the Data object
	private synchronized void updateData(){
		int distance;
		boolean objectDetected, blockIsBlue;
		
		//The distance in centimeters obtained by the us sensor is capped
		//at DISTANCE_CAP
		distance = Math.min((int)(usData[1] * 100), DISTANCE_CAP);
		
		//An object is detected if the distance returned by the us sensor
		//is lesser or equal to DETECTION_DISTANCE (15cm)
		objectDetected = distance <= DETECTION_DISTANCE;
		
		//If an object is detected, the object is a styrofoam block if its
		//green color component is greater than its red color component.
		if(objectDetected){
			cSensor.fetchSample(color, 0);
			blockIsBlue = color[0]<color[1];
		}
		else
			blockIsBlue = false;
		
		//Update data
		data = new Data(objectDetected, blockIsBlue, distance);
	}
	
	//A collection of information related to block detection
	public static class Data{
								//true if an object is detected
		public final boolean	objectDetected,
								//true if the object detected is a blue styrofoam block
								blockIsBlue;
		public final int obstacleDistance;
		
		Data(boolean objectDetected, boolean blockIsBlue, int obstacleDistance){
			this.objectDetected = objectDetected;
			this.blockIsBlue = blockIsBlue;
			this.obstacleDistance = obstacleDistance;
		}
	}
}
