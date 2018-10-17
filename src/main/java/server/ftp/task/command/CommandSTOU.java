package server.ftp.task.command;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.exception.file_manager.PathIsNotDirectoryException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPDataTransfertManager;
import server.ftp.server.FTPDataTransfertManager.DataConnection;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.ftp.task.executable.FileReceiver;
import server.util.Tools;
import server.util.ErrorManager;
import server.util.FinishListener;

public class CommandSTOU extends FTPCommand implements FinishListener {

	// Constants.

	public static final String NAME_CODE = "STOU";

	// Variables.

	private FTPClientConnection ftpClientConnection;

	private DataConnection dataConnection;

	// Constructors.

	public CommandSTOU(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}

	// Methods.

	@Override
	public void run() {

		// TODO Verifier les droits.

		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		report.offerAction("FTPClientConnection = " + ftpClientConnection);

		FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

		FTPDataTransfertManager ftpDataTransfertManager = this.getFTPServerController().getFTPDataTransfertManager();

		this.ftpClientConnection = ftpClientConnection;

		try {

			if (ftpAuthenticationManager.isAlreadyAuthenticated(this.ftpClientConnection)) {

				try {
					String generatedNameFile = FTPFileManager
							.generateUniqueFileName(this.ftpClientConnection.getCurrentWorkingDirectory());

					report.offerAction("GeneratedFileName = " + generatedNameFile);

					Path path = Paths.get(this.ftpClientConnection.getCurrentWorkingDirectory().toString(),
							generatedNameFile);

					this.dataConnection = ftpDataTransfertManager
							.getWaitFreeDataConnectionFor(this.ftpClientConnection);

					try {
						FileReceiver fileReceiver = new FileReceiver(path, this.ftpClientConnection,
								this.dataConnection, false, this.getFTPServerController());

						this.ftpClientConnection.write("125 (" + generatedNameFile + ")\n");
						this.getFTPServerController().executorExecute(fileReceiver);

						fileReceiver.addFinishListener(this);

					} catch (LocalPathIsNotServerPathException e) {
						report.offerAction(
								"Le path n'est pas un path dans la zone de fichier du serveur. Path = " + path);
						report.offerAction("Error = " + e);
						report.setFailed();

						ErrorManager.writeError(e);

						this.ftpClientConnection.write("550 acces denied.\n");
					}

				} catch (PathIsNotDirectoryException e) {

					if (this.dataConnection != null)
						ftpDataTransfertManager.closeDataConnection(this.dataConnection);

					report.offerAction("Gros beug.");
					report.setFailed();

					this.ftpClientConnection.write("553 the current working directory is not a directory.\n");

				}

			} else {
				report.offerAction("Non authentifie");
				report.setFailed();

				this.ftpClientConnection.write("530 not logged in\n");
			}

			this.ftpClientConnection.flush();
		} catch (IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);
	}

	@Override
	public void finish() {
		Report report = new Report("FINISH de Command : " + NAME_CODE);

		try {
			this.ftpClientConnection.write("226 success\n");
			this.ftpClientConnection.flush();

			report.offerAction("Envoie du message de succes.");

			FTPDataTransfertManager ftpDataTransfertManager = this.getFTPServerController()
					.getFTPDataTransfertManager();

			ftpDataTransfertManager.closeDataConnection(this.dataConnection);

			report.offerAction("Fermeture de la data connection");

		} catch (IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);
	}

	@Override
	public void finishError() {
		Report report = new Report("FINISH ERROR de Command : " + NAME_CODE);

		report.setFailed();

		try {
			this.ftpClientConnection.write("451 error during transfert\n");
			this.ftpClientConnection.flush();

			report.offerAction("Envoie du message d'echec.");

			FTPDataTransfertManager ftpDataTransfertManager = this.getFTPServerController()
					.getFTPDataTransfertManager();

			ftpDataTransfertManager.closeDataConnection(this.dataConnection);

			report.offerAction("Fermeture de la data connection");

		} catch (IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);
	}

}
