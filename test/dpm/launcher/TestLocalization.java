package dpm.launcher;

import dpm.repository.Repository;
import dpm.util.Printer;
import dpm.util.RemoteBrickManager;
import lejos.hardware.Button;

public class TestLocalization {

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
		Printer.getInstance().display("Running");
		
		Repository.localize();
		
		Printer.getInstance().display("Finished");
	}
	
}
