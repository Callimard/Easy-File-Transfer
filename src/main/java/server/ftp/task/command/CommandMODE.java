package server.ftp.task.command;

import java.io.IOException;

import server.ftp.debug.Report;
import server.ftp.exception.UnRecognizedModeException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandMODE extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "MODE";

	// Constructors.

	public CommandMODE(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
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

				if (args != null && args.length == 1) {

					report.offerAction("Mode = " + args[0]);

					try {
						this.getClientConnection().setMode(args[0]);
					} catch (UnRecognizedModeException e) {
						report.offerAction("Mode " + args[0] + " non reconnue.");
						report.setFailed();

						this.getClientConnection().write("504 mode unrecognized.\n");
					}
				} else {
					report.offerAction("Argument error.");
					report.setFailed();

					this.getClientConnection().write("501 argument error.\n");
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
