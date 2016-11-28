package dpm.launcher;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Printer;
import dpm.util.Sensors;
import lejos.hardware.Button;

public class TestRedZone implements Launcher, DPMConstants{
	
	public TestRedZone(){}
	
	public static void main(String args[]){
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
			System.exit(0);
			}
		}).start();
		Printer.getInstance().display("Press any button");
		Sensors.getSensor(US_ACTIVE);
		Button.waitForAnyPress();
		Printer.getInstance().display("Running");
		Repository.setRT(2.1, 12.3);
		Repository.launch(new TestRedZone());
		Repository.travelTo(60, 60, AVOID_ALL);
		Printer.getInstance().display("Finished");
	}

	@Override
	public int[] getGreenZone() {
		return new int[] {1, 1, 2, -1};
	}

	@Override
	public int[] getRedZone() {
		return new int[] {1, 2, 2, 1};
	}

	@Override
	public int getStartZone() {
		return LOWER_LEFT;
	}

	@Override
	public int getRole() {
		return BUILDER;
	}

}
