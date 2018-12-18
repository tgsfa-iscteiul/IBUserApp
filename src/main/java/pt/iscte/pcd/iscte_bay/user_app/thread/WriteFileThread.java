package pt.iscte.pcd.iscte_bay.user_app.thread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WriteFileThread extends Thread  implements Runnable {

	private SingleBarrier singleBarrier;
	private byte[] wholeFile;
	private String fileName;
	private String folderName;
	
	
	public WriteFileThread(SingleBarrier singleBarrier, byte[] wholeFile, String fileName, String folderName) {
		this.singleBarrier = singleBarrier;
		this.wholeFile = wholeFile;
		this.fileName = fileName;
		this.folderName = folderName;
	}
	
	@Override
	public void run() {
		singleBarrier.barrierWait(); //waits for all  blocks to arrive
		try {
			System.out.println("writing on "+folderName+"/" +fileName + "  Byte array size:" + wholeFile.length);
			Files.write(Paths.get(folderName+"/"+fileName), wholeFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
