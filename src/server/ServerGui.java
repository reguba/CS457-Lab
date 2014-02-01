package server;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import java.awt.Color;
import java.io.IOException;
import java.net.SocketException;

public class ServerGui {

	private static JTextArea txtDiagLog;
	
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		txtDiagLog = new JTextArea();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGui window = new ServerGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		try {
			ServerController.acceptRequest(txtDiagLog);
		} catch (SocketException e) {
			txtDiagLog.append("Unable to open socket\n");
			e.printStackTrace();
		} catch (IOException e) {
			txtDiagLog.append("Unable to send file\n");
			e.printStackTrace();
		}
	}

	/**
	 * Create the application.
	 */
	public ServerGui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		txtDiagLog.setLineWrap(true);
		txtDiagLog.setForeground(Color.GREEN);
		txtDiagLog.setBackground(Color.BLACK);
		frame.getContentPane().add(txtDiagLog, BorderLayout.CENTER);
	}

}
