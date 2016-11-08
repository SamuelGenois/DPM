package dpm.launcher;

import dpm.navigation.Navigation;
import dpm.repository.Repository;
import dpm.util.Printer;
import dpm.util.RemoteBrickManager;
import lejos.hardware.Button;

public class TestNavigation {

	private static final int	TEST_SPEED = 200,
								TEST_DELAY = 1000;
	
	private static Navigation navigation;
	
	public static void main(String[] args){
	
	(new Thread() {
		public void run() {
			while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
			RemoteBrickManager.closeSlave();
		System.exit(0);
		}
	}).start();
	
	navigation = Repository.getNavigation();
	
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
		navigation.turnTo(0);
		navigation.turnTo(Math.toRadians(45));
		navigation.turnTo(Math.toRadians(90));
		navigation.turnTo(0);
		navigation.turnTo(Math.toRadians(315));
		navigation.turnTo(Math.toRadians(270));
		navigation.turnTo(0);
	}
	
	private static void testTravelTo(){
	}
}
