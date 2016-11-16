package dpm.odometry;

import dpm.util.DPMConstants;
import dpm.util.Sensors;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

/**
 * OdometryCorrection uses grid lines on the field to correct the odometer's (x,y) position
 * 
 * @author Samuel Genois
 *
 */
public class OdometryCorrection extends Thread implements DPMConstants{
	
	private static final long POLLING_DELAY = 50l;
	private static final float LIGHT_THRESHOLD = 0.25f;
	private static final double CORRECTION_THRESHOLD = 2.0;
	
	private final Odometer odometer;
	private final SampleProvider sensor;
	private final float[] sensorData;

	/**
	 * Constructor.
	 * 
	 * @param odometer the odometer to correct
	 */
	public OdometryCorrection(Odometer odometer){
		this.odometer = odometer;
		sensor = Sensors.getSensor(Sensors.COLOR_ODO_CORR);
		sensorData = new float[sensor.sampleSize()];
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
				
				double xDistanceFromGrid = odometer.getX()%SQUARE_SIZE;
				if(xDistanceFromGrid > SQUARE_SIZE/2)
					xDistanceFromGrid = SQUARE_SIZE - xDistanceFromGrid;
				
				double yDistanceFromGrid = odometer.getY()%SQUARE_SIZE;
				if(yDistanceFromGrid > SQUARE_SIZE/2)
					yDistanceFromGrid = SQUARE_SIZE - yDistanceFromGrid;
				
				if(xDistanceFromGrid < CORRECTION_THRESHOLD){
					if(!(yDistanceFromGrid < CORRECTION_THRESHOLD))
						odometer.setPosition(new double[] {Math.floor(odometer.getX()/SQUARE_SIZE)*SQUARE_SIZE, odometer.getY(),odometer.getAng()},
												new boolean[] {true, true, true});
				}
				else if(yDistanceFromGrid < CORRECTION_THRESHOLD)
					odometer.setPosition(new double[] {odometer.getX(), Math.floor(odometer.getY()/SQUARE_SIZE)*SQUARE_SIZE, odometer.getAng()},
						new boolean[] {true, true, true});
				
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
