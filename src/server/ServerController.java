package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Interacts with the ClientController
 * for file transfer.
 * 
 * @author Eric Ostrowski
 *
 */

public class ServerController {
	
	public ServerController() {
		//Construct
	}
	
	public static void connect() throws Exception {
		
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		
		DatagramSocket ServerSocket = new DatagramSocket(9876);		  
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
		ServerSocket.receive(receivePacket);
		
		String clientMessage = new String(receivePacket.getData());
		System.out.println("Got from Client: " + clientMessage);
		
		InetAddress IPAddress = receivePacket.getAddress();
		int returnPort = receivePacket.getPort();
		
		sendData = "Confirmed".getBytes();
		
		DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,returnPort);
		ServerSocket.send(sendPacket);
		
		ServerSocket.close();
	}
}
