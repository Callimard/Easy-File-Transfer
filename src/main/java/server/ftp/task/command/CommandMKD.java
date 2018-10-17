package server.ftp.task.command;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.exception.file_manager.NoAccessToThisFileException;
import server.ftp.exception.file_manager.RemotePahtWrongFormatException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandMKD extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "MKD";

	// Constructors.

	public CommandMKD(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}

	// Mehtods.

	@Override
	public void run() {
		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		try {

			FTPAuthenticationManager ftpAuthenticaionManager = FTPAuthenticationManager.getInstance();

			FTPFileManager ftpFileManager = this.getFTPServerController().getFTPFileManager();

			if (ftpAuthenticaionManager.isAlreadyAuthenticated(this.getClientConnection())) {

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

					Path workingPath = null;

					boolean good = false;

					// Absolute Path
					if (argPath.getRoot() != null) {

						report.offerAction(
								"Dans le if apres verificartion de si on etait au niveau de REMOTE_ROOT_PATH");

						// Absolute path.

						try {

							workingPath = ftpFileManager.convertToLocalPath(argPath.getParent());

							report.offerAction("LocalPath = " + workingPath);

							good = true;

						} catch (RemotePahtWrongFormatException e) {

							report.offerAction("Les fichier parent n'existe pas. LocalPath = " + workingPath);
							report.offerAction("Error : " + e);
							report.setFailed();

							good = false;

							this.getClientConnection().write("550 parent file not exists.");
						}

					} else {

						report.offerAction(
								"Dans le else apres verificartion de si on etait au niveau de REMOTE_ROOT_PATH");

						// Not absolute path.

						workingPath = Paths.get(this.getClientConnection().getCurrentWorkingDirectory().toString(),
								argPath.toString());

						report.offerAction("LocalPath genere = " + workingPath);

						good = true;

					}

					if (good) {

						try {
							ftpFileManager.createDirectory(workingPath, this.getClientConnection());

							report.offerAction("Creation du nouveau repertoir OKAY.");

							this.getClientConnection().write("257 directory has been created\n");

						} catch (IOException e) {

							report.offerAction("Error : " + e);
							report.setFailed();

							this.getClientConnection().write("421 error occured\n");
						} catch (NoAccessToThisFileException e) {
							report.offerAction(
									"Le path n'est pas un path dans la zone de fichier du serveur. WorkingPath = "
											+ workingPath);
							report.offerAction("Error = " + e);
							report.setFailed();

							ErrorManager.writeError(e);

							this.getClientConnection().write("550 acces denied.\n");
						} catch (LocalPathIsNotServerPathException e) {
							report.offerAction("Access refuser pour WorkingPath = " + workingPath);
							report.offerAction("Error = " + e);
							report.setFailed();

							ErrorManager.writeError(e);

							this.getClientConnection().write("550 acces denied.\n");
						}

					}
				} else {

					report.offerAction("Erreur d'argument, il fait un seul argument.");
					report.setFailed();

					this.getClientConnection().write("501 arguments error\n");
				}

			} else {

				report.offerAction("Non authentifier");
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

}
