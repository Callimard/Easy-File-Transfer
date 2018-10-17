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

public class CommandRNFR extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "RNFR";

	// Constructors.

	public CommandRNFR(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}

	// Methods.

	@Override
	public void run() {
		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		report.offerAction("Current working directory = " + this.getClientConnection().getCurrentWorkingDirectory());

		FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

		FTPFileManager ftpFileManager = this.getFTPServerController().getFTPFileManager();

		// TODO Verifier les droits.

		try {

			if (ftpAuthenticationManager.isAlreadyAuthenticated(this.getClientConnection())) {

				if (args != null && args.length >= 1) {

					String pathRenamedFile = "";

					for (int i = 0; i < args.length; i++) {
						if (i < args.length - 1) {
							pathRenamedFile += args[i] + " ";
						} else {
							pathRenamedFile += args[i];
						}
					}

					Path argPath = Paths.get(pathRenamedFile);

					Path workingPath = null;

					boolean good = false;

					// Absolute path.
					if (argPath.getRoot() != null) {

						workingPath = argPath;

						good = true;

						try {
							workingPath = ftpFileManager.convertToLocalPath(workingPath);

							if (!Files.exists(workingPath)) {
								good = false;

								this.getClientConnection().write("550 file not exists\n");
							}

						} catch (RemotePahtWrongFormatException e) {

							report.offerAction("Error : " + e);
							report.setFailed();

							ErrorManager.writeError(e);

							good = false;

							this.getClientConnection().write("550 file error\n");
						}

					} else {

						Path pathCWD = this.getClientConnection().getCurrentWorkingDirectory();

						workingPath = Paths.get(pathCWD.toString(), pathRenamedFile);

						good = true;
					}

					if (good) {

						try {
							if (ftpFileManager.writeAccess(workingPath.getParent(), this.getClientConnection())) {

								report.offerAction("PathWhichWillBeRenamed = " + workingPath);

								this.getClientConnection().setPathWhichWillBeRenamed(workingPath);

								this.getClientConnection().write("350 file ready to be renamed\n");

								report.offerAction("Fichier pret pour le rename.");
							} else {
								report.offerAction("Access refuser pour WorkingPath = " + workingPath);
								report.setFailed();

								this.getClientConnection().write("550 acces denied.\n");
							}
						} catch (LocalPathIsNotServerPathException e) {
							report.offerAction("Access refuser pour WorkingPath = " + workingPath);
							report.offerAction("Error = " + e);
							report.setFailed();

							ErrorManager.writeError(e);

							this.getClientConnection().write("550 acces denied.\n");
						}
					}

				} else {
					this.getClientConnection().write("501 argument error\n");

					report.offerAction("Erreure d'arguments.");
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

}
