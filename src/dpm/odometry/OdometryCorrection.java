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
	private static final float LIGHT_THRESHOLD = 0.3f;
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
				
				double xDistanceFromGrid, yDistanceFromGrid;
				double	x = odometer.getX(),
						y = odometer.getY();
				
				if(x>0){
					xDistanceFromGrid = x%SQUARE_SIZE;
					if(xDistanceFromGrid > SQUARE_SIZE/2)
						xDistanceFromGrid = SQUARE_SIZE - xDistanceFromGrid;
				}
				else
					xDistanceFromGrid = -x;
				
				if(y>0){
					yDistanceFromGrid = y%SQUARE_SIZE;
					if(yDistanceFromGrid > SQUARE_SIZE/2)
						yDistanceFromGrid = SQUARE_SIZE - yDistanceFromGrid;
				}
				else
					yDistanceFromGrid = -y;
				
				if(xDistanceFromGrid < CORRECTION_THRESHOLD){
					if(!(yDistanceFromGrid < CORRECTION_THRESHOLD))
						if(x>0)
							odometer.setPosition(new double[] {Math.floor(x/SQUARE_SIZE)*SQUARE_SIZE, 0, 0},
												new boolean[] {true, false, false});
						else
							odometer.setPosition(new double[] {0, 0, 0}, new boolean[] {true, false, false});
				}
				else if(yDistanceFromGrid < CORRECTION_THRESHOLD)
					if(y>0)
						odometer.setPosition(new double[] {0, Math.floor(y/SQUARE_SIZE)*SQUARE_SIZE, 0},
								new boolean[] {false, true, false});
					else
						odometer.setPosition(new double[] {0, 0, 0}, new boolean[] {false, true, false});
				
				else{
					Sound.buzz();
				}
					
			}
				
			
			try{Thread.sleep(POLLING_DELAY);}catch(InterruptedException e){}
		}
	}
	
}
