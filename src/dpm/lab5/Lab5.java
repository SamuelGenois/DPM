package dpm.lab5;

import dpm.util.MotorControl;
import dpm.util.Printer;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Lab5 {
	
	public static final double	SQUARE_SIZE = 30.48,
								CLAW_RANGE = 3.0;
	
	public static final long ONE_SECOND = 1000l;
	
	public static void main(String[] args){
		
		/* This tread allows us to terminate the program at any time by pressing
		 * the escape button.
		 */
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
				System.exit(0);
			}
		}).start();
		
		//Display a simple menu
		Printer.display("Press left:", 0, true);
		Printer.display("Simple detection", 1, false);
		Printer.display("Press right:", 2, false);
		Printer.display("Block search", 3, false);
		
		int choice;
		do
			choice = Button.waitForAnyPress();
		while(choice != Button.ID_LEFT && choice != Button.ID_RIGHT);
		
		if(choice == Button.ID_LEFT)
			simpleDetection();
		
		else
			blockSearch();
	}
	
	//Updates the LCD's display as specified by the demo instructions.
	//Additionally, displays the last us sensor reading in cm.
	private static void updateLCD(BlockDetection.Data data){
		if(data.objectDetected){
			Printer.display("Object detected", 0, true);
			if(data.blockIsBlue)
				Printer.display("Block", 2, false);
			else
				Printer.display("Not Block", 2, false);
		}
		else
			Printer.display("No object detected", 0, true);
		
		Printer.display(Integer.toString(data.obstacleDistance), 4, false);
	}

	//A simple routine to demonstrate the robot's capacity to detect
	//styrofoam blocks
	private static void simpleDetection() {
		BlockDetection bd = new BlockDetection();
		while(true){
			updateLCD(bd.getData());
			try {Thread.sleep(50);} catch (InterruptedException e) {}
		}
	}

	//A routine where the robot finds a styrofoam block and moves to a
	//predetermined location while avoiding obstacles.
	private static void blockSearch() {
		
		Odometer odo = new Odometer();
		Navigation nav = new Navigation(odo);
		MotorControl mc = MotorControl.getInstance();
		
		//The robot localizes itself
		new USLocalizer(odo).doLocalization();
		
		Button.waitForAnyPress();
		
		int objectCounter = 0;
		double[] objectOrientation = new double[2];
		BlockDetection bd = new BlockDetection();
		BlockDetection.Data data;
		
		mc.setMotorAbsoluteSpeeds(new int[] {USLocalizer.ROTATION_SPEED, -USLocalizer.ROTATION_SPEED}, MotorControl.BOTH_MOTORS);
		
		//Until the robot completes its 90 degree scan
		//or until the robot detects two objects
		while(odo.getAng() >= 0 && objectCounter < 2){
			
			data = bd.getData();
			
			//If an object is found within 60 cm
			if(data.obstacleDistance < 60){
				//Store the robot's current orientation
				objectOrientation[objectCounter] = odo.getAng();
				objectCounter ++;
				//To prevent the robot from detecting the same object twice;
				try {Thread.sleep(ONE_SECOND);} catch (InterruptedException e) {}
			}
		}
		mc.stopMotors();
		
		
		if(objectCounter == 0){
			//If the robot has not detected anything in its first scan,
			//the routine has failed
			for(int i=0; i<3; i++)
				Sound.buzz();
			return;
		}
		else{
			//Moves towards the first detected object until it is considered
			//detected by BlockDetection.
			nav.turnTo(objectOrientation[0]);
			
			mc.setMotorAbsoluteSpeeds(new int[] {USLocalizer.ROTATION_SPEED, USLocalizer.ROTATION_SPEED}, MotorControl.BOTH_MOTORS);
			
			do{
			data = bd.getData();
			} while(!data.objectDetected);
			
			mc.stopMotors();
			
			if(data.blockIsBlue){
				
				//Grab the block
				grabBlock();
				
				if(objectOrientation[0] == 1){
					//The wooden block is most likely behind the styrofoam block,
					//possibly blocking the robot's shortest path to its final destination
					
					//In theory, by using this sequence of travelTo calls the robot should
					//not collide with any obstacle
					nav.travelTo(0,0);
					
					if(objectOrientation[0] > 45.0)
						nav.travelTo(0, 5*SQUARE_SIZE/2);
					else
						nav.travelTo(5*SQUARE_SIZE/2, 0);
					
					nav.travelTo(5*SQUARE_SIZE/2,5*SQUARE_SIZE/2);
				}
				else{
					//The wooden block is not behind the styrofoam block
					//and thus not behing the robot and its final destinaion
					nav.travelTo(5*SQUARE_SIZE/2,5*SQUARE_SIZE/2);
				}
				
			}
			else{
				//The object travelled to is the wooden block
				if(objectOrientation[0] == 1){
					//The wooden block is most likely hiding the blue block
					
					//In theory, by using this sequence of travelTo calls the robot should
					//not collide with any obstacle
					nav.travelTo(0,0);
					
					if(objectOrientation[0] > 45.0)
						nav.travelTo(0, 2*SQUARE_SIZE);
					else
						nav.travelTo(2*SQUARE_SIZE, 0);
					
					nav.travelTo(2*SQUARE_SIZE,2*SQUARE_SIZE);
					
					//Perform a second scan from the opposite corner of the board.
					//However this time the robot is expected to find only one
					//object: the blue styrofoam block (in front of the wooden block).
					nav.turnTo(180);
					mc.setMotorAbsoluteSpeeds(new int[] {-USLocalizer.ROTATION_SPEED, USLocalizer.ROTATION_SPEED}, MotorControl.BOTH_MOTORS);
					
					while(odo.getAng() <= 0 && objectCounter < 2){
						
						data = bd.getData();
						
						//If an object is found within 60 cm
						if(data.obstacleDistance < 60){
							//Store the robot's current orientation
							objectOrientation[objectCounter] = odo.getAng();
							objectCounter ++;
						}
					}
					mc.stopMotors();
					
					if(objectOrientation[0] != 2){
						//If the robot has not found 2 objects at this point
						//the routine has failed
						for(int i=0; i<3; i++)
							Sound.buzz();
						return;
					}
					else{
						//The robot has now found the styrofoam block and the
						//path to the destination (its current location)
						//is clear for sure.
						
						//Make the robot drive to the block, grab it, and drive
						//back to its final destination
						nav.turnTo(objectOrientation[1]);
						
						mc.setMotorAbsoluteSpeeds(new int[] {USLocalizer.ROTATION_SPEED, USLocalizer.ROTATION_SPEED}, MotorControl.BOTH_MOTORS);
						
						do{
						data = bd.getData();
						} while(!data.objectDetected);
						
						mc.stopMotors();
						
						grabBlock();
						
						nav.travelTo(5*SQUARE_SIZE/2,5*SQUARE_SIZE/2);
					}
					
				}
				
				else{
					//The robot has found the wooden block, thus
					//the other object it detected is the styrofoam block.
					//The path from the styrofoam block to the robot's
					//final destination should be clear.
					
					nav.travelTo(0,0);
					
					nav.turnTo(objectOrientation[1]);
					
					mc.setMotorAbsoluteSpeeds(new int[] {USLocalizer.ROTATION_SPEED, USLocalizer.ROTATION_SPEED}, MotorControl.BOTH_MOTORS);
					
					do{
					data = bd.getData();
					} while(!data.objectDetected);
					
					mc.stopMotors();
					
					grabBlock();
					
					nav.travelTo(5*SQUARE_SIZE/2,5*SQUARE_SIZE/2);
				}
			}
		}
		
		for(int i=0; i<3; i++)
			Sound.beep();
	}
	
	//A routine to grab a block in front of the robot
	@SuppressWarnings("resource")
	private static void grabBlock(){
		BlockDetection bd = new BlockDetection();
		MotorControl mc = MotorControl.getInstance();
		BlockDetection.Data data; 
		
		//Move towards the block until it is within claw range (3cm)
		mc.setMotorAbsoluteSpeeds(new int[] {USLocalizer.ROTATION_SPEED, USLocalizer.ROTATION_SPEED}, MotorControl.BOTH_MOTORS);
		
		do{
		data = bd.getData();
		} while(data.obstacleDistance > CLAW_RANGE);
		
		mc.stopMotors();
		
		//Lower the claw and capture the block
		(new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"))).rotate(90);
	}
}
