package dpm.util;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class Sensors {
	
	private static Sensors theInstance;
	
	private SampleProvider usSensor;
	private SampleProvider colorSensor;
	
	private Sensors(){}
	
	public SampleProvider getColorSensor(){
		if (colorSensor == null)
			colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S2")).getMode("RGB");
		return colorSensor;
	}
	
	public static Sensors getInstance(){
		if(theInstance == null)
			theInstance = new Sensors();
		return theInstance;
	}
	
	public SampleProvider getUSSensor(){
		if (usSensor == null)
			usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1")).getMode("Distance");
		return usSensor;
	}
	
}
