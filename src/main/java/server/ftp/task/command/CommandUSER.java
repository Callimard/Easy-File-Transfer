package server.ftp.task.command;

import java.io.IOException;

import server.ftp.debug.Report;
import server.ftp.exception.authentication.AlreadyPreparedException;
import server.ftp.exception.authentication.PrepareAuthenticationFailException;
import server.ftp.exception.authentication.WrongPseudoException;
import server.ftp.exception.authentication.WrongPseudoFormatException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandUSER extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "USER";

	// Constructors.

	public CommandUSER(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}

	// Methods.

	@Override
	public void run() {

		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		try {
			FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

			try {
				ftpAuthenticationManager.prepareAuthentication(this.getClientConnection(), args[0]);

				this.getClientConnection().write("331 need password\n");

				report.offerAction("Preparation de l'authentification reussi. Demande de password");

			} catch (AlreadyPreparedException e) {

				report.offerAction("Connection Deja prepare.");
				report.offerAction("Error : " + e);
				report.setFailed();

				this.getClientConnection().write("530 already prepared\n");
			} catch (WrongPseudoFormatException e) {

				report.offerAction("Format du Pseudo non reconnu.");
				report.offerAction("Error : " + e);
				report.setFailed();

				this.getClientConnection().write("501 wrong pseudo format\n");
			} catch (PrepareAuthenticationFailException e) {

				report.offerAction("Echec de la preparation de l'authentification.");
				report.offerAction("Error : " + e);
				report.setFailed();

				this.getClientConnection().write("530 big error\n");
			} catch (WrongPseudoException e) {

				report.offerAction("Pseudo non reconnu.");
				report.offerAction("Error : " + e);
				report.setFailed();

				this.getClientConnection().write("501 unknown pseudo\n");
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
