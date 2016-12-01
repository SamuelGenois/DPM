package dpm.util;

import java.io.IOException;
import lejos.remote.ev3.RemoteRequestEV3;

/**
 * This class contains the software interface of
 * the slave brick (a RemoteRequestEV3 object), 
 * public access to that interface and means to disconnect 
 * the slave brick when it is o longer needed 
 * (at the end of the programs execution)
 * 
 * @author Samuel Genois
 *
 */
public class RemoteBrickManager{
	
	static final int	MASTER = 0,
						SLAVE = 1;
	
	private static final String SLAVE_NAME = "EV3";
	
	private static RemoteRequestEV3 slave;
	
	/**
	 * Disconnects the slave brick and destroys the interface.
	 */
	public static void closeSlave(){
		if(slave != null)
			slave.disConnect();
		slave = null;
	}
	
	/**
	 * Returns the slave brick's interface
	 * 
	 * @return the slave brick
	 */
	public static RemoteRequestEV3 getSlave(){
		
		if(slave == null){
			
			try {
				slave = new RemoteRequestEV3(BrickCommunication.find(SLAVE_NAME)[0].getIPAddress());
			} catch (IOException e) {
				slave.toString();
			}
			
			if(slave == null)
				slave.toString();
		}
			
		return slave;
	}
}
