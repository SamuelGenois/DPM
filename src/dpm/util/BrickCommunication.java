package dpm.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import lejos.hardware.BrickInfo;

/**
 * This class holds functionality that should be part of the BrickFinder
 * class in the leJOS library according to the leJOS library API. However, for some
 * unknown reason Eclipse fails to recognize the functionality we need, thus we
 * had no choice but to copy it into the project. 
 * 
 * @author leJOS library
 */
public class BrickCommunication {
	
	private static final int DISCOVERY_PORT = 3016;
	private static final String FIND_CMD = "find";
	private static final int MAX_DISCOVERY_TIME = 2000;
	private static final int MAX_PACKET_SIZE = 32;
	private static final int MAX_HOPS = 2;
	
	
	/**
	 * This method returns a list of all the bricks the
	 * local brick establishes contact with that have the
	 * specified name.
	 * 
	 * @param name the name of the brick
	 * @return an array of bricks with that name
	 */
	 public static BrickInfo[] find(String name)
		{
	        DatagramSocket socket=null;
	        Map<String,BrickInfo> ev3s = new HashMap<String,BrickInfo>();
	        try
	        {
	            socket = new DatagramSocket();
	            socket.setBroadcast(true);
	            boolean findAll = name.equalsIgnoreCase("*");
	            broadcastFindRequest(socket, name, null, -1, MAX_HOPS);
	            socket.setSoTimeout(MAX_DISCOVERY_TIME/4);
	            DatagramPacket packet = new DatagramPacket (new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
	    
	            long start = System.currentTimeMillis();
	            
	            while ((System.currentTimeMillis() - start) < MAX_DISCOVERY_TIME) {
	                try {
	                    socket.receive (packet);
	                    String message = new String(packet.getData(), "UTF-8").trim();
	                    if (findAll || message.equalsIgnoreCase(name))
	                    {
	                        String ip = packet.getAddress().getHostAddress();
	                        ev3s.put(ip, new BrickInfo(message.trim(),ip,"EV3"));
	                    }
	                } catch (SocketTimeoutException e)
	                {
	                    // No response ask again
	                    broadcastFindRequest(socket, name, null, -1, MAX_HOPS);                    
	                }
	            }
	        } catch (IOException ex)
	        {
	            System.err.println("Exception opening socket : " + ex);
	        } finally
	        {
	            if (socket != null)
	                socket.close();
	        }
	        BrickInfo[] devices = new BrickInfo[ev3s.size()];
	        int i = 0;
	        
	        for(String ev3: ev3s.keySet()) {
	            devices[i++] = ev3s.get(ev3);
	        }        
	        return devices;
		}
	    
	    private static void broadcastFindRequest(DatagramSocket socket, String name, InetAddress replyAddr, int replyPort, int hop)
		{
	        try
	        {
	            // Broadcast the message over all the network interfaces
	            Enumeration<NetworkInterface> interfaces = NetworkInterface
	                    .getNetworkInterfaces();
	            while (interfaces.hasMoreElements())
	            {
	                NetworkInterface networkInterface = (NetworkInterface) interfaces
	                        .nextElement();

	                if (networkInterface.isLoopback() || !networkInterface.isUp())
	                {
	                    continue; // Don't want to broadcast to the loopback
	                              // interface
	                }

	                for (InterfaceAddress interfaceAddress : networkInterface
	                        .getInterfaceAddresses())
	                {
	                    InetAddress broadcast = interfaceAddress.getBroadcast();
	                    if (broadcast == null)
	                        continue;
	                    String message = FIND_CMD + " " + name;
	                    if (replyAddr == null)
	                        message += " " + interfaceAddress.getAddress().getHostAddress() + " " + socket.getLocalPort();
	                    else
	                        message += " " + replyAddr.getHostAddress() + " " + replyPort;
	                    message += " " + hop;
	                        
	                    byte[] sendData = message.getBytes();

	                    // Send the broadcast packet.
	                    try
	                    {
	                        //System.out.println("Send to " + broadcast.getHostAddress() + " port " + DISCOVERY_PORT );
	                        DatagramPacket sendPacket = new DatagramPacket(
	                                sendData, sendData.length, broadcast, DISCOVERY_PORT);
	                        socket.send(sendPacket);
	                    } catch (Exception e)
	                    {
	                        System.err
	                                .println("Exception sending to : "
	                                        + networkInterface.getDisplayName()
	                                        + " : " + e);
	                    }
	                }
	            }
	        } catch (IOException ex)
	        {
	            System.err.println("Exception opening socket : " + ex);
	        }	    
		}
}
