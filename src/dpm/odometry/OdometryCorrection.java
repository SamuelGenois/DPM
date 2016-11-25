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
	private static final float LIGHT_THRESHOLD = 0.5f;
	private static final double CORRECTION_THRESHOLD = 2.0;
	private static final double REJECTION_THRESHOLD = 5.0;
	
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
				
				boolean increaseX = false, increaseY = false;
				double xDistanceFromGrid, yDistanceFromGrid;
				//Get x and y coordinates
				double	x = odometer.getX(),
						y = odometer.getY();
				//Calculate distance from grid
				//If x-position positive, distance is x-position mod spacing between lines
				//If this is larger than half the distance between lines, distance is spacing between lines - value calculated above
				//and set that x will be increased during adjustment, not decreased
				if(x>0){
					xDistanceFromGrid = x%SQUARE_SIZE;
					if(xDistanceFromGrid > SQUARE_SIZE/2){
						xDistanceFromGrid = SQUARE_SIZE - xDistanceFromGrid;
						increaseX = true;
					}
				}
				//If x-position negative, only possible distance from a line is negative of x-position
				else{
					xDistanceFromGrid = -x;
				}
				
				//Same logic for y coordinate as for x
				if(y>0){
					yDistanceFromGrid = y%SQUARE_SIZE;
					if(yDistanceFromGrid > SQUARE_SIZE/2){
						yDistanceFromGrid = SQUARE_SIZE - yDistanceFromGrid;
						increaseY = true;
					}
				}
				else{
					yDistanceFromGrid = -y;
				}
				
				//For debugging purposes, indicates where correction may occur
				if (xDistanceFromGrid < CORRECTION_THRESHOLD || yDistanceFromGrid < CORRECTION_THRESHOLD){
					Sound.beep();
				}
				
				//If vertical line (x distance from grid is within correction threshold, not y distance): correct x coordinate
				if(xDistanceFromGrid < CORRECTION_THRESHOLD && yDistanceFromGrid > REJECTION_THRESHOLD){
						if(x>0){
							//If x was not set to be increased, decrease it to the position of the previous vertical line
							if (!increaseX){
								odometer.setPosition(new double[] {Math.floor(x/SQUARE_SIZE)*SQUARE_SIZE, 0, 0},
												new boolean[] {true, false, false});
							}
							//If x was set to be increased, increase it to the position of the next vertical line
							else{
								odometer.setPosition(new double[] {Math.ceil(x/SQUARE_SIZE)*SQUARE_SIZE, 0, 0},
										new boolean[] {true, false, false});
							}
						}
						//If x is negative, only correction that makes sense is 0
						else{
							odometer.setPosition(new double[] {0, 0, 0}, new boolean[] {true, false, false});
						}
				}
				//If horizontal line (y distance from grid is within correction threshold, not x distance): correct y coordinate
				//Same logic as for x
				else if(yDistanceFromGrid < CORRECTION_THRESHOLD && xDistanceFromGrid > REJECTION_THRESHOLD){
					if(y>0){
						if (!increaseY){
							odometer.setPosition(new double[] {0, Math.floor(y/SQUARE_SIZE)*SQUARE_SIZE, 0},
								new boolean[] {false, true, false});
						}
						else{
							odometer.setPosition(new double[] {0, Math.ceil(y/SQUARE_SIZE)*SQUARE_SIZE, 0},
									new boolean[] {false, true, false});
						}
					}
					else{
						odometer.setPosition(new double[] {0, 0, 0}, new boolean[] {false, true, false});
					}
				}
				//If line crossing (both x distance or y distance from grid are within correction threshold): skip correction to prevent mistakes
				//For debugging purposes, indicates where correction has not occurred due to line crossing
				else if (yDistanceFromGrid < REJECTION_THRESHOLD && xDistanceFromGrid < REJECTION_THRESHOLD){
					Sound.buzz();
				}
			}
			try{Thread.sleep(POLLING_DELAY);}catch(InterruptedException e){}
		}
	}
	
}
