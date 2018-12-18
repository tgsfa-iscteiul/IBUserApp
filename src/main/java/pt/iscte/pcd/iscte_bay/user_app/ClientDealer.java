package pt.iscte.pcd.iscte_bay.user_app;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlockRequestMessage;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileDetails;
import pt.iscte.pcd.iscte_bay.user_app.dados.WordSearchMessage;
import pt.iscte.pcd.iscte_bay.user_app.thread.BlockThreadPool;
import pt.iscte.pcd.iscte_bay.user_app.thread.SendBlockTask;

/**
 *  Sempre que UserAppServer recebe uma nova conexão, lança uma nova thread ClientDealer que
 *  se encarrega de responder a mensagens solicitadas por outro Utilizador
 *  
 * @author tomas
 *
 */
public class ClientDealer extends Thread {

	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private File[] files;
	private BlockThreadPool threadPool;

	public ClientDealer(Socket s, BlockThreadPool threadPool) {
		socket = s;
		this.threadPool = threadPool;
	}

	@Override
	public void run() {
		try {
			doConnections(socket);
			serve();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void doConnections(Socket socket) throws IOException {
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}

	private void serve() {
		try {
			while (true) {
				files = Client.getFilesFromFolder("files"); // getFilesFromFolder pode ser definido nesta classe?
				Object request =  in.readObject();
				System.out.println("Recebi pedido");
				if(request instanceof WordSearchMessage) {
					System.out.println("Pesquisa");
				//filtrar pedidos  if(in.readObject instanceof Wordsmsg...)
				WordSearchMessage keyWord = (WordSearchMessage)request;
				ArrayList<FileDetails> filesWithKeyword = new ArrayList<>();
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().contains(keyWord.getKeyWord())) {
						String name = files[i].getName();
						int size = (int) files[i].length();
						FileDetails fileDetails = new FileDetails(name, size);
						filesWithKeyword.add(fileDetails);
					}
				}
				out.writeObject(filesWithKeyword);
				} else {
				FileBlockRequestMessage requestedBlock = (FileBlockRequestMessage) request;
				System.out.println("Got a request for file "+ requestedBlock.getBlock().getFileDetails().getFileName());
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().equals(requestedBlock.getBlock().getFileDetails().getFileName())
							&& files[i].length() == requestedBlock.getBlock().getFileDetails().getFileSize()) {
						byte[] fileContents = Files.readAllBytes(files[i].toPath());
						threadPool.submit(new SendBlockTask(fileContents, requestedBlock, out));
						break;
					}
				}
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
