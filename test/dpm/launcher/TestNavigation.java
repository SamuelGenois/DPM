package dpm.launcher;

import dpm.repository.Repository;
import dpm.util.Printer;
import dpm.util.RemoteBrickManager;
import lejos.hardware.Button;
import lejos.hardware.Sound;

public class TestNavigation {

	private static final int	TEST_DELAY = 1000;
	
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
	
	testTurnTo();
	
	Printer.getInstance().display("Press any button");
	Button.waitForAnyPress();
	Printer.getInstance().display("Running");
	
	testTravelTo();
	
	Printer.getInstance().display("Finished");
	}
	
	private static void testTurnTo() {
		Repository.turnTo(0);
		Repository.turnTo(Math.toRadians(45));
		Repository.turnTo(Math.toRadians(90));
		Repository.turnTo(0);
		Repository.turnTo(Math.toRadians(315));
		Repository.turnTo(Math.toRadians(270));
		Repository.turnTo(0);
	}
	
	private static void testTravelTo(){
		Repository.setPosition(new double[] {0.0, 0.0, 0.0});
		
		Repository.travelTo(30, 30);
		Sound.beep();
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		
		Repository.travelTo(30, 30);
		Sound.beep();
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		
		Repository.travelTo(0, 30);
		Sound.beep();
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		
		Repository.travelTo(30, 0);
		Sound.beep();
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		
		Repository.travelTo(0, 0);
		Sound.beep();
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		
	}
}
