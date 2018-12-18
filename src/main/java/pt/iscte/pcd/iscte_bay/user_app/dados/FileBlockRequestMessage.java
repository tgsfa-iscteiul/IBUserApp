package pt.iscte.pcd.iscte_bay.user_app.dados;

import java.io.Serializable;

/**
 *  Mensagem que passa no socket a solicitar parte de um cliente
 *  
 * @author tomas
 *
 */
public class FileBlockRequestMessage implements Serializable {

	
	private FileBlock fileBlock;
	
	
	
	public FileBlockRequestMessage(FileBlock block) {
		fileBlock = block;
	}
	
	
	public FileBlock getBlock() {
		return fileBlock;
	}
	
	
}
