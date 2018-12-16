package pt.iscte.pcd.client;

import java.io.IOException;
import java.util.ArrayList;

public class BlockGetterTask implements Runnable {

	private Block fileBlock;
	private ArrayList<ClientConnector> usersWithFile;
	private SingleBarrier singleBarrier;
	private byte[] wholeFile;

	public BlockGetterTask(Block block, ArrayList<ClientConnector> usersWithFile, SingleBarrier barrier,
			byte[] wholeFile) {
		this.fileBlock = block;
		this.usersWithFile = usersWithFile;
		this.singleBarrier = barrier;
		this.wholeFile = wholeFile;
	}

	@Override
	public void run() {
		FileBlockRequestMessage request = new FileBlockRequestMessage(fileBlock);

		while (findAvailableUser().equals(null)) { // como volta a testar se existem users disponivel ?
			try {
				wait(); // caso nao existam utilizadores livres, espera
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		try {
			findAvailableUser().getOutputStream().writeObject(request); // envia o pedido do bloco
			FilePart fp = (FilePart) findAvailableUser().getInputStream().readObject(); // recebe o bloco pedido
			for (int i = 0; i < fp.getSize(); i++) {  //escreve o bloco 
				int j = i + fp.getOffSet();
				wholeFile[j] = fp.getFilePart()[j]; // rever
			}
			singleBarrier.barrierPost(); // increments barrier
			findAvailableUser().setAvailabe(true);
			notifyAll(); // acorda as outras threads (user ficou disponivel)
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

//              SECCAO CRITICA
	private synchronized ClientConnector findAvailableUser() {
		for (ClientConnector u : usersWithFile) {
			if (u.isAvailable()) { // para que cada cliente nao possa disponibilizar
				u.setAvailabe(false); // mais do que um block em simultaneo
				return u;
			}
		}
		return null; // no users available with the file block

	}

}
