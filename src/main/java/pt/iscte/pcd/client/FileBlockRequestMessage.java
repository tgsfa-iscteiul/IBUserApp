package pt.iscte.pcd.client;

public class FileBlockRequestMessage {

	
	private Block fileBlock;
	
	
	
	public FileBlockRequestMessage(Block block) {
		fileBlock = block;
	}
	
	
	public Block getBlock() {
		return fileBlock;
	}
	
	
}
