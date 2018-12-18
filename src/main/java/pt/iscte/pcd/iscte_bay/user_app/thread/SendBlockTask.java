package pt.iscte.pcd.iscte_bay.user_app.thread;

import java.io.IOException;
import java.io.ObjectOutputStream;

import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlockRequestMessage;
import pt.iscte.pcd.iscte_bay.user_app.dados.FilePart;

public class SendBlockTask implements Runnable {

	private byte[] fileContents;
	private FileBlockRequestMessage requestedBlock;
	private byte[] request;
	private ObjectOutputStream out;
	private int offSet;
	private int size;

	public SendBlockTask(byte[] fileContents, FileBlockRequestMessage requestedBlock, ObjectOutputStream out) {
		this.fileContents = fileContents;
		this.requestedBlock = requestedBlock;
		this.out = out;
		request = new byte[(int)requestedBlock.getBlock().getLength()];
	}

	@Override
	public void run() {
		fillRequest();
		System.out.println("Filled request, sending block...");
		FilePart f = new FilePart(request, offSet, size);
		try {
			out.writeObject(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fillRequest() {  //precisa de sincronizacao ?
		offSet = (int) requestedBlock.getBlock().getOffset();
		size = (int) requestedBlock.getBlock().getLength();

		for (int i = 0; i < size; i++) {  // < ou <=  ?
			int j = i + offSet;
			request[i] = fileContents[j];
		}
	}

}
