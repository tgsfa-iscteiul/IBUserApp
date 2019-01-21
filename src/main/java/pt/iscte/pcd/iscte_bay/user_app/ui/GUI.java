package pt.iscte.pcd.iscte_bay.user_app.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pt.iscte.pcd.iscte_bay.user_app.Client;
import pt.iscte.pcd.iscte_bay.user_app.dados.ClientConnector;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileDetails;

/**
 * Ponto de entrada e implementação do Inteface com o Utilizador
 * 
 * @author tomas
 *
 */
public class GUI {

	private Client client;
	private JFrame frame;
	private JButton downloadButton;
	private JButton searchButton;
	private JTextField textField;
	private JList<String> searchResultList;
	private DefaultListModel<String> listModel = new DefaultListModel<>();
	private JProgressBar progressBar;
	private JLabel label;
	private String selectedFromList = null;
	private HashMap<FileDetails, List<ClientConnector>> files = new HashMap<FileDetails, List<ClientConnector>>();

	public GUI(InetAddress directoryAddress, int directoryPort, int userPort, String filesFolder) {

		frame = new JFrame("The Iscte Bay");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setPreferredSize(new Dimension(400, 250));
		addFrameContent();
		frame.pack();

		client = new Client(directoryAddress, directoryPort, userPort, filesFolder);

		// Provavelmente será aqui que se deve usar uma worker thread do swing
		try {
			client.runClient();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * sets the frame visible to the user
	 */
	public void open() {
		frame.setVisible(true);
	}

	/**
	 * initiates and adds the frame content
	 */
	public void addFrameContent() {
		downloadButton = new JButton("Download");
		searchButton = new JButton("Search");
		textField = new JTextField();
		searchResultList = new JList<>(listModel);
		progressBar = new JProgressBar();
		label = new JLabel();
		textField.setPreferredSize(new Dimension(80, 20));
		label.setText("Text to search:");
		downloadButton.setPreferredSize(new Dimension(200, 100));
		progressBar.setPreferredSize(new Dimension(200, 100));

		JPanel textFIeldPanel = new JPanel();
		textFIeldPanel.add(textField);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout());
		topPanel.add(label);
		topPanel.add(searchButton);
		topPanel.add(textFIeldPanel, FlowLayout.CENTER);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(140, 200));
		rightPanel.add(downloadButton, BorderLayout.NORTH);
		rightPanel.add(progressBar, BorderLayout.SOUTH);

		JPanel leftPanel = new JPanel();
		leftPanel.add(searchResultList);

		frame.add(topPanel, BorderLayout.NORTH);
		frame.add(rightPanel, BorderLayout.EAST);
		frame.add(leftPanel, BorderLayout.WEST);

		addActionListeners();
	}

	/**
	 * adds all necessary listeners for buttons
	 */
	public void addActionListeners() {

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				try {
					System.out.println("Closing client Socket...");
					client.close();
					System.out.println("Socket closed");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if (!textField.getText().isEmpty()) {
					listModel.clear();
					try {
						client.sendFileNameRequest(textField.getText());
						files = client.getUsersFilesMap();
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (files != null) {
						for (FileDetails fd : files.keySet()) {
							int numClients = files.get(fd).size();
							listModel.addElement(fd.getFileName() + " (" + numClients + ")");
						}
					} else {
						listModel.addElement("No users available with file");
					}
				}
			}
		});

		downloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Clicked download button");
				if (selectedFromList != null) {
					System.out.println("Selected not null");
					for (FileDetails f : files.keySet()) {
						String fileName = selectedFromList.replaceAll("\\s*\\(\\d+\\)", ""); //filtra os parenteses e numero de users
						if (f.getFileName().equals(fileName)) {
							System.out.println("Found file. Making request ...");
							try {
								client.requestFileParts(f);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							break;
						}
					}
				}
			}
		});

		searchResultList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent evt) {
				if (evt.getValueIsAdjusting())
					return;
				
				selectedFromList = searchResultList.getSelectedValue();
				System.out.println(selectedFromList);
			}
		});

	}

}
