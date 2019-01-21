package pt.iscte.pcd.iscte_bay.user_app.dados;

import java.io.Serializable;

/**
 *  Informação acerca de uma parte de um ficheiro que será 
 *  transferida de um Utilizador especifico
 *  
 * @author tomas
 */
public class FileBlock implements Serializable{

	// TODO  : FileDetails not necessary
	private String fileName;
	private int offset;
	private int offsetEnd;
	private int length;
	private Integer fileTotalLength;
	
	
	
	public FileBlock(String name, int offset, int length, Integer fileTotalLength) { 
		fileName = name;
		this.offset = offset;
		this.length = length;
		this.offsetEnd = this.offset + this.length;
		this.fileTotalLength = fileTotalLength;
	}

	public FileBlock( int offset, int length) {
		this(null,offset,length, null);
	}


	public String getName() {
		return fileName;
	}



	public int getOffset() {
		return offset;
	}



	public int getLength() {
		return length;
	}

	public int getOffsetEnd() {
		return offsetEnd;
	}
	
	public int getFileTotalLength() {
		return fileTotalLength;
	}
	
}
