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
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import pt.iscte.pcd.iscte_bay.file_transfer.FileAssemble;
import pt.iscte.pcd.iscte_bay.user_app.dados.ClientConnector;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlock;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlockRequestMessage;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileDetails;
import pt.iscte.pcd.iscte_bay.user_app.dados.FilePart;
import pt.iscte.pcd.iscte_bay.user_app.dados.WordSearchMessage;
import pt.iscte.pcd.iscte_bay.user_app.thread.BlockGetterTask;
import pt.iscte.pcd.iscte_bay.user_app.thread.BlockThreadPool;
import pt.iscte.pcd.iscte_bay.user_app.thread.SingleBarrier;
import pt.iscte.pcd.iscte_bay.user_app.thread.TaskBlockingQueue;
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

	private Socket directorySocket;

	private BufferedReader in;
	private PrintWriter out;
	private ObjectOutputStream outStream;
	private ObjectInputStream inStream;

	private ArrayList<String> userList;
	private ArrayList<ClientConnector> connectionsList;
	private HashMap<FileDetails, List<ClientConnector>> usersFilesMap;
	private ArrayList<FileBlock> blockList;
	private ArrayList<ClientConnector> usersHavingFile;
	private ArrayList<FilePart> blocksToWrite;


	private WordSearchMessage word;

	public static final int FILEBLOCKSIZE = 1024;

	public Client(InetAddress directoryAddress, int directoryPort, int userPort, String folderName) {
		this.directoryAddress = directoryAddress;
		this.directoryPort = directoryPort;
		this.userPort = userPort;
		this.filesFolder = folderName;


	}

	public void runClient() throws IOException {
		try {
			connectToDirectory();
			registerInDirectory();
			new UserAppServer(userPort, filesFolder).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * liga-se ao diretorio
	 * 
	 * @throws IOException
	 */
	private void connectToDirectory() throws IOException {
		System.out.println("Endereco = " + directoryAddress);
		directorySocket = new Socket(directoryAddress, directoryPort);
		System.out.println("Socket = " + directorySocket);
		in = new BufferedReader(new InputStreamReader(directorySocket.getInputStream()));
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(directorySocket.getOutputStream())), true);
	}

	/**
	 * regista-se no diretorio
	 * 
	 * @throws IOException
	 */
	private void registerInDirectory() throws IOException {
		out.println("INSC " + directorySocket.getLocalAddress().getHostAddress() + " " + userPort);
	}

	/**
	 * pede ao servidor os clientes inscritos no
	 * diretorio e adiciona a lista
	 */
	private void requestRegisteredUsers() throws IOException {
		out.println("CLT");
		userList = new ArrayList<>();
		String str = null;
		do { // get users registered in directory
			str = in.readLine();
			userList.add(str);
		} while (!str.equals("END"));
	}

	/**
	 * faz a conexao com os utilizadores recebidos no diretorio, guarda as
	 * streams e socket p/ comunicacao futura
	 * 
	 * @throws IOException
	 */
	private void connectToUsers() throws IOException {
		requestRegisteredUsers();
		connectionsList = new ArrayList<ClientConnector>();
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

	/**
	 * Para que o programa não se conecte a si próprio
	 */
	private boolean canConnect(InetAddress ip, int port) {

		if (this.userPort == port && directorySocket.getLocalAddress().equals(ip))
			return false; // impede a ligacao a si proprio
		for (ClientConnector c : connectionsList) {
			if (c.getSocket().getInetAddress().equals(ip) && c.getSocket().getPort() == port)
				return false;
		}
		return true;
	}

	/**
	 * pede a outros utilizadores ficheiro/os que contenham a keyword
	 * 
	 * @param fileName
	 */
	public void sendFileNameRequest(String fileName) throws Exception {
		word = new WordSearchMessage(fileName);
		usersFilesMap = new HashMap<FileDetails, List<ClientConnector>>();
		connectToUsers(); //liga-se aos users
		if (!connectionsList.isEmpty()) {
			ArrayList<FileDetails> listFromPeer = new ArrayList<>();
			for (ClientConnector c : connectionsList) {
				c.getOutputStream().writeObject(word);
				listFromPeer = (ArrayList<FileDetails>) c.getInputStream().readObject();
				if (!listFromPeer.isEmpty()) {
					for (FileDetails file : listFromPeer) {
						if (usersFilesMap.containsKey(file)) {
							usersFilesMap.get(file).add(c);
						} else {
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

	public void requestFileParts(FileDetails fileDetails) throws InterruptedException {
		createBlocks(fileDetails);
		usersHavingFile = new ArrayList<ClientConnector>(usersFilesMap.get(fileDetails));
		blocksToWrite = new ArrayList<FilePart>();

		int numberOfThreads = usersHavingFile.size(); // cria 1 workerThread por conexao
		BlockThreadPool threadPool = new BlockThreadPool(numberOfThreads);
		SingleBarrier singleBarrier = new SingleBarrier(blockList.size());
		for (FileBlock fb : blockList) {
			BlockGetterTask getFile = new BlockGetterTask(fb, singleBarrier, usersHavingFile, blocksToWrite);
			threadPool.submit(getFile);
		}
		singleBarrier.barrierWait(); // espera que os blocos cheguem todos
		System.out.println("Passed the barrier : " + blocksToWrite);
		Collections.sort(blocksToWrite);
		WriteFileThread writingThread = new WriteFileThread(fileDetails, blocksToWrite, filesFolder);
		writingThread.start();

	}

	private void createBlocks(FileDetails fd) {
		blockList = new ArrayList<FileBlock>();
		FileBlock block;
		int totalLength = (int) fd.getFileSize();
		int lastBlockLength = totalLength % FILEBLOCKSIZE;
		int lastBlockOffset = totalLength - lastBlockLength;

		int offset = 0;

		while (offset < totalLength) {
			if (offset >= lastBlockOffset) {
				block = new FileBlock(fd.getFileName(), lastBlockOffset, lastBlockLength, (int) fd.getFileSize());
			} else {
				block = new FileBlock(fd.getFileName(), offset, FILEBLOCKSIZE, (int) fd.getFileSize());
			}
			offset = offset + FILEBLOCKSIZE;
			blockList.add(block);
		}
	}



	public static File[] getFilesFromFolder(String folderName) {
		File folder = new File(folderName);
		File[] files = folder.listFiles();
		return files;
	}



	/**
	 * fecha a socket do diretorio
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		out.println("END");
		directorySocket.close();
	}

}
