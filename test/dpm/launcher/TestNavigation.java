package dpm.launcher;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Printer;
import dpm.util.RemoteBrickManager;
import lejos.hardware.Button;
import lejos.hardware.Sound;

public class TestNavigation implements DPMConstants{

	private static final int	TEST_DELAY = 1000;
	
	public static void main(String[] args){
	
	(new Thread() {
		public void run() {
			while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
			RemoteBrickManager.closeSlave();
		System.exit(0);
		}
	}).start();
	Repository.setRT(2.1, 12.5);
	Printer.getInstance().display("Press any button");
	Button.waitForAnyPress();
	Printer.getInstance().display("Running");
	
	//testTurnTo();
	
	//Printer.getInstance().display("Press any button");
	//Button.waitForAnyPress();
	//Printer.getInstance().display("Running");
	
	  testTravelTo();
	
	Printer.getInstance().display("Finished");
	}
	
	private static void testTurnTo() {
		Repository.turnTo(0);
		Repository.turnTo(45);
		Repository.turnTo(90);
		Repository.turnTo(0);
		Repository.turnTo(315);
		Repository.turnTo(270);
		Repository.turnTo(0);
	}
	
	private static void testTravelTo(){
		Repository.setPosition(new double[] {0.0, 0.0, 0.0}, new boolean[] {true, true, true});
		
		Repository.travelTo(30, 30, NO_AVOIDANCE);
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		
		Repository.travelTo(0, 30, NO_AVOIDANCE);
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		
		Repository.travelTo(30, 0, NO_AVOIDANCE);
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		
		Repository.travelTo(0, 0, NO_AVOIDANCE);
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		
		Repository.turnTo(0);
		
	}
}