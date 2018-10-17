package server.ftp.task.executable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.PathDoesNotExistException;
import server.ftp.server.FTPExecutableDataConnection;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPDataTransfertManager.DataConnection;
import server.ftp.server.FTPServerController;
import server.util.ErrorManager;

public class FileSender extends FTPExecutableDataConnection {

	// Variables.

	private Path fileToTransfert;

	// Constructors.

	/**
	 * The path must exists. It's must be a local path.
	 * 
	 * @param fileToTransfert
	 * @param dataConnection
	 * @param ftpServerController
	 * @throws PathDoesNotExistException
	 * @throws IOException
	 */
	public FileSender(Path fileToTransfert, FTPClientConnection ftpClientConnection, DataConnection dataConnection,
			FTPServerController ftpServerController) throws PathDoesNotExistException, IOException {
		super(ftpClientConnection, dataConnection, ftpServerController);

		Report report = new Report("FileSender");

		report.offerAction("FTPClientConnection : " + ftpClientConnection);

		this.fileToTransfert = fileToTransfert;

		report.offerAction("FileToTransfert = " + this.fileToTransfert);

		if (!Files.exists(this.fileToTransfert)) {

			report.offerAction("Erreur, le file to transfer n'existe pas. FileToTransfert = " + this.fileToTransfert);
			report.setFailed();

			this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);

			throw new PathDoesNotExistException("FTPClientCOnnection = "
					+ this.getDataConnection().getFTPClientConnection() + " path = " + this.fileToTransfert);
		}

		System.err.println("Transfert pour la FTPConnection : " + this.getDataConnection().getFTPClientConnection()
				+ " du fichier : " + this.fileToTransfert);

		report.offerAction("Commencement du transfert de fichier.");

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);

	}

	@Override
	public void run() {
		try (BufferedReader fileReader = Files.newBufferedReader(this.fileToTransfert, StandardCharsets.ISO_8859_1)) {
			int nbCharRead = 0;
			int nb_char_read_max = 500;
			char[] charBuf = new char[nb_char_read_max];
			while ((nbCharRead = fileReader.read(charBuf)) != -1) {

				if (nbCharRead < nb_char_read_max) {

					char[] tmp = new char[nbCharRead];

					for (int i = 0; i < nbCharRead; i++) {
						tmp[i] = charBuf[i];
					}

					this.getDataConnection().writeData(tmp);
					this.getDataConnection().flush();

				} else {

					this.getDataConnection().writeData(charBuf);
					this.getDataConnection().flush();

				}
			}

			this.updateFinishListener();

			// Kill if the client connection is kill.
			this.getFTPServerController().getClientManager().killClient(this.getFTPClientConnection());

		} catch (IOException e) {

			ErrorManager.writeError(e);
			this.updateFinishErrorListener();

		}

	}

}
