package pt.iscte.pcd.iscte_bay.user_app.thread;

import java.io.IOException;
import java.util.ArrayList;

import pt.iscte.pcd.iscte_bay.user_app.dados.ClientConnector;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlock;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlockRequestMessage;
import pt.iscte.pcd.iscte_bay.user_app.dados.FilePart;

public class BlockGetterTask implements Runnable {

	private FileBlock fileBlock;
	private ArrayList<ClientConnector> usersWithFile;
	private SingleBarrier singleBarrier;
	private byte[] wholeFile;

	public BlockGetterTask(FileBlock block, ArrayList<ClientConnector> usersWithFile, SingleBarrier barrier,
			byte[] wholeFile) {
		this.fileBlock = block;
		this.usersWithFile = usersWithFile;
		this.singleBarrier = barrier;
		this.wholeFile = wholeFile;
	}

	@Override
	public void run() {
		System.out.println("Vou executar blockGetter Task");
		FileBlockRequestMessage request = new FileBlockRequestMessage(fileBlock);

		while (findAvailableUser() ==null) { // como volta a testar se existem users disponivel ?
			try {
				wait(); // caso nao existam utilizadores livres, espera
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		try {
			System.out.println("vou Efetuar pedido");

			findAvailableUser().getOutputStream().writeObject(request); // envia o pedido do bloco
			System.out.println("Efetuar pedido");

			FilePart fp = (FilePart) findAvailableUser().getInputStream().readObject(); // recebe o bloco pedido
			System.out.println("bloco recebido");

			for (int i = 0; i < fp.getSize(); i++) {  //escreve o bloco 
				int j = i + fp.getOffSet();
				wholeFile[j] = fp.getFilePart()[j]; // rever
			}
			singleBarrier.barrierPost(); // increments barrier
			findAvailableUser().setAvailabe(true);
			notifyAll(); // acorda as outras threads (user ficou disponivel)
			System.out.println("Downloaded block["+ fileBlock.getOffset() + " " +fileBlock.getLength() + "]");
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
