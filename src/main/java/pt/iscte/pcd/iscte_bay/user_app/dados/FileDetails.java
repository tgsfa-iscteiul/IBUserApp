package pt.iscte.pcd.iscte_bay.user_app.dados;

import java.io.Serializable;

/**
 *  Informação acerca de um ficheiro a transferir
 *  
 * @author tomas
 *
 */
public class FileDetails implements Serializable {

	private String fileName;
	private int fileLength;

	public FileDetails(String name, int size) {
		this.fileName = name;
		this.fileLength = size;
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileLength;
	}

	
	
	
	@Override
	public boolean equals(Object o) {
		FileDetails fdt = (FileDetails) o;
		if (!fdt.getFileName().equals(this.fileName))
			return false;
		if (fdt.getFileSize() != this.fileLength)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int hash;
		hash = (int) this.fileLength + this.fileName.length();
		return hash;
	}

}
