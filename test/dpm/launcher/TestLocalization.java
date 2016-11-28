package dpm.launcher;

import dpm.repository.Repository;
import dpm.util.Printer;
import dpm.util.RemoteBrickManager;
import lejos.hardware.Button;

public class TestLocalization{

	public static void main(String[] args){
		
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
				RemoteBrickManager.closeSlave();
				System.exit(0);
			}
		}).start();
		
		(new Thread() {
			public void run() {
				while(true)
					Printer.getInstance().display(Double.toString(Repository.getAng()));
			}
		}).start();
		
		Button.waitForAnyPress();
		
		Repository.localize();
	}
	
}
