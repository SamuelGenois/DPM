package dpm.launcher;

import dpm.util.RemoteBrickManager;

public class Disconnect {
	
	public static void main(String[] args){
		
		RemoteBrickManager.getSlave();
		RemoteBrickManager.closeSlave();
	}
}
