package udp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class udpclient{
  public static void main(String args[]) throws Exception{
	  
	  //ClientGui window = new ClientGui();
	  //window.setVisible(true);
	  
	  DatagramSocket clientSocket = new DatagramSocket();
	  BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	  
	  System.out.println("Enter a message: ");
	  
	  String message = inFromUser.readLine();
	  byte[] sendData = new byte[1024];
	  
	  sendData=message.getBytes();
	  
	  InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
	  DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,9876);
	  
	  clientSocket.send(sendPacket);
	  
	  byte[] receiveData = new byte[1024];
	  DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
	  
	  clientSocket.receive(receivePacket);
	  
	  String serverMessage = new String(receivePacket.getData());
	  System.out.println("Got from server: " + serverMessage);
	  
	  clientSocket.close();
  }
}
