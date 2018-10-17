package server.ftp.task.command;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.exception.file_manager.RemotePahtWrongFormatException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPDataTransfertManager;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.ftp.server.FTPDataTransfertManager.DataConnection;
import server.ftp.task.executable.FileReceiver;
import server.util.Tools;
import server.util.ErrorManager;
import server.util.FinishListener;

public class CommandAPPE extends FTPCommand implements FinishListener {

	// Constants.

	public static final String NAME_CODE = "APPE";

	// Variables.

	private FTPClientConnection ftpClientConnection;

	private DataConnection dataConnection;

	// Constructors.

	public CommandAPPE(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}

	// Methods.
	
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

	@Override
	public void run() {
		// TODO Verifier les droits.

		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		report.offerAction("FTPClientConnection = " + ftpClientConnection);

		FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

		FTPDataTransfertManager ftpDataTransfertManager = this.getFTPServerController().getFTPDataTransfertManager();

		FTPFileManager ftpFileManager = this.getFTPServerController().getFTPFileManager();

		this.ftpClientConnection = ftpClientConnection;

		try {

			if (ftpAuthenticationManager.isAlreadyAuthenticated(ftpClientConnection)) {

				if (args != null && args.length >= 1) {

					String stringPath = "";

					for (int i = 0; i < args.length; i++) {
						if (i < args.length - 1) {
							stringPath += args[i] + " ";
						} else {
							stringPath += args[i];
						}
					}

					Path argPath = Paths.get(stringPath);

					Path localPath = null;

					try {

						if (FTPFileManager.REMOTE_ROOT_PATH.equals(argPath.getRoot())) {

							localPath = ftpFileManager.convertToLocalPath(argPath);
						} else {
							localPath = Paths.get(this.ftpClientConnection.getCurrentWorkingDirectory().toString(),
									stringPath);
						}

						try {

							this.dataConnection = ftpDataTransfertManager
									.getWaitFreeDataConnectionFor(this.ftpClientConnection);

							FileReceiver fileReceiver = new FileReceiver(localPath, this.ftpClientConnection,
									this.dataConnection, true, this.getFTPServerController());

							this.ftpClientConnection.write("125 starting file transfert\n");
							this.getFTPServerController().executorExecute(fileReceiver);

							fileReceiver.addFinishListener(this);

							report.offerAction("Lancement de l'execution du transfert.");

						} catch (IOException e) {

							ftpDataTransfertManager.closeDataConnection(this.dataConnection);

							report.offerAction("Erreur local de fichier. Erreur = " + e);
							report.setFailed();

							ErrorManager.writeError(e);

							this.ftpClientConnection.write("450 local error\n");
						} catch (LocalPathIsNotServerPathException e) {

							ftpDataTransfertManager.closeDataConnection(this.dataConnection);

							report.offerAction("Le path n'est pas un path dans la zone de fichier du serveur. Path = "
									+ localPath.getParent());
							report.offerAction("Error = " + e);
							report.setFailed();

							ErrorManager.writeError(e);

							this.ftpClientConnection.write("550 acces denied.\n");
						}

					} catch (RemotePahtWrongFormatException e) {

						report.offerAction("Problème de format du path. Erreur = " + e);
						report.setFailed();

						this.ftpClientConnection.write("553 paht format not allow^n");
					}

				} else {
					report.offerAction("Erreur d'argument.");
					report.setFailed();

					this.ftpClientConnection.write("501 argument error\n");
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

}
