package dpm.launcher;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Motors;
import dpm.util.Printer;
import dpm.util.RemoteBrickManager;
import dpm.util.Sensors;
import lejos.hardware.Button;

public class TestBlockSearch implements DPMConstants{
	
	public static void main(String[] args){
		
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
				RemoteBrickManager.closeSlave();
				System.exit(0);
			}
		}).start();
		
		Motors.getMotor(LEFT);
		Motors.getMotor(RIGHT);
		Motors.getMotor(LIFT);
		Motors.getMotor(CLAW);
		Motors.getMotor(SENSOR);
		Sensors.getSensor(COLOR_BLOCK_ID);
		Sensors.getSensor(COLOR_ODO_CORR);
		Sensors.getSensor(US_ACTIVE);
		Printer.getInstance().display("Press any button", 0, true);
		Button.waitForAnyPress();
		Printer.getInstance().display("Running", 0, false);
		
		Repository.search();
		
		Printer.getInstance().display("Finished", 0, false);
	}
}
