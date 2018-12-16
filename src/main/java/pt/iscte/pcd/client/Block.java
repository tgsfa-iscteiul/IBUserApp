package pt.iscte.pcd.client;

public class Block {

	private FileDetails fileDetails;
	private int offset;
	private int length;
	
	
	
	public Block(FileDetails details, int offset, int length) { 
		fileDetails = details;
		this.offset = offset;
		this.length = length;
	}



	public FileDetails getFileDetails() {
		return fileDetails;
	}



	public long getOffset() {
		return offset;
	}



	public long getLength() {
		return length;
	}






	
	
}
