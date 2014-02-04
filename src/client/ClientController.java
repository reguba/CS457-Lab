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
import java.util.Random;

import javax.swing.JTextArea;

import utils.Utils;

/**
 * The ClientController contains all of the methods required
 * to request and receive files from a server.
 * 
 * @author Eric Ostrowski, Alex Schuitema, Austin Anderson
 *
 */
class ClientController implements Runnable	{

	//Rules of engagement:
	
	//Client sends request for a file
	//Server acknowledges receipt of request with either file not found or
	//a packet indicating how many packets will be send to transfer the file
	//The client will then listen for that many packets and send back acknowledgments
	//for each packet received.
	
	//Step 1: File request and file size are confirmed
	
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
	
	private DatagramSocket clientSocket;
	private JTextArea diagLog;
	private String ipAddress;
	private int port;
	private String filename;
	private boolean packetSkip;
	
	public ClientController(String filename, String ipAddress, int port, boolean packetSkip, JTextArea log) throws SocketException	{
		this.diagLog = log;
		this.ipAddress = ipAddress;
		this.port = port;
		this.packetSkip = packetSkip;
		this.filename = filename;
		clientSocket = new DatagramSocket();
		clientSocket.setSoTimeout(1500);		
	}
	
	/**
	 * Requests a file from the server at the specified IP and port.
	 * @param fileName The full name of the file being requested.
	 * @param ipAddress The IP address of the server.
	 * @param port The port number the server is operating on.
	 * @param filename The name of the file to be transfered.
	 * @param packetSkip Indicates whether or not the client should simulate packet loss.
	 * @throws UnknownHostException When IP address of the host cannot be determined.
	 * @throws SocketException When the socket could not be opened or the designated port couldn't be bound.
	 * @throws IOException When the socket is unable to either receive or send data.
	 */
	private void requestFile(String filename, String ipAddress, int port, boolean packetSkip) throws UnknownHostException, SocketException, IOException {
		
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
		receiveFile(filename, getFileRequestAcknowledgment(), ipAddress, port, packetSkip);
	}
	
	/**
	 * Begins to receive the file specified from the server and writes the file
	 * to the client's current working directory.
	 * @param filename The name of the that is expected.
	 * @param numberOfBytes The size of the file expected in bytes.
	 * @param ipAddress The IP address of the server.
	 * @param port The port the server is operating on.
	 * @param packetSkip Indicates whether or not the client should simulate packet loss.
	 * @throws IOException when the client fails to receive the file.
	 */
	private void receiveFile(String filename, long numberOfBytes, String ipAddress, int port, boolean packetSkip) throws IOException	{
		
		FileOutputStream outputFile = new FileOutputStream(new File(filename));
		FileDescriptor fd = outputFile.getFD();
		DatagramPacket filePacket;
		
		int numberOfPackets = Utils.getNumberOfPacketsToSend(numberOfBytes);
		long byteCount = 0;
		long bytesToWrite = 1020;
		
		Random rng = new Random();
		
		for(int i = 0; i < numberOfPackets; i++)	{
			
			try {
				
				//If we are simulating packet miss then randomly start to miss packets
				//and wait for a bit.
				if(packetSkip && rng.nextBoolean())	{
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					throw new SocketTimeoutException();
				}
				
				filePacket = Utils.receivePacket(clientSocket, diagLog);
				
				//Only acknowledge the packet if it's what we were expecting.
				if(!wasExpectedPacketReceived(i, filePacket))	{
					throw new SocketTimeoutException();
				}
				
				sendFilePacketAcknowledgment(i, ipAddress, port);
				diagLog.append("Recieved packet: " + (i + 1) + "\n");
				
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
				diagLog.append("Waiting for packet: " + (i + 1) + "\n");
				i--; //Wait for packet again
				e.printStackTrace();
			}
		}
		
		outputFile.close();
		diagLog.append("Finished receiving: " + filename + "\n");
	}
	
	/**
	 * Returns true if the packet specified has the same packet number as
	 * the packet number specified.
	 * @param packetNumber The number of the packet expected.
	 * @param received The packet to be checked.
	 */
	private boolean wasExpectedPacketReceived(int packetNumber, DatagramPacket received)	{
		ByteBuffer buff = ByteBuffer.wrap(received.getData(), 0, Integer.SIZE);
		return (buff.getInt() == packetNumber);
	}
	
	/**
	 * Send a packet acknowledging the receipt of a specific packet.
	 * @param packetNumber The number of the packet received.
	 * @param ipAddress The IP address of the server.
	 * @param port The port the server is operating on.
	 * @throws IOException when the packet fails to send.
	 * @throws UnknownHostException when the IP address cannot be resolved.
	 */
	private void sendFilePacketAcknowledgment(int packetNumber, String ipAddress, int port) throws UnknownHostException, IOException	{
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff.putInt(packetNumber);
		Utils.sendPacket(clientSocket, buff.array(), InetAddress.getByName(ipAddress), port);
	}

	/**
	 * Waits for a file request acknowledgment from the server to determine
	 * if the server will service the request.
	 * @return The size of the file requested in bytes.
	 * @throws IOException if the server has indicated that the file cannot be found.
	 */
	private long getFileRequestAcknowledgment() throws IOException {
		
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
	 * @throws IOException when the packet is unable to be sent.
	 */
	private void sendFileRequestPacket(String fileName, String ipAddress, int port) throws IOException {
		diagLog.append("Requesting file: " + fileName + "\n");
		diagLog.append("From: " + ipAddress + " : " + Integer.toString(port) + "\n");
		
		byte[] nameData = new byte[1024];
		nameData = fileName.getBytes();
		Utils.sendPacket(clientSocket, nameData, ipAddress, port);
	}
	
	/**
	 * Ensures the client shuts down appropriately.
	 * Should be called before the client terminates
	 * if the initialize method has been called.
	 */
	public void killClient() {
		clientSocket.close();
	}

	@Override
	public void run() {
		try {
			requestFile(filename, ipAddress, port, packetSkip);
		} catch (UnknownHostException e1) {
			diagLog.append("Unable to determine local IP address\n");
			e1.printStackTrace();
		} catch (IOException e1) {
			diagLog.append(e1.getMessage() + "\n");
			e1.printStackTrace();
		}
		
		clientSocket.close();
	}
}
