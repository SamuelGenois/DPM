package dpm.launcher;

import dpm.util.Printer;
import dpm.util.RemoteBrickManager;
import dpm.util.Sensors;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;

public class TestSensor{
	
	private static final long PRINTER_DELAY = 50l;

	public static void main(String[] args){
		
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
				RemoteBrickManager.closeSlave();
				System.exit(0);
			}
		}).start();
		
		Printer.getInstance().display("Press any button");
		Button.waitForAnyPress();
		
		testRemoteSensor();
	}
	
	private static void testRemoteSensor(){
		SampleProvider usSensor = Sensors.getSensor(Sensors.US_LEFT);
		float[] usData = new float[1];
		
		while(true){
			usSensor.fetchSample(usData, 0);
			Printer.getInstance().display(Integer.toString((int)(usData[0]*100)));
			try {Thread.sleep(PRINTER_DELAY);} catch (InterruptedException e) {}
		}
	}
}
