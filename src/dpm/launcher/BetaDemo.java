package dpm.launcher;

import dpm.repository.Repository;
import lejos.hardware.Button;
import lejos.hardware.Sound;

import java.util.HashMap;

/**
 * Class that executes the tasks required in the beta demo
 * <br>Gets Wifi data and calculates coordinates of green zone
 * <br>Starts the preliminary block search algorithm
 * <br>Once a block is found, travels to green zone and drops it
 * @author Emile Traoré
 *
 */
public class BetaDemo{
	private static boolean wifiAcquired = false;
	private static int GZx, GZy;
	public static void main (String[] args){
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
			System.exit(0);
			}
		}).start();
		
		HashMap<String, Integer> wifiData = Repository.getWifiData();
		wifiAcquired = true;
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
		Repository.localize();
		Sound.beep();
		Repository.search();
		Repository.doFinalization(GZx, GZy);
	}
}
