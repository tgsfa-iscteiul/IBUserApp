package pt.iscte.pcd.iscte_bay.user_app.dados;

import java.io.Serializable;

/**
 *  Parte de um ficheiro transferido entre dois clientes
 *  
 * @author tomas
 */
public class FilePart implements Serializable{

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
