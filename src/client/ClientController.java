package client;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import javax.swing.JTextArea;

import utils.Utils;


class ClientController{

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
	
	private static DatagramSocket clientSocket;
	private static JTextArea diagLog;
	
	public static void initializeClient(JTextArea log) throws SocketException {
		diagLog = log;
		
		clientSocket = new DatagramSocket();
		clientSocket.setSoTimeout(1500);
	}
	
	/**
	 * Requests a file from the server at the specified IP and port.
	 * @param fileName The full name of the file being requested.
	 * @param ipAddress The IP address of the server.
	 * @param port The port number the server is operating on.
	 * @throws UnknownHostException When IP address of the host cannot be determined.
	 * @throws SocketException When the socket could not be opened or the designated port couldn't be bound.
	 * @throws IOException When the socket is unable to either receive or send data.
	 */
	public static void requestFile(String filename, String ipAddress, int port) throws UnknownHostException, SocketException, IOException {
		
		if(Utils.isNullOrEmptyString(filename)) {
			throw new IOException("Invalid file name");
		}
		
		if(Utils.isNullOrEmptyString(ipAddress))	{
			throw new IOException("Invalid IP address");
		}
		
		if(port < 1 || port > 65535) {
			throw new IOException("Invalid port number");
		}
		
		sendFileRequestPacket(filename, ipAddress, port);
		receiveFile(filename, getFileRequestAcknowledgment(), ipAddress, port);
	}
	
	private static void receiveFile(String filename, long numberOfBytes, String ipAddress, int port) throws IOException	{
		
		FileOutputStream outputFile = new FileOutputStream(new File(filename));
		FileDescriptor fd = outputFile.getFD();
		DatagramPacket filePacket;
		
		int numberOfPackets = Utils.getNumberOfPacketsToSend(numberOfBytes);
		long byteCount = 0;
		long bytesToWrite = 1020;
		
		for(int i = 0; i < numberOfPackets; i++)	{
			
			try {
				
				filePacket = Utils.receivePacket(clientSocket, diagLog);
				sendFilePacketAcknowledgment(i, ipAddress, port);
				ByteBuffer buff = ByteBuffer.wrap(filePacket.getData(), 4, 1020);
				
				if((byteCount + 1020) > numberOfBytes)	{
					bytesToWrite = numberOfBytes - byteCount; //Last packet may not be full
				}
				
				outputFile.write(buff.array(), 4, (int) bytesToWrite);
				outputFile.flush();
				
				//Every 10KB-ish force it to write to disk
				if(byteCount % 10200 == 0)	{
					fd.sync();
				}
				
				byteCount += bytesToWrite;
				
			} catch (SocketTimeoutException e) {
				diagLog.append("Waiting for packet: " + i + "\n");
				i--; //Wait for packet again
				e.printStackTrace();
			}
		}
		
		outputFile.close();
		diagLog.append("Finished receiving: " + filename + "\n");
	}
	
	/**
	 * Send a packet acknowledging the receipt of a specific packet.
	 * @param packetNumber The number of the packet received.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	private static void sendFilePacketAcknowledgment(int packetNumber, String ipAddress, int port) throws UnknownHostException, IOException	{
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff.putInt(packetNumber);
		Utils.sendPacket(clientSocket, buff.array(), InetAddress.getByName(ipAddress), port);
	}

	private static long getFileRequestAcknowledgment() throws IOException {
		
		long numberOfBytes = 0;
		
		DatagramPacket acknowledgment = Utils.receivePacket(clientSocket, diagLog);
		
		ByteBuffer buff = ByteBuffer.wrap(acknowledgment.getData(), 0, Long.SIZE);
		numberOfBytes = buff.getLong();
		
		if(numberOfBytes == -1) {
			throw new IOException("File not found");
		}
		
		diagLog.append("KB expected: " + (numberOfBytes / 1024.0) + "\n");
		
		return numberOfBytes;
	}
	
	/**
	 * Sends a packet to the server requesting a specific file.
	 * @param fileName The name of the file being requested.
	 * @param ipAddress The IP address of the server.
	 * @param port The port the server is operating on.
	 * @throws IOException When the packet is unable to be sent.
	 */
	private static void sendFileRequestPacket(String fileName, String ipAddress, int port) throws IOException {
		diagLog.append("Requesting file: " + fileName + "\n");
		diagLog.append("From: " + ipAddress + " : " + Integer.toString(port) + "\n");
		byte[] nameData = new byte[1024];
		nameData = fileName.getBytes();
		sendPacket(nameData, ipAddress, port);
	}
	

	private static void sendPacket(byte[] data, String ipAddress, int port) throws IOException {
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(ipAddress), port);
		clientSocket.send(sendPacket);
	}
	
	public static void killClient() {
		clientSocket.close();
	}
}
