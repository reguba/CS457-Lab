package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import utils.Utils;


class ClientController{

	private static DatagramSocket clientSocket;
	
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
		
		clientSocket = new DatagramSocket();
		
		byte[] sendData = new byte[1024];
		sendData = fileName.getBytes();
		
		sendPacket(sendData, ipAddress, port);
		DatagramPacket receivedPacket = receivePacket();
		
		String serverMessage = new String(receivedPacket.getData());
		
		clientSocket.close();
		
	}
	
	private static void sendPacket(byte[] data, String ipAddress, int port) throws IOException {
		
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(ipAddress), port);
		clientSocket.send(sendPacket);
	}
	
	private static DatagramPacket receivePacket() throws IOException {
		
		byte[] receiveData = new byte[1024];
		
		DatagramPacket receivedPacket = new DatagramPacket(receiveData,receiveData.length);
		clientSocket.receive(receivedPacket);
		
		return receivedPacket;
	}
}
