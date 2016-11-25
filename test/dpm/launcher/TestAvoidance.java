package dpm.launcher;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Printer;
import dpm.util.Sensors;
import lejos.hardware.Button;

public class TestAvoidance extends Thread implements DPMConstants{
	public static void main(String[] args){
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
			System.exit(0);
			}
		}).start();
		Printer.getInstance().display("Press any derp");
		Sensors.getSensor(US_ACTIVE);
		Button.waitForAnyPress();
		Printer.getInstance().display("Derping");
		Repository.setRT(2.21, 12.35);
		Repository.travelTo(60, 0, AVOID_OR_PICKUP);
		/*Repository.travelTo(5, 60, AVOID_ALL);
		Repository.travelTo(10, 60, AVOID_ALL);
		Repository.travelTo(30, 60, AVOID_ALL);
		Repository.travelTo(60, 60, AVOID_ALL);
		Repository.travelTo(0, 60, AVOID_ALL);
		Repository.travelTo(0, 0, AVOID_ALL);*/
	}
}