package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import javax.swing.JTextArea;

import utils.Utils;

/**
 * The ServerController contains methods required
 * to listen for file requests from clients and
 * to service those requests.
 * 
 * @author Eric Ostrowski, Alex Schuitema, Austin Anderson
 *
 */

public class ServerController {
	
	//Rules of engagement:

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
	//0 - 7 -- Number of bytes to be sent (-1 if file not found)
	
	//Step 2: Server receives client acceptance, begins to send packets
	
	//Server data packet structure
	//1024 bytes
	//0 - 3 -- PacketNumber
	//4 - 1023 -- Data
	
	//Client acknowledgment packet
	//4 bytes
	//0 - 3 -- Packet number received
	
	private static DatagramSocket serverSocket;
	private static JTextArea diagLog;
	
	/**
	 * Sets the ServerController to listen for incoming file requests
	 * from a client.
	 * @param log The UI component on which diagnostic information should be displayed.
	 * @throws IOException when an error occurs while receiving a file request packet.
	 */
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
			serverSocket.setSoTimeout(0);
			//Listen for requests
			//Receive a file request and acknowledge it
			DatagramPacket request = Utils.receivePacket(serverSocket, diagLog);			
			
			try {
				
				File file = sendFileRequestAcknowledgment(request);
				sendFile(file, request);
				
			} catch (IOException e) {
				diagLog.append(e.getMessage() + "\n");
				e.printStackTrace();
				sendBadFileAcknowledgment(request);	
			}
		}
	}
	
	/**
	 * Gets the File object that represents the file specified.
	 * @param filename The path of the file.
	 * @return The File object representing the file specified.
	 * @throws IOException when the path is empty or null, if the file could not be found
	 * or if the file is a directory.
	 */
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
	
	/**
	 * Reads the specified file and begins sending packets to the client.
	 * @param file The file to be sent to the client.
	 * @param request The packet containing the original file request from the client.
	 * @throws IOException when the file either cannot be read or fails to send.
	 */
	private static void sendFile(File file, DatagramPacket request) throws IOException {
		int numberOfPackets = Utils.getNumberOfPacketsToSend(file);
		
		FileInputStream inputStream = new FileInputStream(file);
		
		byte[] packetData;
		ByteBuffer buff;
		
		//Set the time for acknowledgments to come in
		serverSocket.setSoTimeout(1500);
		
		for(int i = 0; i < numberOfPackets; i++)	{
			
			packetData = new byte[1024];
			
			//First four bytes are the packet number
			buff = ByteBuffer.wrap(packetData, 0, 4);
			buff.putInt(i);
			
			//Last 1020 bytes are the data
			inputStream.read(packetData, 4, 1020);
			
			Utils.sendPacket(serverSocket, packetData, request.getAddress(), request.getPort());
			diagLog.append("Sending packet: " + (i + 1) + "/" + numberOfPackets + "\n");
			
			//As long as we cannot confirm packet was received, re-send it
			while(!wasPacketReceived(i))	{
				Utils.sendPacket(serverSocket, packetData, request.getAddress(), request.getPort());
			}
		}
		
		inputStream.close();
	}
	
	/**
	 * Verifies with the client that the packet specified was received.
	 * @param packetNumber The number of the packet sent to the client.
	 * @return True if an acknowledgment was received for the packet specified, false otherwise.
	 * @throws IOException when the acknowledgment could not be received.
	 */
	private static boolean wasPacketReceived(int packetNumber) throws IOException {
		
		try {
			
			DatagramPacket acknowledgment = Utils.receivePacket(serverSocket, diagLog);
			ByteBuffer buff = ByteBuffer.wrap(acknowledgment.getData());
			
			//If the acknowledgment is inaccurate, consider it bad
			if(buff.getInt() != packetNumber)	{
				return false;
			}
			
		} catch (SocketTimeoutException e) {
			diagLog.append("No acknowledgment for packet: " + packetNumber + "\n");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sends an acknowledgment to the client indicating that the file
	 * requested could not be found.
	 * @param request The packet containing the original file request from the client.
	 * @throws IOException when the packet is unable to be sent.
	 */
	private static void sendBadFileAcknowledgment(DatagramPacket request) throws IOException	{
		ByteBuffer buff = ByteBuffer.allocate(8);
		buff.putLong(-1); //Tell the client no data is coming
		Utils.sendPacket(serverSocket, buff.array(), request.getAddress(), request.getPort());
	}
	
	/**
	 * Sends an acknowledgment to the client indicating that the file request
	 * will be serviced and the size of the file to be transfered in bytes.
	 * @param request The packet containing the original file request from the client.
	 * @return The File object representing the file requested by the client.
	 * @throws IOException when the file cannot be obtained or the packet cannot be sent.
	 */
	private static File sendFileRequestAcknowledgment(DatagramPacket request) throws IOException {
		
		String filename = new String(request.getData()).trim();
		
		diagLog.append("Received request for: " + filename + "\n");
		diagLog.append("From: " + request.getAddress().toString() + " : " + Integer.toString(request.getPort()) + "\n");
		File file = getFile(filename);
		
		ByteBuffer buff = ByteBuffer.allocate(8);
		buff.putLong(file.length());
		Utils.sendPacket(serverSocket, buff.array(), request.getAddress(), request.getPort());
		
		return file;
	}
	
	/**
	 * This method should be called when the server is being shut down.
	 */
	public static void killServer() {
		serverSocket.close();
	}
}
