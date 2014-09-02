package gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.TextArea;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.text.DefaultCaret;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import logika.Receiver;
import logika.Sender;

public class MenuPks2 {

	private JFrame frame;
	private JTextField textField;
	public JTextArea textArea = null;
	private JTextField textField_1;
	private JTextField textField_2;
	private Thread t = null;
	private Receiver receiver = null;
	private Sender sender = null;
	private JLabel lblNewLabel_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;
	private JLabel lblStatus;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MenuPks2 window = new MenuPks2();
					window.frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */

	public static void setTheWindowLook() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public MenuPks2() {
		initialize();
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		setTheWindowLook();
		frame = new JFrame();
		frame.setBounds(100, 100, 1268, 593);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(10, 11, 1232, 533);
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblCieovAdresa = new JLabel("Cie\u013Eov\u00E1 adresa:");
		lblCieovAdresa.setBounds(917, 343, 111, 14);
		panel.add(lblCieovAdresa);

		textField = new JTextField();
		textField.setBounds(1069, 340, 153, 20);
		try {
			textField.setText(InetAddress.getLocalHost().getHostAddress().replaceAll("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})", "$1.$2.$3."));
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		panel.add(textField);
		textField.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 1212, 321);

		panel.add(scrollPane);

		textArea = new JTextArea();
		textArea.setFont(new Font("Courier New", Font.PLAIN, 16));
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane.setViewportView(textArea);

		JButton btnPrijma = new JButton("Prij\u00EDma\u0165");
		btnPrijma.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
				lblNewLabel_2.setText("Prijimam");
				receiver = new Receiver(textArea, textField_3, lblNewLabel_2, textField_5.getText());
				t = new Thread(receiver);
				t.start();
			}
		});
		btnPrijma.setBounds(10, 499, 103, 23);
		panel.add(btnPrijma);

		JLabel lblVekosVzorky = new JLabel("Ve\u013Ekos\u0165 vzorky");
		lblVekosVzorky.setBounds(917, 433, 89, 14);
		panel.add(lblVekosVzorky);

		textField_1 = new JTextField();
		textField_1.setBounds(1069, 430, 153, 20);
		panel.add(textField_1);
		textField_1.setColumns(10);

		JLabel lblNewLabel = new JLabel("Po\u010Det vzoriek");
		lblNewLabel.setBounds(917, 467, 89, 14);
		panel.add(lblNewLabel);

		textField_2 = new JTextField();
		textField_2.setBounds(1069, 461, 153, 20);
		panel.add(textField_2);
		textField_2.setColumns(10);

		JButton btnOdosla = new JButton("Odosla\u0165");
		btnOdosla.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
				lblNewLabel_2.setText("Odosielam");
				sender = new Sender(textField.getText(), textField_1
						.getText(), textField_2.getText(), textArea, lblNewLabel_2, textField_4, textField_6.getText());
				t = new Thread(sender);
				t.start();
			}
		});
		btnOdosla.setBounds(917, 499, 111, 23);
		panel.add(btnOdosla);

		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				receiver.terminate();
			}
		});
		btnStop.setBounds(162, 499, 77, 23);
		panel.add(btnStop);

		lblStatus = new JLabel("Status:");
		lblStatus.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 20));
		lblStatus.setBounds(10, 334, 80, 34);
		panel.add(lblStatus);

		lblNewLabel_2 = new JLabel("Pripravené");
		lblNewLabel_2
				.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 20));
		lblNewLabel_2.setBounds(100, 338, 132, 26);
		panel.add(lblNewLabel_2);

		JLabel lblPravdepodobnosChyby = new JLabel("Pravdepodobnos\u0165 chyby");
		lblPravdepodobnosChyby.setBounds(10, 467, 142, 14);
		panel.add(lblPravdepodobnosChyby);

		textField_3 = new JTextField();
		textField_3.setBounds(162, 464, 86, 20);
		textField_3.setText("5");
		panel.add(textField_3);
		textField_3.setColumns(10);

		JLabel lblPravdepodbnosChyby = new JLabel("Pravdepodbnos\u0165 chyby");
		lblPravdepodbnosChyby.setBounds(917, 376, 142, 14);
		panel.add(lblPravdepodbnosChyby);

		textField_4 = new JTextField();
		textField_4.setBounds(1069, 373, 153, 20);
		textField_4.setText("5");
		panel.add(textField_4);
		textField_4.setColumns(10);

		JButton btnZrui = new JButton("Zru\u0161i\u0165");
		btnZrui.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sender.canceled = true;
			}
		});
		btnZrui.setBounds(1069, 499, 153, 23);
		panel.add(btnZrui);
		
		JLabel lblTimeout = new JLabel("Timeout");
		lblTimeout.setBounds(10, 433, 142, 14);
		panel.add(lblTimeout);
		
		textField_5 = new JTextField();
		textField_5.setBounds(162, 430, 86, 20);
		textField_5.setText("5000");
		panel.add(textField_5);
		textField_5.setColumns(10);
		
		JLabel lblTimeout_1 = new JLabel("Timeout");
		lblTimeout_1.setBounds(917, 401, 111, 14);
		panel.add(lblTimeout_1);
		
		textField_6 = new JTextField();
		textField_6.setBounds(1069, 399, 153, 20);
		textField_6.setText("100");
		panel.add(textField_6);
		textField_6.setColumns(10);
	}
}
