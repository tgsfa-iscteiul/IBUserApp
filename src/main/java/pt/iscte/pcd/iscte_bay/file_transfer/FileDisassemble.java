package pt.iscte.pcd.iscte_bay.file_transfer;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import pt.iscte.pcd.iscte_bay.user_app.dados.FileBlock;
import pt.iscte.pcd.iscte_bay.user_app.dados.FileDetails;
import pt.iscte.pcd.iscte_bay.user_app.dados.FilePart;
import pt.iscte.pcd.iscte_bay.user_app.thread.SingleBarrier;
import pt.iscte.pcd.iscte_bay.user_app.thread.TaskBlockingQueue;

/**
 * Lê blocos de 1024 bytes de um ficheiro, para que sejam transferidos entre clientes
 * e voltem a ser juntos e gravados ficando o ficheiro igual ao original
 * 
 * @author tomas
 */
public class FileDisassemble {
	private File fileToTransfer;
	private List<FilePart> fileParts;
	private List<FileBlock> fileBlocks;
	private byte[] fileContents;
	public static final int FILEBLOCKSIZE = 1024;

	public FileDisassemble(File _fileToTransfer) throws Exception {
		fileToTransfer = _fileToTransfer;
		fileBlocks = new ArrayList<FileBlock>();
		fileParts = new ArrayList<FilePart>();
		disassemble();
	}

	private void loadFile() throws Exception {
		fileContents = Files.readAllBytes(fileToTransfer.toPath());	
	}

	/**
	 * Cria os blocos de 1024 bytes em que o ficheiro será partido. 
	 * O último bloco tem como dimensão o resto do ficheiro
	 */
	private void createBlocks() {
		FileBlock block;
		int totalLength = (int) fileToTransfer.length();

		int lastBlockLength = totalLength % FILEBLOCKSIZE;
		int lastBlockOffset = totalLength - lastBlockLength;

		int offset = 0;

		while (offset < totalLength) {
			if (offset >= lastBlockOffset) {
				block = new FileBlock( lastBlockOffset, lastBlockLength);
			} else {
				block = new FileBlock( offset, FILEBLOCKSIZE); // 0-1023 ou 1-1024 ? 1024 incluido ?
			}
			offset = offset + FILEBLOCKSIZE ;
			fileBlocks.add(block);
		}
	}
	
	/**
	 * Cria a partes dos ficheiros de acordo com os blocos entretanto já criados
	 * @throws Exception 
	 */
	private void createFileParts() throws Exception {
		fileParts = new ArrayList<FilePart>();
		for (FileBlock fileBlock : fileBlocks) {
			FilePart filePart = createFilePart(fileBlock);
			fileParts.add(filePart);
		}
	}
	
	/**
	 * A partir do array global de ficheiros cria um bloco especifico para um ofset e tamanho
	 * @throws Exception 
	 */
	private FilePart createFilePart(FileBlock fileBlock) throws Exception {
		if ( fileBlocks == null )
			throw new Exception("Cannot break file into parts without Blocks created");
		
		byte[] fileBlockContent = new byte[fileBlock.getLength()];
		
		for ( int i = fileBlock.getOffset() ; i  <  fileBlock.getOffsetEnd() ; i++ ) { 
			int j = (int) (i -  fileBlock.getOffset());
			fileBlockContent[j] = fileContents[i];
		}
		FilePart f = new FilePart(fileBlockContent, fileBlock.getOffset(), fileBlock.getLength());
		return f;
	}
	
	private void disassemble() throws Exception {
		loadFile();
		createBlocks();
		createFileParts();		
	}

	public List<FilePart> getFileParts() {
		return fileParts;
	}

}
