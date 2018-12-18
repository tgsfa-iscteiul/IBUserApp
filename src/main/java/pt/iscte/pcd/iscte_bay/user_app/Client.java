package pt.iscte.pcd.iscte_bay.user_app;

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

import pt.iscte.pcd.iscte_bay.user_app.dados.ClientConnector;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlock;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileDetails;
import pt.iscte.pcd.iscte_bay.user_app.dados.WordSearchMessage;
import pt.iscte.pcd.iscte_bay.user_app.thread.BlockGetterTask;
import pt.iscte.pcd.iscte_bay.user_app.thread.BlockThreadPool;
import pt.iscte.pcd.iscte_bay.user_app.thread.SingleBarrier;
import pt.iscte.pcd.iscte_bay.user_app.thread.WriteFileThread;

/**
 * Implementação das funcionalidades de cliente, ou seja tudo o que se relaciona
 * com a comunicação efectuada por iniciativa da UserApp com outros Utilizadores
 * e com o directório.
 * 
 * @author tomas
 *
 */
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
	private ArrayList<FileBlock> blockList;

	private WordSearchMessage word;
	private byte[] wholeFile; // each block needs to be added to this array

	public static final int FILEBLOCKSIZE = 1024;

	public Client(InetAddress directoryAddress, int directoryPort, int userPort, String filesFolder) {
		this.directoryAddress = directoryAddress;
		this.directoryPort = directoryPort;
		this.userPort = userPort;
		this.filesFolder = filesFolder;

		blockList = new ArrayList<>();
		
	}

	public void runClient() throws IOException {
		try {
			connectToDirectory();
			registerInDirectory();
			// requestRegisteredUsers();
			new UserAppServer(userPort).start(); // deve ser iniciado aqui ?
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
		// address = InetAddress.getByName(null);
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
		// devolve o porto que esta a lançar ?
		out.println("INSC " + directorySocket.getLocalAddress().getHostAddress() + " " + userPort);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * makes a request to the server for registered users
	 */
	private void requestRegisteredUsers() throws IOException {
		out.println("CLT");
		userList = new ArrayList<>();
		String str = null;
		do { // get users registered in directory
			str = in.readLine();
			System.out.println(str);
			userList.add(str);
		} while (!str.equals("END"));
	}

	/**
	 * makes a connection with the users received from directory, saves the streams
	 * and socket in list for future communication
	 * 
	 * @throws IOException
	 */
	private void connectToUsers() throws IOException { // PROBLEMA a conexao de um user que ja se tenha desconetado
		requestRegisteredUsers();
		connectionsList = new ArrayList<ClientConnector>(); // refaz a lista de conexoes
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

		if (this.userPort == port && directorySocket.getLocalAddress().equals(ip))
			return false; // impede a ligacao a si proprio
		for (ClientConnector c : connectionsList) { // verifica se já não está ligado ao user NAO FUNCIONA ?
			if (c.getSocket().getInetAddress().equals(ip) && c.getSocket().getPort() == port)
				return false;
		}
		return true;
	}

	/**
	 * makes a request to other users for a file with a certain keyword, saves the
	 * files with a specific keyword
	 * 
	 * @param fileName
	 */
	public void sendFileNameRequest(String fileName) throws Exception {
		word = new WordSearchMessage(fileName);
		usersFilesMap = new HashMap<FileDetails, List<ClientConnector>>();
		connectToUsers(); // Se se conseguiu ligar a pelo menos 1 user, envia o hashMap
		if (!connectionsList.isEmpty()) {
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
		}
	}

	public HashMap<FileDetails, List<ClientConnector>> getUsersFilesMap() {
		if (!usersFilesMap.isEmpty())
			return usersFilesMap;
		return null;
	}

	public void requestFileParts(FileDetails fileDetails) {
		sortFileBlocks(fileDetails);
		ArrayList<ClientConnector> usersHavingFile = new ArrayList<ClientConnector>(usersFilesMap.get(fileDetails));
		int numberOfThreads = usersHavingFile.size(); // creates 1 worker thread per user having file
		BlockThreadPool threadPool = new BlockThreadPool(numberOfThreads);
		SingleBarrier singleBarrier = new SingleBarrier(blockList.size());
		WriteFileThread writingThread = new WriteFileThread(singleBarrier, wholeFile, fileDetails.getFileName(), filesFolder);
		writingThread.start();
		int i = 0 ;
		for (FileBlock b : blockList) { // creates as many task threads as there are blocks to be downloaded
			System.out.println("Submit : block " + i );
//			threadPool.submit(new BlockGetterTask(b, usersHavingFile, singleBarrier, wholeFile));
			i++;
		}
	}

	private void sortFileBlocks(FileDetails fileDetails) {
		FileBlock block;
		int totalLength = (int) fileDetails.getFileSize();
		wholeFile = new  byte[totalLength];

		int lastBlockLength = totalLength % FILEBLOCKSIZE;
		int lastBlockOffset = totalLength - lastBlockLength;

		int offset = 0;

		while (offset < totalLength) {
			if (offset >= lastBlockOffset) {
				block = new FileBlock(fileDetails, lastBlockOffset, lastBlockLength);
			} else {
				block = new FileBlock(fileDetails, offset, FILEBLOCKSIZE); // 0-1023 ou 1-1024 ? 1024 incluido ?
			}
			offset = offset + FILEBLOCKSIZE + 1;
			// regiao critica ?
			blockList.add(block);
		}
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
