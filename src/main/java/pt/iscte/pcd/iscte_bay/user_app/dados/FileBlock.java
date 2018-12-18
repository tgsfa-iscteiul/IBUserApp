package pt.iscte.pcd.iscte_bay.user_app.dados;

import java.io.Serializable;

/**
 *  Informação acerca de uma parte de um ficheiro que será 
 *  transferida de um Utilizador especifico
 *  
 * @author tomas
 */
public class FileBlock implements Serializable{

	private FileDetails fileDetails;
	private int offset;
	private int length;
	
	
	
	public FileBlock(FileDetails details, int offset, int length) { 
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
