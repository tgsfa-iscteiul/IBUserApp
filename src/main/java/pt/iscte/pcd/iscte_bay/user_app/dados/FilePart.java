package pt.iscte.pcd.iscte_bay.user_app.dados;

import java.io.Serializable;

/**
 *  Parte de um ficheiro transferido entre dois clientes
 *  
 * @author tomas
 */
//@SuppressWarnings("serial")
public class FilePart implements Serializable, Comparable<FilePart>{
	private byte[] filePart;
	private int offSet;
	private int size;
	private ClientConnector sourceUser;
	
	
	
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
	
	public void setSourceUser(ClientConnector sourceUser) {
		this.sourceUser = sourceUser;
	}
	
	@Override
	public int compareTo(FilePart fp) {  // DA EXCECAO
		return offSet - fp.offSet;
	}
	
		
}
