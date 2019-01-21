package pt.iscte.pcd.iscte_bay.user_app.thread;

import java.io.IOException;
import java.util.ArrayList;

import pt.iscte.pcd.iscte_bay.user_app.dados.ClientConnector;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlock;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlockRequestMessage;
import pt.iscte.pcd.iscte_bay.user_app.dados.FilePart;

/**
 * Thread que efectua pedidos dos blocos de um ficheiro ao(s) cliente(s) que
 * estiverem disponíveis 
 * 
 * @author tomas
 *
 */
public class BlockGetterTask extends Thread implements Runnable {

	private FileBlock fileBlock;
	private ArrayList<ClientConnector> usersWithFile;
	private SingleBarrier singleBarrier;
	private ArrayList<FilePart> listOfBlocks;

	public BlockGetterTask(FileBlock fileBlock, SingleBarrier sb, ArrayList<ClientConnector> usersWithFile,
			ArrayList<FilePart> listOfBlocks) {
		this.fileBlock = fileBlock;
		this.usersWithFile = usersWithFile;
		this.singleBarrier = sb;
		this.listOfBlocks = listOfBlocks;
	}

	@Override
	public void run()  { 
		ClientConnector peer;
		while ((peer = findAvailableUser()) == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Pede parte de um ficheiro ao outro utilizador
		FileBlockRequestMessage request = new FileBlockRequestMessage(fileBlock);
		FileBlock fileBlock = null;
		try {
			fileBlock = request.getBlock();
			synchronized (peer) {
				peer.getOutputStream().writeObject(request);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Recebe a parte do ficheiro do outro utilizador
		FilePart filePart = null;
		synchronized (peer) {
			try {
				filePart = (FilePart) peer.getInputStream().readObject();
				if (filePart == null) {
					System.out.println("Error : Received NULL FILE PART");
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				return;
			}
		}

		synchronized (listOfBlocks) {
			listOfBlocks.add(filePart);
		}


		singleBarrier.barrierPost();
		peer.setAvailabe(true);
		System.out.println("Executei BlockGetterTask : " + filePart);
	}

	/**
	 * Itera na lista de conexões a clientes até encontrar uma que esteja desocupada
	 */
	private synchronized ClientConnector findAvailableUser() { // Nao precisa de ser synchronized ?
		for (ClientConnector u : usersWithFile) {
			if (u.isAvailable()) { // para que cada cliente nao possa disponibilizar
				u.setAvailabe(false); // mais do que um block em simultaneo
				return u;
			}
		}
		return null; // no users available with the file block
	}

}
