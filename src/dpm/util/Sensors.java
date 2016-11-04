package dpm.util;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class Sensors {
	
	private static final int	US_DISTANCE = 0,
								US_LISTEN = US_DISTANCE+1,
								COLOR_RED = US_LISTEN+1,
								COLOR_RGB = COLOR_RED+1;
								
	public static final int		US_LEFT = 100,
								US_RIGHT = US_LEFT+1,
								US_PASSIVE = US_RIGHT+1,
								COLOR_BLOCK_ID = US_PASSIVE+1,
								COLOR_ODO_CORR = COLOR_BLOCK_ID+1;
	
	private static SampleProvider	leftUSSensor,
									rightUSSensor,
									usInterferenceSensor,
									odometryCorrectionLSensor,
									blockIndentificationSensor;
	
	public static SampleProvider getSensor(int id){
		switch(id){
			case US_LEFT:
				if(leftUSSensor == null)
					leftUSSensor = new Sensor(RemoteBrickManager.MASTER, "S1" , US_DISTANCE);
				return leftUSSensor;
			case US_RIGHT:
				if(rightUSSensor == null)
					rightUSSensor = new Sensor(RemoteBrickManager.MASTER, "S2" , US_DISTANCE);
				return rightUSSensor;
			case US_PASSIVE:
				if(usInterferenceSensor == null)
					usInterferenceSensor = new Sensor(RemoteBrickManager.SLAVE, "S1" , US_LISTEN);
				return usInterferenceSensor;
			case COLOR_BLOCK_ID:
				if(blockIndentificationSensor == null)
					blockIndentificationSensor = new Sensor(RemoteBrickManager.MASTER, "S3" , COLOR_RGB);
				return blockIndentificationSensor;
			case COLOR_ODO_CORR:
				if(odometryCorrectionLSensor == null)
					odometryCorrectionLSensor = new Sensor(RemoteBrickManager.MASTER, "S4" , COLOR_RED);
				return odometryCorrectionLSensor;
			default:
				return null;
		}
		
	}
	
	public static class Sensor implements SampleProvider{
		
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
