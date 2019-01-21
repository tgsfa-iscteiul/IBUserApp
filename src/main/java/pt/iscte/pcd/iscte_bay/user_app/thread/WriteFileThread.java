package pt.iscte.pcd.iscte_bay.user_app.thread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import pt.iscte.pcd.iscte_bay.user_app.dados.FileDetails;
import pt.iscte.pcd.iscte_bay.user_app.dados.FilePart;

public class WriteFileThread extends Thread  {

//	private byte[] wholeFile;
//	private String fileName;
//	private String folderName;
	
	private FileDetails fileDetails;
	private List<FilePart> fileParts;
	private String folderName;

	
	
//	public WriteFileThread(byte[] wholeFile, String fileName, String folderName) {
//		this.wholeFile = wholeFile;
//		this.fileName = fileName;
//		this.folderName = folderName;
//}
	public WriteFileThread(FileDetails fileDetails, List<FilePart> fileParts, String folderName) {
		this.fileDetails = fileDetails;

		this.fileParts = fileParts;
		this.folderName = folderName;
	}
	
	@Override
	public void run() {
//		try {
			System.out.println("writing on "+folderName+"/" +fileDetails.getFileName() + "  Byte array size:" );    //+ wholeFile.length);
			assembleFile();
			System.out.println("Wrote file");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public void assembleFile() {
		byte[] fileBytes = new byte[(int)fileDetails.getFileSize()];
		int i = 0;
		for (FilePart filePart : fileParts) {
			byte[] part = filePart.getFilePart();
			for (int j = 0 ; j < filePart.getSize() ; j++ ) {
				fileBytes[i] =  part[j];
				i++;
			}
		}
//		Files.write(Paths.get("/tmp/" + fileDetails.getFileName()),fileBytes);
		try {
			Files.write(Paths.get("/home/tomas/eclipse-workspace/IBUserApp/" + folderName + "/" + fileDetails.getFileName()), fileBytes);
		} catch (IOException e) {
			System.out.println("Error writing file");
			e.printStackTrace();
		}

	}
	
	
	
}
