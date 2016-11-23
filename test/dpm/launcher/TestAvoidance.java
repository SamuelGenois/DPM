package dpm.launcher;

import dpm.repository.Repository;
import dpm.util.Printer;
import lejos.hardware.Button;

public class TestAvoidance extends Thread{
	public static void main(String[] args){
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
			System.exit(0);
			}
		}).start();
		Printer.getInstance().display("Press any button");
		Button.waitForAnyPress();
		Printer.getInstance().display("Running");
		if(Repository.travelTo(60, 60))
			Printer.getInstance().display("Success");
		else
			Printer.getInstance().display("Failed");
	}
}
