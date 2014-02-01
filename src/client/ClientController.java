package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
	public static void requestFile(String fileName, String ipAddress, int port) throws UnknownHostException, SocketException, IOException {
		
		if(Utils.isNullOrEmptyString(fileName)) {
			throw new IOException("Invalid file name");
		}
		
		if(Utils.isNullOrEmptyString(ipAddress))	{
			throw new IOException("Invalid IP address");
		}
		
		if(port < 1 || port > 65535) {
			throw new IOException("Invalid port number");
		}
		
		sendFileRequestPacket(fileName, ipAddress, port);
		getFileRequestAcknowledgment();
		
	}
	

	private static int getFileRequestAcknowledgment() throws IOException {
		
		int numberOfPackets = 0;
		
		DatagramPacket acknowledgment = Utils.receivePacket(clientSocket, diagLog);
		
		ByteBuffer buff = ByteBuffer.wrap(acknowledgment.getData(), 0, Integer.SIZE);
		numberOfPackets = buff.getInt();
		
		if(numberOfPackets == 0) {
			throw new IOException("File not found");
		}
		
		diagLog.append("Packets expected: " + numberOfPackets + "\n");
		
		return numberOfPackets;
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
