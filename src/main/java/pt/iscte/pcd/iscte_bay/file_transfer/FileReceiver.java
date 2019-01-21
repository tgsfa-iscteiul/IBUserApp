package pt.iscte.pcd.iscte_bay.file_transfer;

import javax.swing.plaf.synth.SynthSeparatorUI;

import pt.iscte.pcd.iscte_bay.user_app.dados.FilePart;
import pt.iscte.pcd.iscte_bay.user_app.thread.SingleBarrier;
import pt.iscte.pcd.iscte_bay.user_app.thread.TaskBlockingQueue;

/**
 * Classe para testar o funcionamento multi-threading e se, quando todos os blocos forem recebidos 
 * a barreira abre e o ficheiro é escrito
 * 
 * @author tomas
 *
 */
public class FileReceiver extends Thread {
	private SingleBarrier singleBarrier;
	private TaskBlockingQueue waitingParts;
	private FileDisassemble fileDisassemble;
	
	public FileReceiver(SingleBarrier singleBarrier, TaskBlockingQueue waitingParts, FileDisassemble fileDisassemble) {
		this.singleBarrier = singleBarrier;
		this.waitingParts = waitingParts;
		this.fileDisassemble = fileDisassemble;
	}
	
	@Override
	public void run() {
		for (FilePart filePart : fileDisassemble.getFileParts()) {
			System.out.println("   Part  starting at " + filePart.getOffSet());
			waitingParts.add(filePart);
			singleBarrier.barrierPost();
		}
		
	}
}
