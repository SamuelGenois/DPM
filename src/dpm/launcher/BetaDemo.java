package dpm.launcher;

import dpm.repository.Repository;
<<<<<<< HEAD
=======
import dpm.util.DPMConstants;
>>>>>>> origin/sam
import dpm.util.Printer;
import lejos.hardware.Button;
//import lejos.hardware.Sound;

import java.util.HashMap;

/**
 * Class that executes the tasks required in the beta demo
 * <br>Gets Wifi data and calculates coordinates of green zone
 * <br>Starts the preliminary block search algorithm
 * <br>Once a block is found, travels to green zone and drops it
 * @author Emile Traoré
 *
 */
<<<<<<< HEAD
public class BetaDemo{
	private static boolean wifiAcquired = false;
	private static int GZx, GZy;
=======
public class BetaDemo implements Launcher, DPMConstants{
	//private static boolean runningDemo = true;
	//private static boolean wifiAcquired = false;
	
	private final int[] greenZone, redZone;
	private final int startingCorner, role;
	
	//private static int GZx, GZy;
>>>>>>> origin/sam
	public static void main (String[] args){
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
			System.exit(0);
			}
		}).start();
<<<<<<< HEAD
=======
		/*
		(new Thread() {
			public void run() {
				while (!wifiAcquired){
				}
				try{Thread.sleep(10000);}catch (Exception e){}
				Sound.buzz();
				runningDemo = false;
				Repository.doFinalization(GZx, GZy);
			}
		}).start();
		*/
>>>>>>> origin/sam
		
		Launcher launcher = new BetaDemo();
		
		while(launcher.getRole() == NO_ROLE){
			launcher = new BetaDemo();
		}
		
		Printer.getInstance().display("Ready");
		Button.waitForAnyPress();
		
		Repository.launch(launcher);
		Repository.localize();
		
		(new Thread() {
			public void run() {
				while (!Repository.clawIsEmpty()){
					try{Thread.sleep(1000l);}catch (Exception e){}
				}
				Repository.doFinalization();
			}
		}).start();
	}
	
	private BetaDemo() {
		
		HashMap<String, Integer> wifiData = new WifiConnect().ConnectWifi();
		
		//For Testing?
		//wifiAcquired = true;
		
		greenZone = new int[] {wifiData.get("LGZx"), wifiData.get("UGZy"), wifiData.get("UGZx"), wifiData.get("LGZy")};
		redZone = new int[] {wifiData.get("LRZx"), wifiData.get("URZy"), wifiData.get("URZx"), wifiData.get("LRZy")};
		startingCorner = wifiData.get("BSC");
		
		if(wifiData.get("BTN") == TEAM_NUMBER)
			role = BUILDER;
		else if(wifiData.get("CTN") == TEAM_NUMBER)
			role = BUILDER;
		else
			role = NO_ROLE;
	}
	
	//Code to delete
	/*
		//Determine the coordinates of green zone based on wifi data
		int initialX, initialY;
		int startCorner = wifiData.get("BSC");
		GZx = (wifiData.get("LGZx")+wifiData.get("UGZx"))/2;
		GZy = (wifiData.get("LGZy")+wifiData.get("UGZy"))/2;
		if (startCorner == 1){
			initialX = 0;
			initialY = 0;
			GZx = (initialX+GZx)*30;
			GZy = (initialY+GZy)*30;
		}
		else if (startCorner == 2){
			//6 should be 10 for competition
			initialX = 6;
			initialY = 0;
			GZx = (initialY+GZy)*30;
			GZy = (initialX-GZx)*30;
		}
		else if (startCorner == 3){
			//6 should be 10 for competition
			initialX = 6;
			initialY = 6;
			GZx = (initialX-GZx)*30;
			GZy = (initialY-GZy)*30;
		}
		else if (startCorner == 4){
			//6 should be 10 for competition
			initialX = 0;
			initialY = 6;
			GZx = (initialY-GZy)*30;
			GZy = (initialX+GZx)*30;
		}
		Printer.getInstance().display("\n\n\n"+GZx+"\n"+GZy);
		//Repository.localize();
<<<<<<< HEAD
		//Sound.beep();
		//Repository.search();
		//Repository.doFinalization(GZx, GZy);
=======
		while (runningDemo){
			Repository.travelTo(0,60);
			Repository.travelTo(60,60);
			Repository.travelTo(60,0);
			Repository.travelTo(0,0);
		}
	
	}
	*/
	
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
>>>>>>> origin/sam
	}
}
