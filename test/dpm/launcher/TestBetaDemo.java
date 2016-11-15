package dpm.launcher;

import dpm.repository.Repository;
import lejos.hardware.Button;
import lejos.hardware.Sound;

import java.util.HashMap;

public class TestBetaDemo{
	private static boolean runningDemo = true;
	private static boolean wifiAcquired = false;
	private static int GZx, GZy;
	public static void main (String[] args){
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE){}
			System.exit(0);
			}
		}).start();
		(new Thread() {
			public void run() {
				while (!wifiAcquired){
				}
				try{Thread.sleep(30000);}catch (Exception e){}
				Sound.buzz();
				runningDemo = false;
				Repository.doFinalization(GZx, GZy);
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
		//Repository.localize();
		while (runningDemo){
			Repository.travelTo(0,60);
			Repository.travelTo(60,60);
			Repository.travelTo(60,0);
			Repository.travelTo(0,0);
		}
	}
}
