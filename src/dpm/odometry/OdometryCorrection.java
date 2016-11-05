package dpm.odometry;

import dpm.util.Sensors;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

/**
 * OdometryCorrection uses grid lines on the field to correct the odometer's (x,y) position
 * 
 * @author Samuel Genois
 *
 */
public class OdometryCorrection extends Thread{
	
	private static final long POLLING_DELAY = 50l;
	private static final float LIGHT_THRESHOLD = 0.25f;
	private static final double CORRECTION_THRESHOLD = 2.0;
	private static final double SQUARE_SIZE = 30.48;
	
	private final Odometer odometer;
	private final SampleProvider sensor;
	private final float[] sensorData;
	
	private double[] position;

	/**
	 * Constructor.
	 * 
	 * @param odometer the odometer to correct
	 */
	public OdometryCorrection(Odometer odometer){
		this.odometer = odometer;
		sensor = Sensors.getSensor(Sensors.COLOR_ODO_CORR);
		sensorData = new float[sensor.sampleSize()];
		position = new double[3];
		this.start();
	}
	
	/**
	 * The run method of the odometry correction thread. Continuously
	 * searches for a grid line. When a grid line is found, calculates
	 * which x or y value the line represents and changes the odometer's
	 * x or y value to the line's value.
	 */
	@Override
	public void run(){
		while(true){
			sensor.fetchSample(sensorData, 0);
			
			if(sensorData[0] < LIGHT_THRESHOLD){
				Sound.beep();
				odometer.getPosition(position);
				
				double xDistanceFromGrid = position[Odometer.X]%SQUARE_SIZE;
				if(xDistanceFromGrid > SQUARE_SIZE/2)
					xDistanceFromGrid = SQUARE_SIZE - xDistanceFromGrid;
				
				double yDistanceFromGrid = position[Odometer.Y]%SQUARE_SIZE;
				if(yDistanceFromGrid > SQUARE_SIZE/2)
					yDistanceFromGrid = SQUARE_SIZE - yDistanceFromGrid;
				
				if(xDistanceFromGrid < CORRECTION_THRESHOLD){
					if(!(yDistanceFromGrid < CORRECTION_THRESHOLD))
						odometer.setX(Math.floor(position[Odometer.X]/SQUARE_SIZE)*SQUARE_SIZE);
				}
				else if(yDistanceFromGrid < CORRECTION_THRESHOLD)
					odometer.setY(Math.floor(position[Odometer.Y]/SQUARE_SIZE)*SQUARE_SIZE);
				
				else{
					Sound.buzz();
					Sound.buzz();
					Sound.buzz();
				}
					
			}
				
			
			try{Thread.sleep(POLLING_DELAY);}catch(InterruptedException e){}
		}
	}
	
}
