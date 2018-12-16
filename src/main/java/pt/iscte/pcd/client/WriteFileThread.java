package pt.iscte.pcd.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WriteFileThread  implements Runnable {

	private SingleBarrier singleBarrier;
	private byte[] wholeFile;
	private String fileName;
	private String folderName = "files";
	
	
	public WriteFileThread(SingleBarrier singleBarrier, byte[] wholeFile, String fileName) {
		this.singleBarrier = singleBarrier;
		this.wholeFile = wholeFile;
		this.fileName = fileName;
	}
	
	@Override
	public void run() {
		singleBarrier.barrierWait(); //waits for all  blocks to arrive
		try {
			Files.write(Paths.get(folderName+"/"+fileName), wholeFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
