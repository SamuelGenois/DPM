package dpm.launcher;

import dpm.navigation.ObstacleAvoidance;
import dpm.repository.Repository;
import dpm.util.Printer;
import lejos.hardware.Button;
import lejos.hardware.Sound;

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
		Repository.startNavigation();
		Repository.startAvoidance();
		Repository.travelTo(60, 60);
	}
}
