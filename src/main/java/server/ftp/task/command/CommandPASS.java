package server.ftp.task.command;

import java.io.IOException;

import server.ftp.debug.Report;
import server.ftp.exception.authentication.AlreadyAuthenticatedClientException;
import server.ftp.exception.authentication.AuthenticationFailedException;
import server.ftp.exception.authentication.NotPreparedException;
import server.ftp.exception.authentication.WrongPasswordException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandPASS extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "PASS";

	// Constructors.

	public CommandPASS(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}

	// mMthods.

	@Override
	public void run() {
		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		boolean close = false;

		try {

			FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

			try {

				String pseudo = ftpAuthenticationManager.getPseudo(this.getClientConnection());

				report.offerAction("Pseudo = " + pseudo);

				report.offerAction("Password = " + args[0]);

				ftpAuthenticationManager.finalizeAuthentication(this.getClientConnection(), args[0]);

				FTPFileManager ftpFileManager = this.getFTPServerController().getFTPFileManager();

				this.getClientConnection().setCurrentWorkingDirectory(ftpFileManager.getLocalUserWorkspaceDirectoryPath(pseudo));

				this.getClientConnection().setPseudo(pseudo);

				this.getClientConnection().write("230 logged in\n");

				report.offerAction("Log reussi pour " + pseudo + ".");

			} catch (NotPreparedException e) {

				report.offerAction("Connection non preparee.");
				report.offerAction("Error : " + e);
				report.setFailed();

				this.getClientConnection().write("503 Not prepared\n");
				close = true;
			} catch (AlreadyAuthenticatedClientException e) {

				report.offerAction("Connection deja authentifie");
				report.offerAction("Error : " + e);
				report.setFailed();

				this.getClientConnection().write("530 Already authenticated\n");
				close = true;
			} catch (WrongPasswordException e) {

				report.offerAction("Mauvais password.");
				report.offerAction("Error : " + e);
				report.setFailed();

				this.getClientConnection().write("501 Wrong password\n");
				close = true;
			} catch (AuthenticationFailedException e) {

				report.offerAction("Echec de l'authentification.");
				report.offerAction("Error : " + e);
				report.setFailed();

				this.getClientConnection().write("530 Authentication failed\n");
				close = true;
			}

			this.getClientConnection().flush();

			if (close) {
				this.getClientConnection().close();

				report.offerAction("Fermeture de la connection.");
				report.setFailed();
			}

		} catch (IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(this.getClientConnection(), report);
	}

}
