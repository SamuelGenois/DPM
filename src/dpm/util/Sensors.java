package dpm.util;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

/**
 * 
 * @author Samuel Genois
 */
public class Sensors {
	
	/**
	 * The id of the ultrasonic sensor used to detected
	 * distance in front of the robot
	 */
	public static final int	US_LEFT = 0;
	
	/**
	 * The id of the ultrasonic sensor used to detect
	 * ultrasonic interference
	 */
	public static final int	US_PASSIVE = 1;
	
	/**
	 * The id of the color sensor used to identify blue
	 * styrofoam blocks
	 */
	public static final int	COLOR_BLOCK_ID = 2;
	
	/**
	 * The id of the color sensor used to detect grid lines
	 */
	public static final int	COLOR_ODO_CORR = 3;
	
	private static final int	US_DISTANCE = 100,
								US_LISTEN = 101,
								COLOR_RED = 102,
								COLOR_RGB = 103;
	
	private static SampleProvider[]	sensors = new SampleProvider[4];
	
	/**
	 * Returns a reference to the sensor corresponding to the
	 * provided id. If the sensor (interface) is yet not initialized,
	 * initializes it.
	 * 
	 * @param id the id of the desired sensor reference
	 * @return the sensor reference
	 */
	public static SampleProvider getSensor(int id){
		switch(id){
			case US_LEFT:
				if(sensors[US_LEFT] == null)
					sensors[US_LEFT] = new Sensor(RemoteBrickManager.MASTER, "S1" , US_DISTANCE);
				return sensors[US_LEFT];
			case US_PASSIVE:
				if(sensors[US_PASSIVE] == null)
					sensors[US_PASSIVE] = new Sensor(RemoteBrickManager.SLAVE, "S1" , US_LISTEN);
				return sensors[US_PASSIVE];
			case COLOR_BLOCK_ID:
				if(sensors[COLOR_BLOCK_ID] == null)
					sensors[COLOR_BLOCK_ID] = new Sensor(RemoteBrickManager.MASTER, "S3" , COLOR_RGB);
				return sensors[COLOR_BLOCK_ID];
			case COLOR_ODO_CORR:
				if(sensors[COLOR_ODO_CORR] == null)
					sensors[COLOR_ODO_CORR] = new Sensor(RemoteBrickManager.MASTER, "S4" , COLOR_RED);
				return sensors[COLOR_ODO_CORR];
			default:
				return null;
		}
		
	}
	
	private static class Sensor implements SampleProvider{
		
		private SampleProvider sensor;
		
		public Sensor(int brick, String portName, int sensorType){
			switch(sensorType){
				case US_DISTANCE:
					if(brick == RemoteBrickManager.MASTER)
						sensor = new EV3UltrasonicSensor(LocalEV3.get().getPort(portName)).getDistanceMode();
					else if(brick == RemoteBrickManager.SLAVE)
						sensor = RemoteBrickManager.getSlave().createSampleProvider(portName, "lejos.hardware.sensor.EV3UltrasonicSensor", "Distance");
					else
						sensor = null;
					break;
				case US_LISTEN:
					if(brick == RemoteBrickManager.MASTER)
						sensor = new EV3UltrasonicSensor(LocalEV3.get().getPort(portName)).getListenMode();
					else if(brick == RemoteBrickManager.SLAVE)
						sensor = RemoteBrickManager.getSlave().createSampleProvider(portName, "Ultrasonic", "Listen");
					else
						sensor = null;
					break;
				case COLOR_RED:
					if(brick == RemoteBrickManager.MASTER)
						sensor = new EV3ColorSensor(LocalEV3.get().getPort(portName)).getRedMode();
					else if(brick == RemoteBrickManager.SLAVE)
						sensor = RemoteBrickManager.getSlave().createSampleProvider(portName, "Color", "Red");
					else
						sensor = null;
					break;
				case COLOR_RGB:
					if(brick == RemoteBrickManager.MASTER)
						sensor = new EV3ColorSensor(LocalEV3.get().getPort(portName)).getRGBMode();
					else if(brick == RemoteBrickManager.SLAVE)
						sensor = RemoteBrickManager.getSlave().createSampleProvider(portName, "Color", "RGB");
					else
						sensor = null;
					break;
				default:
					sensor = null;
			}
		}

		@Override
		public synchronized int sampleSize() {
			return sensor.sampleSize();
		}

		@Override
		public synchronized void fetchSample(float[] sample, int offset) {
			sensor.fetchSample(sample, offset);
		}
	}
}
