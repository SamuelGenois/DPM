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
		Repository.setRT(2.1, 12.5);
		Repository.setPosition(new double []{0,0,90}, new boolean []{true, true, true});
		Printer.getInstance().display("Press any derp");
		Button.waitForAnyPress();
		Printer.getInstance().display("Derping");
		Repository.travelTo(30, 60);
		Repository.travelTo(60, 30);
		Repository.travelTo(0, 60);
		Repository.travelTo(0, 0);
	}
}