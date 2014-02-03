package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JScrollPane;

/**
 * GUI code for the client.
 * 
 * Makes use of the ClientController to interact with
 * the server.
 * 
 * @author Eric Ostrowski
 *
 */

public class ClientGui extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtIpAddress;
	private JTextField txtPortNumber;
	private JTextField txtFileName;
	private static JTextArea txtDiagLog;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) throws Exception {
		
		txtDiagLog = new JTextArea();
		
		ClientController.initializeClient(txtDiagLog);
		
		//Ensure the controller can shutdown properly
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	            txtDiagLog.append("Shutting down...\n");
	            ClientController.killClient();
	        }
	    }, "Shutdown-thread"));
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGui frame = new ClientGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	
	/*
	 * Center the window on the screen
	 */
	private void centerFrame() {
		//Place window in center of screen
		setSize(592, 382);
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - getHeight()) / 2);
		setLocation(x, y);
	}
	
	/*
	 * Set the look and feel to the system default 
	 */
	private void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the frame.
	 */
	public ClientGui() {
		setTitle("Client");
		
		setLookAndFeel();
		centerFrame();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.WEST);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblIp = new JLabel("IP:");
		panel.add(lblIp, "2, 2, right, default");
		
		txtIpAddress = new JTextField();
		txtIpAddress.setText("127.0.0.1");
		panel.add(txtIpAddress, "4, 2, left, default");
		txtIpAddress.setColumns(15);
		
		JLabel lblPort = new JLabel("Port:");
		panel.add(lblPort, "2, 4, right, default");
		
		txtPortNumber = new JTextField();
		txtPortNumber.setText("9876");
		panel.add(txtPortNumber, "4, 4, left, default");
		txtPortNumber.setColumns(5);
		
		JLabel lblFilename = new JLabel("File Name:");
		panel.add(lblFilename, "2, 6, right, default");
		
		txtFileName = new JTextField();
		panel.add(txtFileName, "4, 6, fill, default");
		txtFileName.setColumns(10);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
						try {
							ClientController.requestFile(txtFileName.getText(), txtIpAddress.getText(), Integer.parseInt(txtPortNumber.getText()));
						} catch (NumberFormatException e1) {
							txtDiagLog.append("Invalid port number\n");
							e1.printStackTrace();
						} catch (UnknownHostException e1) {
							txtDiagLog.append("Unable to determine local IP address\n");
							e1.printStackTrace();
						} catch (SocketException e1) {
							txtDiagLog.append("Unable to open socket\n");
							e1.printStackTrace();
						} catch (IOException e1) {
							txtDiagLog.append(e1.getMessage() + "\n");
							e1.printStackTrace();
						}
			}
		});
		panel.add(btnConnect, "4, 8, left, default");
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		Component rigidArea = Box.createRigidArea(new Dimension(20, 20));
		panel_1.add(rigidArea, BorderLayout.WEST);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);
		
		txtDiagLog.setLineWrap(true);
		txtDiagLog.setForeground(Color.GREEN);
		txtDiagLog.setFont(new Font("Arial", Font.PLAIN, 12));
		txtDiagLog.setEditable(false);
		txtDiagLog.setBackground(Color.BLACK);
		scrollPane.setViewportView(txtDiagLog);
		
		
	}

}
