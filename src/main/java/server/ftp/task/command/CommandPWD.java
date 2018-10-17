package server.ftp.task.command;

import java.io.IOException;
import java.nio.file.Path;

import server.ftp.debug.Report;
import server.ftp.exception.authentication.FailToUpdateAuthenticationException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandPWD extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "PWD";

	// Constructors.

	public CommandPWD(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
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

				try {
					ftpAuthenticationManager.updateQueryDoneNoAuthenticationVerification(this.getClientConnection());
				} catch (FailToUpdateAuthenticationException e) {

					report.offerAction("Error : " + e);
					report.setFailed();

					ErrorManager.writeError(e);
				}

				Path pathWD = this.getClientConnection().getCurrentWorkingDirectory();

				report.offerAction("PathWD = " + pathWD);

				if (pathWD != null) {

					String stringToReplace = pathWD.toString();
					String path = stringToReplace.replace(ftpFileManager.STRING_PATH_LOCALISATION_FTP_DATA,
							FTPFileManager.REMOTE_ROOT_STRING);

					this.getClientConnection().write("257 \"" + path + "\"\n");

					report.offerAction("Envoie du path = " + path);

				} else {

					String pseudo = ftpAuthenticationManager.getPseudo(this.getClientConnection());

					this.getClientConnection()
							.write("257 \"" + ftpFileManager.getRemoteUserWorkspaceDirectoryPath(pseudo) + "\"\n");

					report.offerAction("Envoie du message = 257 " + FTPFileManager.REMOTE_USERS_DIRECTORY_PATH
							+ FTPFileManager.FILE_SEPARATOR + pseudo);

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
