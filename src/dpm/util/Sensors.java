package dpm.util;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

/**
 * A utility class that provides references to the robot's sensor's interfaces.
 * The initialization of those interfaces is done internally.
 *  
 * @author Samuel Genois
 */
public class Sensors implements DPMConstants{
	
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
			case US_ACTIVE:
				if(sensors[US_ACTIVE] == null)
					sensors[US_ACTIVE] = new Sensor(RemoteBrickManager.MASTER, "S1" , US_DISTANCE);
				return sensors[US_ACTIVE];
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
