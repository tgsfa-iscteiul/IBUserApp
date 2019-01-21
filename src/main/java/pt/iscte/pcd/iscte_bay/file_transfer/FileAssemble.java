package pt.iscte.pcd.iscte_bay.file_transfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import pt.iscte.pcd.iscte_bay.user_app.dados.FileDetails;
import pt.iscte.pcd.iscte_bay.user_app.dados.FilePart;

/**
 * Recebendo uma lista dos blocos de bytes que correspondem a um ficheiro transferido por 
 * partes, volta a juntar tudo num array que escreve num ficheiro de destino.
 * 
 * @author tomas
 *
 */
public class FileAssemble {
	private List<FilePart> fileParts;
	private FileDetails fileDetails;

	public FileAssemble(FileDetails _fileDetails, List<FilePart> _fileParts) {
		fileDetails = _fileDetails;
		fileParts = _fileParts;
	}

	/**
	 * Junta os blocos e escreve o ficheiro de destino
	 * @throws Exception 
	 */
	public void assembleFile() throws Exception {
		byte[] fileBytes = new byte[(int)fileDetails.getFileSize()];
		int i = 0;
		for (FilePart filePart : fileParts) {
			byte[] part = filePart.getFilePart();
			for (int j = 0 ; j < filePart.getSize() ; j++ ) {
				fileBytes[i] =  part[j];
				i++;
			}
		}
		Files.write(Paths.get("/tmp/" + fileDetails.getFileName()),fileBytes);
	}

}
