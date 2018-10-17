package server.ftp.task.command;

import java.io.IOException;
import java.util.StringTokenizer;

import server.ftp.debug.Report;
import server.ftp.exception.authentication.FailToUpdateAuthenticationException;
import server.ftp.exception.data_transfert_manager.NoDataPortFreeException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPDataTransfertManager;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;
import server.util.NetworkTools;

public class CommandPASV extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "PASV";

	// Constructors.

	public CommandPASV(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
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

				try {
					ftpAuthenticationManager.updateQueryDoneNoAuthenticationVerification(this.getClientConnection());
				} catch (FailToUpdateAuthenticationException e) {

					report.offerAction("Error : " + e);
					report.setFailed();

					ErrorManager.writeError(e);
				}

				FTPDataTransfertManager ftpDataTransfertManager = this.getFTPServerController()
						.getFTPDataTransfertManager();

				try {
					int port = ftpDataTransfertManager.openDataConnectionFor(this.getClientConnection());

					report.offerAction("Port attribue = " + port);

					int portLeft = port >> 8;
					int portRight = port - (portLeft << 8);

					report.offerAction("PortLeft = " + portLeft + " PortRight = " + portRight);

					String externalAddress = NetworkTools.getBoxIpAddress();

					StringTokenizer separator = new StringTokenizer(externalAddress, ".");

					String ip4 = separator.nextToken();
					String ip3 = separator.nextToken();
					String ip2 = separator.nextToken();
					String ip1 = separator.nextToken();

					this.getClientConnection().write("227 enter in passiv mode (" + ip4 + "," + ip3 + "," + ip2 + "," + ip1
							+ "," + portLeft + "," + portRight + ")\n");

					report.offerAction("Envoie du message de mode PASSIV OKAY. (" + ip4 + "," + ip3 + "," + ip2 + ","
							+ ip1 + "," + portLeft + "," + portRight + ")");

				} catch (NoDataPortFreeException e) {

					report.offerAction("Pas de port de data connection libre.");
					report.offerAction("Error : " + e);
					report.setFailed();

					this.getClientConnection().write("421 no port for data connection free\n");
				}

			} else {

				report.offerAction("Pas authentifie.");
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
