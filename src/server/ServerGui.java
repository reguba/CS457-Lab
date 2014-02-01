package server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class ServerGui {
	
	private JFrame frame;
	private static JTextArea txtDiagLog;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		txtDiagLog = new JTextArea();
		((DefaultCaret)txtDiagLog.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		txtDiagLog.setEditable(false);
		
		//Ensure the controller can shutdown properly
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	            txtDiagLog.append("Shutting down...\n");
	            ServerController.killServer();
	        }
	    }, "Shutdown-thread"));
		
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
		} catch (IOException e) {
			txtDiagLog.append("Unable to receive requests\n");
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
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAutoscrolls(true);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		txtDiagLog.setLineWrap(true);
		txtDiagLog.setForeground(Color.GREEN);
		txtDiagLog.setBackground(Color.BLACK);
		scrollPane.setViewportView(txtDiagLog);
	}

}
