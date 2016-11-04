package dpm.launcher;

import dpm.util.Motors;
import dpm.util.Printer;
import lejos.hardware.Button;
import lejos.robotics.RegulatedMotor;

public class TestMotor {
	
	private static final int	TEST_SPEED = 200,
								TEST_DELAY = 1000;
	
	private static RegulatedMotor	leftMotor, rightMotor;

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
		
		leftMotor = Motors.getMotor(Motors.LEFT);
		rightMotor = Motors.getMotor(Motors.RIGHT);
		
		testRotateMotors();
		
		Printer.getInstance().display("Press any button");
		Button.waitForAnyPress();
		Printer.getInstance().display("Running");
		
		testSetMotorSpeed();
		
		Printer.getInstance().display("Finished");
	}
	
	private static void testRotateMotors() {
		
		leftMotor.rotate(90, true);
		rightMotor.rotate(-90, false);
		leftMotor.rotate(90, true);
		rightMotor.rotate(-90, false);
		leftMotor.rotate(-180, true);
		rightMotor.rotate(180, false);
		leftMotor.rotate(-180, true);
		rightMotor.rotate(180, false);
		leftMotor.rotate(-180, true);
		rightMotor.rotate(180, false);
		leftMotor.rotate(360, true);
		rightMotor.rotate(-360, false);
	}
	
	private static void testSetMotorSpeed(){
		
		leftMotor.setSpeed(TEST_SPEED);
		rightMotor.setSpeed(TEST_SPEED);
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		leftMotor.stop();
		rightMotor.stop();
		
		leftMotor.setSpeed(TEST_SPEED);
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		leftMotor.stop();
		
		rightMotor.setSpeed(TEST_SPEED);
		try {Thread.sleep(TEST_DELAY);} catch (InterruptedException e) {}
		rightMotor.stop();
	}
}
