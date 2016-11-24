package dpm.launcher;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import dpm.repository.Repository;
import dpm.util.DPMConstants;
import dpm.util.Printer;
import lejos.hardware.Button;

/**
 * Class that executes the tasks required in the final demo
 * 
 * @author Samuel Genois
 *
 */
public class FinalDemo implements Launcher, DPMConstants{

	private static final long FOUR_MINUTES = 240000l;
	
	private final int[] greenZone, redZone;
	private final int startingCorner, role;
	
	/**
	 * The main method
	 */
	public static void main (String[] args){
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
			System.exit(0);
			}
		}).start();
		
		Printer.getInstance().display("Ready");
		
		Button.waitForAnyPress();
		
		Printer.getInstance().display("Running");
		
		//A launcher subsystem holding round specific data is created 
		Launcher launcher = createFinalDemo();
		
		//The repository is given a reference to a launcher subsystem
		Repository.launch(launcher);
		
		//The localization routine is performed
		Repository.localize();
		
		//The block searching routine is performed
		Repository.search();
		
		//After 4 minutes, the finalization routine is performed.
		(new Timer()).schedule(new TimerTask() {
			public void run() {
				Repository.doFinalization();
				Printer.getInstance().display("Finished");
			}
		}, FOUR_MINUTES);
	}
	
	private static FinalDemo createFinalDemo(){
		try{
			return new FinalDemo();
		}
		catch(Exception e){
			return createFinalDemo();
		}
	}
	
	private FinalDemo() throws Exception{
		
		HashMap<String, Integer> wifiData = new WifiConnect().ConnectWifi();
		
		greenZone = new int[] {wifiData.get("LGZx"), wifiData.get("UGZy"), wifiData.get("UGZx"), wifiData.get("LGZy")};
		redZone = new int[] {wifiData.get("LRZx"), wifiData.get("URZy"), wifiData.get("URZx"), wifiData.get("LRZy")};
		startingCorner = wifiData.get("BSC");
		
		if(wifiData.get("BTN") == TEAM_NUMBER)
			role = BUILDER;
		else if(wifiData.get("CTN") == TEAM_NUMBER)
			role = GARBAGE_COLLECTOR;
		else
			throw new Exception();
	}
	
	
	@Override
	public int[] getGreenZone() {
		return greenZone;
	}

	@Override
	public int[] getRedZone() {
		return redZone;
	}

	@Override
	public int getStartZone() {
		return startingCorner;
	}

	@Override
	public int getRole() {
		return role;
	}
	
}
