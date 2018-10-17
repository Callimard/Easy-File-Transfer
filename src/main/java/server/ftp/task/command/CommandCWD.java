package server.ftp.task.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.exception.file_manager.RemotePahtWrongFormatException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandCWD extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "CWD";

	// Constructors.

	public CommandCWD(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}
	
	// Methods.

	@Override
	public void run() {
		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		try {

			FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

			if (ftpAuthenticationManager.isAlreadyAuthenticated(this.getClientConnection())) {

				FTPFileManager ftpFileManager = this.getFTPServerController().getFTPFileManager();

				if (args != null && args.length >= 1) {

					// TODO Verify correctly access with FTPFileManager.

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

					// Absolute path
					if (argPath.getRoot() != null) {
						try {

							workingPath = ftpFileManager.convertToLocalPath(argPath);

							good = true;

						} catch (RemotePahtWrongFormatException e) {

							good = false;

							report.offerAction("Probleme de convertion de path en local path. ArgPath = " + argPath);
							report.offerAction("Error : " + e);
							report.setFailed();

							this.getClientConnection().write("550 path not exits\n");
						}
					} else {
						workingPath = Paths.get(this.getClientConnection().getCurrentWorkingDirectory().toString(),
								argPath.toString());

						good = true;
					}

					if (good) {
						try {

							report.offerAction("WorkingPath = " + workingPath);

							if (workingPath == null || !Files.exists(workingPath)) {

								report.offerAction("Le path n'existe pas. Path = " + workingPath);
								report.setFailed();

								this.getClientConnection().write("550 path does not exist\n\n");

								workingPath = null;

							} else {
								if (ftpFileManager.readAccess(workingPath, this.getClientConnection())) {

									this.getClientConnection().setCurrentWorkingDirectory(workingPath);

									report.offerAction(
											"changement de working directory OKAY. Working Directory = " + workingPath);

									this.getClientConnection().write("250 change working directory done\n");

								} else {

									report.offerAction("Acces refuse.");
									report.setFailed();

									this.getClientConnection().write("550 access denied\n");
								}

							}

						} catch (LocalPathIsNotServerPathException e) {
							report.offerAction("Erreur de path qui n'est pas dans la bonne zone. ArgPath = " + argPath);
							report.setFailed();

							ErrorManager.writeError(e);

							this.getClientConnection().write("550 access denied\n");
						}
					}
				} else {

					report.offerAction("Erreur d'arguments.");
					report.setFailed();

					this.getClientConnection().write("501 argument error\n");
				}

			} else {

				report.offerAction("Non authentifi�.");
				report.setFailed();

				this.getClientConnection().write("530 not authenticated\n");
			}

			this.getClientConnection().flush();
		} catch (

		IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(this.getClientConnection(), report);
	}

}
