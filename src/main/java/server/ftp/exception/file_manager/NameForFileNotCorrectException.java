package server.ftp.exception.file_manager;

public class NameForFileNotCorrectException extends Exception {

	public NameForFileNotCorrectException(String nameFile) {
		super("Nom du fichier : " + nameFile);
	}
	
}
