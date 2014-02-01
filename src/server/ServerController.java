package server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import javax.swing.JTextArea;

import utils.Utils;

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
	private static JTextArea diagLog;
	private static final double MAX_DATA = 1020.0;
	
	public static void acceptRequest(JTextArea log) throws IOException	{
		
		diagLog = log;
		
		try {
			serverSocket = new DatagramSocket(9876);
			diagLog.append("Opened socket on port 9876\n");
		
		} catch (SocketException e) {
			diagLog.append(e.getMessage() + "\n");
			e.printStackTrace();
		}
		
		while(true) {
			
			//Listen for requests
			//Receive a file request and acknowledge it
			DatagramPacket request = Utils.receivePacket(serverSocket, diagLog);			
			
			try {
				
				sendFileRequestAcknowledgment(request);
			
			} catch (IOException e) {
				diagLog.append(e.getMessage() + "\n");
				e.printStackTrace();
				sendBadFileAcknowledgment(request);				
			}
		}
	}
	
	private static File getFile(String filename) throws IOException {
		
		if(Utils.isNullOrEmptyString(filename)) {
			throw new IOException("Client did not specify filename");
		}
		
		File file = new File(filename);
		
		if(!file.exists())	{
			throw new IOException("File not found");
		}
		
		if(file.isDirectory()) {
			throw new IOException("Client requested directory");
		}
		
		return file;
	}
	
	private static int getNumberOfPacketsToSend(File file) {
		double toReturn = Math.ceil(file.length() / MAX_DATA);
		return (int) Math.ceil(file.length() / MAX_DATA);
	}
	
	private static void sendBadFileAcknowledgment(DatagramPacket request) throws IOException	{
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff.putInt(0); //Tell the client no packets are coming
		Utils.sendPacket(serverSocket, buff.array(), request.getAddress(), request.getPort());
	}
	
	private static void sendFileRequestAcknowledgment(DatagramPacket request) throws IOException {
		
		String filename = new String(request.getData()).trim();
		
		diagLog.append("Received request for: " + filename + "\n");
		diagLog.append("From: " + request.getAddress().toString() + " : " + Integer.toString(request.getPort()) + "\n");
		File file = getFile(filename);
		
		//Determine if file exists...
		//Figure out how many packets it will take to send
		//Send acknowledgment back to client
		
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff.putInt(getNumberOfPacketsToSend(file)); //TODO replace with actual value 
		Utils.sendPacket(serverSocket, buff.array(), request.getAddress(), request.getPort());
		
	}
	
	/**
	 * This method should be called when the server is being shut down.
	 */
	public static void killServer() {
		serverSocket.close();
	}
}
