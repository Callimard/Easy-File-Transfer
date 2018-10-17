package server.ftp.task.command;

import java.io.IOException;
import java.util.StringTokenizer;

import server.ftp.debug.Report;
import server.ftp.exception.authentication.FailToUpdateAuthenticationException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandPORT extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "PORT";

	private static final String COMA_SEPARATOR = ",";

	// Constructors.

	public CommandPORT(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}

	// Methods.

	@Override
	public void run() {
		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		// TODO

		try {

			FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

			if (ftpAuthenticationManager.isAlreadyAuthenticated(this.getClientConnection())) {

				try {
					ftpAuthenticationManager.updateQueryDoneNoAuthenticationVerification(this.getClientConnection());
				} catch (FailToUpdateAuthenticationException e) {

					report.offerAction("Error : " + e);
					report.setFailed();

					ErrorManager.writeError(e);
				}

				if (args != null && args.length >= 1) {

					StringTokenizer separator = new StringTokenizer(args[0], COMA_SEPARATOR);

					int i = 0;
					while (i < 4) {
						separator.nextToken();
						i++;
					}

					int bHigh = Integer.valueOf(separator.nextToken());
					int bLow = Integer.valueOf(separator.nextToken());

					int port = bHigh << 8;
					port += bLow;

					report.offerAction("Port = " + port);

					this.getClientConnection().write("200 port " + port + "\n");

					report.offerAction("Envoie du message d'acceptation de connection sur le port " + port + ".");

				} else {

					report.offerAction("Argument manquant.");
					report.setFailed();

					this.getClientConnection().write("501 miss argument\n");
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
