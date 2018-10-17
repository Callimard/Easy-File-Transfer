package server.ftp.test.main;

import java.nio.file.Path;
import java.nio.file.Paths;

import server.ftp.exception.WrongPathDescriptionFormatException;
import server.ftp.server.FTPFileAccess;
import server.ftp.server.FTPFileAccess.FileAccess;

public class FileAccessTest {

	public FileAccessTest() {
	}

	public static void main(String[] args) {

		Path path = Paths.get("E:\\Users\\Callimard\\UPMC\\Master\\Lettre de motivation.docx");

		FTPFileAccess ftpFileAccess = new FTPFileAccess(path, null, "troll", FileAccess.READ_WRITE_EXECUTE,
				FileAccess.READ_WRITE, FileAccess.READ_WRITE_EXECUTE);

		System.out.println(ftpFileAccess.getPathDescriptionLongFormat());

		try {
			System.out.println(FTPFileAccess.parseFromLongDescribe(Paths.get("E:\\Users\\Callimard\\UPMC\\Master"),
					ftpFileAccess.getPathDescriptionLongFormat()).getPathDescriptionLongFormat());
		} catch (WrongPathDescriptionFormatException e) {
			e.printStackTrace();
		}

	}

}
