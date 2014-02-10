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
import java.net.SocketException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JProgressBar;

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
	private static JCheckBox chkBPacketMiss;
	private static JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) throws Exception {
		
		txtDiagLog = new JTextArea();
		progressBar = new JProgressBar();
		
		((DefaultCaret)txtDiagLog.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		txtDiagLog.setEditable(false);
		
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
				FormFactory.DEFAULT_ROWSPEC,
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
		txtIpAddress.setColumns(11);
		
		JLabel lblPort = new JLabel("Port:");
		panel.add(lblPort, "2, 4, right, default");
		
		txtPortNumber = new JTextField();
		txtPortNumber.setText("9876");
		panel.add(txtPortNumber, "4, 4, left, default");
		txtPortNumber.setColumns(5);
		
		JLabel lblFilename = new JLabel("File Name:");
		panel.add(lblFilename, "2, 6, right, default");
		
		txtFileName = new JTextField();
		panel.add(txtFileName, "4, 6, left, default");
		txtFileName.setColumns(11);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Thread thread = new Thread(new ClientController(txtFileName.getText(), txtIpAddress.getText(), Integer.parseInt(txtPortNumber.getText()), chkBPacketMiss.isSelected(), txtDiagLog, progressBar));
					thread.start();
				} catch (NumberFormatException e1) {
					txtDiagLog.append("Invalid port number\n");
					e1.printStackTrace();
				} catch (SocketException e1) {
					txtDiagLog.append("Unable to open socket\n");
					e1.printStackTrace();
				}
			}
		});
		
		JLabel lblPacketMiss = new JLabel("Packet Miss:");
		panel.add(lblPacketMiss, "2, 8");
		
		chkBPacketMiss = new JCheckBox("");
		panel.add(chkBPacketMiss, "4, 8");
		panel.add(btnConnect, "4, 10, left, default");
		
		JLabel lblProgress = new JLabel("Progress:");
		panel.add(lblProgress, "4, 14");
		
		panel.add(progressBar, "4, 16");
		
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
