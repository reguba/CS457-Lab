package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

	//Client sends request for a file
	//Server acknowledges receipt of request with either file not found or
	//a packet indicating how many packets will be send to transfer the file
	//The client will then listen for that many packets and send back acknowledgments
	//for each packet received.
	
	//Step 1: File request and packet counts are confirmed
	
	//Client file name request
	//1024 bytes
	//0 - 1023 -- File name
	
	//Server file request response packet
	//4 bytes
	//0 - 3 -- Number of packets to be sent (0 if file not found)
	
	//Client file accept response packet
	//4 bytes
	//0 - 3 -- Number of packets expected
	
	//Step 2: Server receives client acceptance, begins to send packets
	
	//Server UDP data packet structure
	//1024 bytes
	//0 - 3 -- PacketNumber
	//4 - 1023 -- Data
	
	//Client acknowledgment packet
	//4 bytes
	//0 - 3 -- Packet number received

/**
 * Interacts with the ClientController
 * for file transfer.
 * 
 * @author Eric Ostrowski
 *
 */

public class ServerController {
	
	private static DatagramSocket serverSocket;
	
	public static void connect() throws Exception {
		
		serverSocket = new DatagramSocket(9876);
		
		//Receive a file request and acknowledge it
		sendFileRequestAcknowledgment(receivePacket());
		
		serverSocket.close();
	}
	
	private static void sendFileRequestAcknowledgment(DatagramPacket request) throws IOException {
		
		//Determine if file exists...
		//Figure out how many packets it will take to send
		//Send acknowledgment back to client
		
		ByteBuffer buff = ByteBuffer.allocateDirect(4);
		//wrap a byte array
		buff.putInt(120); //TODO replace with actual value 
		//sendPacket(result, request.getAddress(), request.getPort());
		
	}
	
	private static void sendPacket(byte[] data, String ipAddress, int port) throws IOException {
		sendPacket(data, InetAddress.getByName(ipAddress), port);		
	}
	
	private static void sendPacket(byte[] data, InetAddress ipAddress, int port) throws IOException	{
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, port);
		serverSocket.send(sendPacket);
	}
	
	private static DatagramPacket receivePacket() throws IOException {
		
		byte[] receiveData = new byte[1024];
		
		DatagramPacket receivedPacket = new DatagramPacket(receiveData,receiveData.length);
		serverSocket.receive(receivedPacket);
		
		return receivedPacket;
	}
}
