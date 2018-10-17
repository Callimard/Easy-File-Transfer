package server.ftp.task.command;

import java.io.IOException;

import server.ftp.debug.Report;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandSYST extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "SYST";

	// Constructors.

	public CommandSYST(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}
	// Methods.

	@Override
	public void run() {

		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		try {

			this.getClientConnection().write("215 " + System.getProperty("os.name") + "\n");

			report.offerAction("Envoie du message indicant le system. Sytem = " + System.getProperty("os.name"));

			this.getClientConnection().flush();
		} catch (IOException e) {

			report.offerAction("Error : " + e);
			report.setFailed();

			ErrorManager.writeError(e);
		}

		this.getFTPServerController().getFTPActionReminder().addReportFor(this.getClientConnection(), report);
	}

}
