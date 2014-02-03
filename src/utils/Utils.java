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
 * @author Eric Ostrowski
 *
 */

/**
 * Returns true if the string is neither null or empty.
 * 
 * @author Eric Ostrowski
 *
 */
public class Utils {
	
	private static final double MAX_DATA = 1020.0;
	
	public static boolean isNullOrEmptyString(String str) {
		return (str == null || str.isEmpty());
	}
	
	public static int getNumberOfPacketsToSend(File file) {
		return getNumberOfPacketsToSend(file.length());
	}
	
	public static int getNumberOfPacketsToSend(long numberOfBytes)	{
		return (int) Math.ceil(numberOfBytes / MAX_DATA);
	}
	
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
	 * @param data The data to send in the packet.
	 * @param ipAddress The IP address of the destination.
	 * @param port The port destination is operating on.
	 * @throws IOException When the packet is unable to be sent.
	 */
	public static void sendPacket(DatagramSocket sock, byte[] data, InetAddress ipAddress, int port) throws IOException	{
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, port);
		sock.send(sendPacket);
	}
}
