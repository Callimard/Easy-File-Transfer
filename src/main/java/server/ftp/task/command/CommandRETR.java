package server.ftp.task.command;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.exception.file_manager.PathDoesNotExistException;
import server.ftp.exception.file_manager.RemotePahtWrongFormatException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPDataTransfertManager;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPDataTransfertManager.DataConnection;
import server.ftp.server.FTPServerController;
import server.ftp.task.executable.FileSender;
import server.util.Tools;
import server.util.ErrorManager;
import server.util.FinishListener;

public class CommandRETR extends FTPCommand implements FinishListener {

	// Constants.

	public static final String NAME_CODE = "RETR";

	private DataConnection dataConnection;

	// Constructors.

	public CommandRETR(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}

	// Methods.

	@Override
	public void finishError() {

		Report report = new Report("FINISH ERROR de Command : " + NAME_CODE);

		report.setFailed();

		try {
			this.getClientConnection().write("451 error during transfert\n");
			this.getClientConnection().flush();

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

		this.getFTPServerController().getFTPActionReminder().addReportFor(this.getClientConnection(), report);
	}

	@Override
	public void run() {
		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		report.offerAction("Current working directory = " + this.getClientConnection().getCurrentWorkingDirectory());

		FTPDataTransfertManager ftpDataTransfertManager = this.getFTPServerController().getFTPDataTransfertManager();

		this.dataConnection = ftpDataTransfertManager.getWaitFreeDataConnectionFor(this.getClientConnection());

		FTPFileManager ftpFileManager = this.getFTPServerController().getFTPFileManager();

		Path fileToTransfert = null;

		try {

			FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

			if (ftpAuthenticationManager.isAlreadyAuthenticated(this.getClientConnection())) {

				String stringPath = "";

				for (int i = 0; i < args.length; i++) {
					if (i < args.length - 1) {
						stringPath += args[i] + " ";
					} else {
						stringPath += args[i];
					}
				}

				Path argPath = Paths.get(stringPath);

				if (args != null && args.length >= 1) {

					try {

						if (argPath.getRoot() != null) {
							fileToTransfert = ftpFileManager.convertToLocalPath(Paths.get(argPath.toString()));

							report.offerAction("FileTorTransfer = " + fileToTransfert);

						} else {
							Path pathWD = this.getClientConnection().getCurrentWorkingDirectory();

							report.offerAction("PathWD = " + pathWD);

							fileToTransfert = Paths.get(pathWD.toString(), argPath.toString());

							report.offerAction("FileTorTransfer = " + fileToTransfert);
						}

						report.offerAction("FileToTransfer = " + fileToTransfert);

						if (ftpFileManager.readAccess(fileToTransfert, this.getClientConnection())) {

							FileSender fileSender = new FileSender(fileToTransfert, this.getClientConnection(),
									this.dataConnection, this.getFTPServerController());

							this.getClientConnection().write("125 starting file transfert\n");

							fileSender.addFinishListener(this);
							this.getFTPServerController().executorExecute(fileSender);

							report.offerAction("Lancement de l'execution du transfert.");
						} else {
							report.offerAction("Access refuser pour FileToTransfert = " + fileToTransfert);
							report.setFailed();

							ftpDataTransfertManager.closeDataConnection(this.dataConnection);

							this.getClientConnection().write("550 acces denied.\n");
						}

					} catch (IOException | PathDoesNotExistException | RemotePahtWrongFormatException e) {

						report.offerAction("Error : " + e);
						report.setFailed();

						ftpDataTransfertManager.closeDataConnection(this.dataConnection);

						ErrorManager.writeError(e);

						this.getClientConnection().write("550 file not exists\n");
					} catch (LocalPathIsNotServerPathException e) {
						report.offerAction("Access refuser pour FileToTransfert = " + fileToTransfert);
						report.offerAction("Error = " + e);
						report.setFailed();

						ftpDataTransfertManager.closeDataConnection(this.dataConnection);

						ErrorManager.writeError(e);

						this.getClientConnection().write("550 acces denied.\n");
					}

				} else {

					report.offerAction("Erreur d'argument.");
					report.setFailed();

					this.getClientConnection().write("501 argument error\n");
				}

			} else {

				report.offerAction("Non authentifie");
				report.setFailed();

				this.getClientConnection().write("530 not authenticated\n");
			}

			this.getClientConnection().flush();
		} catch (IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(this.getClientConnection(), report);

	}

	@Override
	public void finish() {

		Report report = new Report("FINISH de Command : " + NAME_CODE);

		try {
			this.getClientConnection().write("226 success\n");
			this.getClientConnection().flush();

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

		this.getFTPServerController().getFTPActionReminder().addReportFor(this.getClientConnection(), report);
	}

}
