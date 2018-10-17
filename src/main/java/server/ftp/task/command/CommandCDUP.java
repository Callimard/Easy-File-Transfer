package server.ftp.task.command;

import java.io.IOException;
import java.nio.file.Path;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandCDUP extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "CDUP";

	// Constructors.

	public CommandCDUP(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
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

				Path pathWD = this.getClientConnection().getCurrentWorkingDirectory();

				report.offerAction("Current directory = " + pathWD);

				try {
					if (ftpFileManager.readAccess(pathWD.getParent(), this.getClientConnection())) {

						this.getClientConnection().setCurrentWorkingDirectory(pathWD.getParent());

						report.offerAction("Changement de repertoire OKAY. REP = " + pathWD.getParent());

						this.getClientConnection().write("200 change to parent directory okay\n");

					} else {

						report.offerAction("Acces refus�.");
						report.setFailed();

						this.getClientConnection().write("550 access denied\n");
					}
				} catch (LocalPathIsNotServerPathException e) {
					report.offerAction("Erreur de path qui n'est pas dans la bonne zone. PathParent = "
							+ pathWD.getParent() + " pathWD = " + pathWD);
					report.setFailed();

					this.getClientConnection().write("550 access denied\n");

					ErrorManager.writeError(e);
				}

			} else {

				report.offerAction("Pas authentifi�");
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
