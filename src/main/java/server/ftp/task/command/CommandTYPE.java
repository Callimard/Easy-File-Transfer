package server.ftp.task.command;

import java.io.IOException;

import server.ftp.debug.Report;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandTYPE extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "TYPE";

	// Constructors.

	public CommandTYPE(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
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

				switch (args[0]) {
				case "I":
					this.getClientConnection().write("200 TYPE Image\n");

					report.offerAction("Envoie de type image.");

					break;

				case "E":
					this.getClientConnection().write("200 TYPE EBCDIC\n");

					report.offerAction("Envoie de type EBCDIC.");

					break;

				case "L":
					this.getClientConnection().write("504 TYPE Local byte not supported\n");

					report.offerAction("Envoie de type local byte non supporte.");

					break;

				case "A":
					this.getClientConnection().write("200 TYPE ASCII\n");

					report.offerAction("Envoie de type ASCII.");

					break;

				default:
					this.getClientConnection().write("501 argument not recognized\n");

					report.offerAction("Envoie de type non reconnue.");

					break;
				}

			} else {

				report.offerAction("Non authentifie.");
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
