package dpm.launcher;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Printer;
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
		Button.waitForAnyPress();
		Printer.getInstance().display("Derping");
		Repository.travelTo(60, 60, AVOID_ALL);
		Repository.travelTo(60, 0, AVOID_ALL);
		Repository.travelTo(0, 60, AVOID_ALL);
		Repository.travelTo(0, 0, AVOID_ALL);
	}
}