package pt.iscte.pcd.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Client {

	private InetAddress directoryAddress;
	private int directoryPort;
	private int userPort;
	private String filesFolder;
	
	private InetAddress userAddress;
	private Socket directorySocket;

	private BufferedReader in;
	private PrintWriter out;
	private ObjectOutputStream outStream;
	private ObjectInputStream inStream;

	private ArrayList<String> userList;
	private ArrayList<ClientConnector> connectionsList;
	private HashMap<FileDetails, List<ClientConnector>> usersFilesMap;
	private ArrayList<Block> blockList;

	private WordSearchMessage word;
	private byte[] wholeFile; // each block needs to be added to this array

	public static final int FILEBLOCKSIZE = 1024;


	public Client(InetAddress directoryAddress, int directoryPort, int userPort, String filesFolder) {
		this.directoryAddress = directoryAddress;
		this.directoryPort = directoryPort;
		this.userPort = userPort;
		this.filesFolder = filesFolder;
		
		connectionsList = new ArrayList<>();
		blockList = new ArrayList<>();
	}

	public void runClient() throws IOException {
		try {
			connectToDirectory();
			registerInDirectory();
			new ClientServer(userPort).start(); // deve ser iniciado aqui ?
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * connects to server opening the input/output channel between client and server
	 * 
	 * @throws IOException
	 */
	private void connectToDirectory() throws IOException {
		//address = InetAddress.getByName(null);
		System.out.println("Endereco = " + directoryAddress);
		directorySocket = new Socket(directoryAddress, directoryPort);
		System.out.println("Socket = " + directorySocket);
		in = new BufferedReader(new InputStreamReader(directorySocket.getInputStream()));
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(directorySocket.getOutputStream())), true);
	}

	/**
	 * registers the user in directory
	 * 
	 * @throws IOException
	 */
	private void registerInDirectory() throws IOException {
		//  devolve o porto que esta a lançar ? 
		out.println("INSC " + "Nome " + directorySocket.getLocalAddress().getHostAddress() + " " + userPort);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * makes a request to the server for registered users
	 */
	public ArrayList<String> requestRegisteredUsers() throws IOException {
		out.println("CLT");
		userList = new ArrayList<>();
		String str = null;
		do { // lists users registered in directory
			str = in.readLine();
			userList.add(str);
		} while (!str.equals("END"));
		return userList;
	}

	/**
	 * makes a request to other users for a file with a certain keyword
	 * 
	 * @param fileName
	 */
	public HashMap<FileDetails, List<ClientConnector>> sendFileNameRequest(String fileName) throws Exception {
		word = new WordSearchMessage(fileName);
		usersFilesMap = new HashMap<FileDetails, List<ClientConnector>>();
		connectToUsers();
		ArrayList<FileDetails> listFromPeer = new ArrayList<>();
		for (ClientConnector c : connectionsList) {
			c.getOutputStream().writeObject(word);
			listFromPeer = (ArrayList<FileDetails>) c.getInputStream().readObject();
			if (!listFromPeer.isEmpty()) {
				// filesWithKeyword.addAll(listFromPeer);
				for (FileDetails file : listFromPeer) {
					if (usersFilesMap.containsKey(file)) {
						usersFilesMap.get(file).add(c);
					} else { // REVER !
						ArrayList<ClientConnector> usersWithFile = new ArrayList<ClientConnector>();
						usersWithFile.add(c);
						usersFilesMap.put(file, usersWithFile);
					}
				}
			}
		}
		return usersFilesMap;
	}

	public void requestFileParts(FileDetails fileDetails) {
		sortFileBlocks(fileDetails);
		ArrayList<ClientConnector> usersHavingFile = new ArrayList<ClientConnector>(usersFilesMap.get(fileDetails));
		int numberOfThreads = usersHavingFile.size(); // creates 1 worker thread per user having file
		BlockThreadPool threadPool = new BlockThreadPool(numberOfThreads);
		SingleBarrier singleBarrier = new SingleBarrier(blockList.size());
		WriteFileThread writingThread = new WriteFileThread(singleBarrier, wholeFile, fileDetails.getFileName());
		writingThread.run();
		for (Block b : blockList) { // creates as many tasks threads as there are blocks to be downloaded
			threadPool.submit(new BlockGetterTask(b, usersHavingFile, singleBarrier, wholeFile));
		}
	}

	private void sortFileBlocks(FileDetails fileDetails) {
		Block block;
		int totalLength = (int) fileDetails.getFileSize();

		int lastBlockLength = totalLength % FILEBLOCKSIZE;
		int lastBlockOffset = totalLength - lastBlockLength;

		int offset = 0;

		while (offset < totalLength) {
			if (offset >= lastBlockOffset) {
				block = new Block(fileDetails, lastBlockOffset, lastBlockLength);
			} else {
				block = new Block(fileDetails, offset, FILEBLOCKSIZE); // 0-1023 ou 1-1024 ? 1024 incluido ?
			}
			offset = offset + FILEBLOCKSIZE + 1;
			// regiao critica ?
			blockList.add(block);
		}
	}

	private void connectToUsers() throws IOException {
		for (String s : userList) {
			if (!s.equals("END")) {
				String split[] = s.split(" ");
				InetAddress ip = InetAddress.getByName(split[0].substring(1));
				int port = Integer.parseInt(split[1]);
				if (canConnect(ip, port)) {
					Socket sockt = new Socket(ip, port);
					outStream = new ObjectOutputStream(sockt.getOutputStream());
					inStream = new ObjectInputStream(sockt.getInputStream());
					ClientConnector connector = new ClientConnector(sockt, inStream, outStream);
					connectionsList.add(connector);
				}
			}
		}
	}

	private boolean canConnect(InetAddress ip, int port) {
		if (this.userPort == port &&  directorySocket.getLocalAddress() == ip) {
			return false;
		}
		for (ClientConnector c : connectionsList) {
			if (c.getSocket().getInetAddress().equals(ip) && c.getSocket().getLocalPort() == port) {
				return false;
			}
		}
		return true;
	}

	/**
	 * TODO : Rever se o metodo deve ser static ou se deve estar aqui (mudar para
	 * ClientDealer)
	 * 
	 * @return
	 */
	public static File[] getFilesFromFolder(String folderName) {
		File folder = new File(folderName);
		File[] files = folder.listFiles();
		return files;
	}

	// public void startServing() throws IOException {
	// serverSocket = new ServerSocket(PORT);
	// try {
	// while (true) {
	// Socket socket = serverSocket.accept();
	// System.out.println("Conex�o aceite: " + socket);
	// new DealWithClient(socket, userTable).start();
	// }
	// } finally {
	// serverSocket.close();
	// }
	// }

	/**
	 * closes directory socket
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		out.println("END");
		directorySocket.close();
	}

}
