package pt.iscte.pcd.client;

public class FilePart {

	private byte[] filePart;
	private int offSet;
	private int size;
	
	
	
	public FilePart(byte[] block, int offSet, int size) {
		filePart = block;
		this.offSet = offSet;
		this.size = size;
	}
	
	
	public byte[] getFilePart() {
		return filePart;
	}
	
	public int getOffSet() {
		return offSet;
	}
	
	public int getSize() {
		return size;
	}
	
	
}
