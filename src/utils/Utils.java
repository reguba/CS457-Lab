package utils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import javax.swing.JTextArea;

/**
 * Any helpful utility methods go in here!
 * 
 * @author Eric Ostrowski, Alex Schuitema, Austin Anderson
 *
 */

public class Utils {
	
	//Max amount of bytes that can be in one packet
	private static final double MAX_DATA = 1020.0;
	
	/**
	 * Returns true if str is either null or the empty string.
	 */
	public static boolean isNullOrEmptyString(String str) {
		return (str == null || str.isEmpty());
	}
	
	/**
	 * Returns the number of packets required to send the specified file.
	 * @param file The file to be sent.
	 */
	public static int getNumberOfPacketsToSend(File file) {
		return getNumberOfPacketsToSend(file.length());
	}
	
	/**
	 * Returns the number of packets required to send the specified file.
	 * @param numberOfBytes The size of the file to send in bytes.
	 */
	public static int getNumberOfPacketsToSend(long numberOfBytes)	{
		return (int) Math.ceil(numberOfBytes / MAX_DATA);
	}
	
	/**
	 * Listens for a packet to be received.
	 * @param sock The socket on which the packet is expected.
	 * @param log The UI component to which diagnostic information should be displayed.
	 * @return The DatagramPacket that was received.
	 * @throws SocketTimeoutException when the socket timesout before receiving a packet.
	 * @throws IOException when an I/O error occurs.
	 */
	public static DatagramPacket receivePacket(DatagramSocket sock, JTextArea log) throws SocketTimeoutException, IOException {
		
		byte[] receiveData = new byte[1024];
		
		DatagramPacket receivedPacket = new DatagramPacket(receiveData,receiveData.length);
		sock.receive(receivedPacket);
		
		//log.append("Received packet from: " + receivedPacket.getAddress().toString() + " : " + Integer.toString(receivedPacket.getPort()) + "\n");
		
		return receivedPacket;
	}
	
	/**
	 * Sends a packet containing the specified data to the specified IP address
	 * and port number.
	 * @param sock The socket that the packet should be sent on.
	 * @param data The data to send in the packet.
	 * @param ipAddress The IP address of the destination.
	 * @param port The port that the destination is operating on.
	 * @throws IOException when an I/O error occurs.
	 */
	public static void sendPacket(DatagramSocket sock, byte[] data, InetAddress ipAddress, int port) throws IOException	{
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, port);
		sock.send(sendPacket);
	}
	
	/**
	 * Sends a packet containing the specified data to the specified IP address
	 * and port number.
	 * @param sock The socket that the packet should be sent on.
	 * @param data The data to send in the packet.
	 * @param ipAddress The IP address of the destination.
	 * @param port The port that the destination is operating on.
	 * @throws IOException when an I/O error occurs.
	 */
	public static void sendPacket(DatagramSocket sock, byte[] data, String ipAddress, int port) throws IOException {
		sendPacket(sock, data, InetAddress.getByName(ipAddress), port);
	}
}
