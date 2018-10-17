package server.ftp.task.command;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;

import server.ftp.debug.Report;
import server.ftp.exception.authentication.FailToUpdateAuthenticationException;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.exception.file_manager.NoAccessToThisFileException;
import server.ftp.exception.file_manager.RemotePahtWrongFormatException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPDataTransfertManager;
import server.ftp.server.FTPDataTransfertManager.DataConnection;
import server.ftp.server.FTPExecutableDataConnection;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.ftp.task.executable.ArrayStringSender;
import server.ftp.task.executable.EmptyBlockSender;
import server.util.Tools;
import server.util.ErrorManager;
import server.util.FinishListener;

public class CommandLIST extends FTPCommand implements FinishListener {

	// Variables.

	private FTPClientConnection ftpClientConnection;

	private DataConnection dataConnection;

	private FTPExecutableDataConnection executableDataConnection;

	// Constants.

	public static final String NAME_CODE = "LIST";

	// Constructors.

	public CommandLIST(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
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

			report.offerAction("Fermeture de data connection.");

		} catch (IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);
	}

	@Override
	public void finishError() {

		Report report = new Report("FINISH WITH ERROR de Command : " + NAME_CODE);

		try {
			this.ftpClientConnection.write("451 error during transfert\n");
			this.ftpClientConnection.flush();

			report.offerAction("Envoie du message d'error.");

			FTPDataTransfertManager ftpDataTransfertManager = this.getFTPServerController()
					.getFTPDataTransfertManager();

			ftpDataTransfertManager.closeDataConnection(this.dataConnection);

			report.offerAction("Fermeture de data connection.");

		} catch (IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);
	}

	@Override
	public void run() {
		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		try {

			boolean good = false;

			FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

			if (ftpAuthenticationManager.isAlreadyAuthenticated(ftpClientConnection)) {

				this.ftpClientConnection = ftpClientConnection;

				FTPDataTransfertManager ftpDataTransfertManager = this.getFTPServerController()
						.getFTPDataTransfertManager();

				this.dataConnection = ftpDataTransfertManager.getWaitFreeDataConnectionFor(this.ftpClientConnection);

				FTPFileManager ftpFileManager = this.getFTPServerController().getFTPFileManager();

				try {
					ftpAuthenticationManager.updateQueryDoneNoAuthenticationVerification(ftpClientConnection);
				} catch (FailToUpdateAuthenticationException e) {

					report.offerAction("Error : " + e);
					report.setFailed();

					ErrorManager.writeError(e);
				}

				Path p = null;

				if (args == null) {

					p = this.ftpClientConnection.getCurrentWorkingDirectory();

					report.offerAction("Fichier pris a partir du working directory. P = " + p);

					good = true;

				} else if (args != null && args.length >= 1) {

					String stringPath = "";

					for (int i = 0; i < args.length; i++) {
						if (i < args.length - 1) {
							stringPath += args[i] + " ";
						} else {
							stringPath += args[i];
						}
					}

					try {
						p = ftpFileManager.convertToLocalPath(Paths.get(stringPath));
						good = true;

						report.offerAction("Fichier convertie en local = " + p);

					} catch (RemotePahtWrongFormatException e) {

						report.offerAction("Erreur d'arguments.");
						report.offerAction("Error : " + e);
						report.setFailed();

						this.ftpClientConnection.write("501 argument error)\n");
					}

				} else {

					good = false;

					report.offerAction("Erreur d'arguments.");
					report.setFailed();

					this.ftpClientConnection.write("501 argument error (too many arguments)\n");
				}

				if (good) {

					try {

						report.offerAction("Dans le if du good");

						List<Path> list;
						list = ftpFileManager.getListSubFile(p, ftpClientConnection);

						Vector<Path> listSubPath = null;

						if (list != null)
							listSubPath = new Vector<>(list);

						if (listSubPath != null && !listSubPath.isEmpty()) {

							String[] arrayString = new String[listSubPath.size()];

							for (int i = 0; i < listSubPath.size(); i++) {
								Path path = listSubPath.get(i);

								String stringPath = path.toString();

								try {
									stringPath = ftpFileManager.getPathDescriptionLongFormat(path);
								} catch (LocalPathIsNotServerPathException e) {

									report.offerAction("Erreur de convertion pour Path = " + path);

									ErrorManager.writeError(e);
								}

								arrayString[i] = stringPath;
							}

							report.offerAction("Array string cree = " + Tools.toStringArray(arrayString));

							this.executableDataConnection = new ArrayStringSender(ftpClientConnection, arrayString,
									this.dataConnection, this.getFTPServerController());

							report.offerAction("Creation d'un ARRAY STRING SENDER.");
						} else {
							this.executableDataConnection = new EmptyBlockSender(ftpClientConnection,
									this.dataConnection, this.getFTPServerController());

							report.offerAction("Creation d'un EMPTY BLOCK SENDER.");

						}

						this.ftpClientConnection.write("125 starting list transfert\n");

						report.offerAction("Envoie du message de commencement de transfert.");

					} catch (LocalPathIsNotServerPathException e) {

						report.offerAction("Le path n'est pas un path dans la zone de fichier du serveur. Path = " + p);
						report.offerAction("Error = " + e);
						report.setFailed();

						ErrorManager.writeError(e);

						this.ftpClientConnection.write("550 acces denied.\n");
					} catch (NoAccessToThisFileException e) {

						report.offerAction("Access refuser pour Path = " + p);
						report.offerAction("Error = " + e);
						report.setFailed();

						ErrorManager.writeError(e);

						this.ftpClientConnection.write("550 acces denied.\n");
					}
				}

			} else {

				report.offerAction("Non authentifie");
				report.setFailed();

				ftpClientConnection.write("530 not authenticated\n");
			}

			this.ftpClientConnection.flush();

			if (good) {

				report.offerAction("Dans le deuxieme if du good.");

				this.executableDataConnection.addFinishListener(this);
				this.getFTPServerController().executorExecute(this.executableDataConnection);

				report.offerAction("Lancement de l'execution du SENDER.");
			}

		} catch (IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);
	}

}
