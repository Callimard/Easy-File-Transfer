package server.ftp.task.executable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPDataTransfertManager.DataConnection;
import server.ftp.server.FTPExecutableDataConnection;
import server.ftp.server.FTPFileAccess;
import server.ftp.server.FTPFileAccess.FileAccess;
import server.ftp.server.FTPServerController;
import server.util.ErrorManager;

public class FileReceiver extends FTPExecutableDataConnection {

	// Constants.

	private static final int READ_CHAR_BUF_SIZE = 500;

	// Variables.

	private Path fileToReceived;

	private boolean append;

	// Constructors.

	public FileReceiver(Path fileToReceived, FTPClientConnection ftpClientConnection, DataConnection dataConnection,
			boolean append, FTPServerController ftpServerController)
			throws IOException, LocalPathIsNotServerPathException {
		super(ftpClientConnection, dataConnection, ftpServerController);

		Report report = new Report("FileReceiver");

		report.offerAction("FTPClientConnection : " + ftpClientConnection);

		this.fileToReceived = fileToReceived;

		report.offerAction("FileToReceived = " + this.fileToReceived);

		this.append = append;

		this.getFTPServerController().getFTPFileManager().putFile(this.fileToReceived,
				new FTPFileAccess(this.fileToReceived, ftpClientConnection.getPseudo(), ftpClientConnection.getPseudo(),
						FileAccess.READ_WRITE_EXECUTE, FileAccess.READ_EXECUTE, FileAccess.NOTHING));

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);
	}

	// Methods.

	@Override
	public void run() {

		if (!this.append) {

			try (BufferedWriter fileWriter = Files.newBufferedWriter(this.fileToReceived,
					StandardCharsets.ISO_8859_1)) {

				char[] cbuf = new char[READ_CHAR_BUF_SIZE];

				@SuppressWarnings("unused")
				int nbCharRead = -1;
				while (!this.getDataConnection().isClosed()
						&& (nbCharRead = this.getDataConnection().readCharBufBlock(cbuf)) != -1) {

					if (cbuf != null)
						fileWriter.write(cbuf);
				}

				this.updateFinishListener();

				// Kill if the client connection is kill.
				this.getFTPServerController().getClientManager().killClient(this.getFTPClientConnection());

			} catch (IOException e) {
				this.updateFinishErrorListener();

				ErrorManager.writeError(e);
			}

		} else {
			try (BufferedWriter fileWriter = Files.newBufferedWriter(this.fileToReceived, StandardCharsets.ISO_8859_1,
					StandardOpenOption.APPEND)) {

				char[] cbuf = new char[READ_CHAR_BUF_SIZE];

				@SuppressWarnings("unused")
				int nbCharRead = -1;
				while (!this.getDataConnection().isClosed()
						&& (nbCharRead = this.getDataConnection().readCharBufBlock(cbuf)) != -1) {

					if (cbuf != null)
						fileWriter.write(cbuf);
				}

				this.updateFinishListener();

				// Kill if the client connection is kill.
				this.getFTPServerController().getClientManager().killClient(this.getFTPClientConnection());

			} catch (IOException e) {
				this.updateFinishErrorListener();

				ErrorManager.writeError(e);
			}
		}

	}

}
