package pt.iscte.pcd.iscte_bay.user_app;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

import pt.iscte.pcd.iscte_bay.file_transfer.FileDisassemble;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlockRequestMessage;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileDetails;
import pt.iscte.pcd.iscte_bay.user_app.dados.FilePart;
import pt.iscte.pcd.iscte_bay.user_app.dados.WordSearchMessage;
import pt.iscte.pcd.iscte_bay.user_app.thread.BlockThreadPool;

/**
 * Sempre que UserAppServer recebe uma nova conexão, lança uma nova thread
 * ClientDealer que se encarrega de responder a mensagens solicitadas por outro
 * Utilizador
 * 
 * @author tomas
 *
 */
public class ClientDealer extends Thread {

	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private File[] files;
	private String folderName;
	private FileDisassemble disassemble;

	public ClientDealer(Socket s, BlockThreadPool threadPool, String folderName) {
		socket = s;
		this.folderName = folderName;
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
				System.out.println("Closed socket");
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

	/**
	 * Ciclo infinito que recebe os pedidos de pesquisa de palavras ou de blocos de ficheiros
	 */
	private void serve() {
		try {
			while (true) {
				files = Client.getFilesFromFolder(folderName);
				Object request = new Object();
				synchronized (in) {
					request = in.readObject();
				}
				if (request instanceof WordSearchMessage) {
					WordSearchMessage keyWord = (WordSearchMessage) request;
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
				} else if (request instanceof FileBlockRequestMessage) {
					FileBlockRequestMessage requestedBlock = (FileBlockRequestMessage) request;
					for (int j = 0; j < files.length; j++) {
						if (files[j].getName().equals(requestedBlock.getBlock().getName())
								&& files[j].length() == requestedBlock.getBlock().getFileTotalLength()){
							disassemble = new FileDisassemble(files[j]);
							FilePart p = fillRequest(requestedBlock);
							try {
								synchronized (out) {
									out.writeObject(p);
								}
							} catch (Exception ex) {
								System.out.println("Erro a enviar bloco: " + ex.getMessage() );
								ex.printStackTrace();								
							}

							break;
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception while serving : " + e.getMessage() );
			e.printStackTrace();
		}
	}

	/**
	 * Preenche os bytes correspondentes ao ofset e dimensão solicitada pelo utilizador remoto
	 * 
	 * @param requestedBlock Parametros do bloco solicitado
	 * @return Bloco a devolver
	 */
	private FilePart fillRequest(FileBlockRequestMessage requestedBlock) {
		int requestedBlockOffset = requestedBlock.getBlock().getOffset();
		for (FilePart fp : disassemble.getFileParts()) {
			if (requestedBlockOffset == fp.getOffSet()) {
				return fp;
			}
		}
		return null;
	}

}
