package server.ftp.task.executable;

import java.io.IOException;

import server.ftp.debug.Report;
import server.ftp.server.FTPExecutableDataConnection;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPDataTransfertManager.DataConnection;
import server.ftp.server.FTPServerController;
import server.util.ErrorManager;

public class EmptyBlockSender extends FTPExecutableDataConnection {

	// Constructors.

	public EmptyBlockSender(FTPClientConnection ftpClientConnection, DataConnection dataConnection,
			FTPServerController ftpServerController) {
		super(ftpClientConnection, dataConnection, ftpServerController);

		Report report = new Report("EmptyBlockSender");

		report.offerAction("FTPClientConnection : " + ftpClientConnection);

		report.offerAction("Envoie d'un empty string.");

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);

	}

	// Methods.

	@Override
	public void run() {
		try {
			this.getDataConnection().writeData("\n");
			this.getDataConnection().flush();

			this.updateFinishListener();

			// Kill if the client connection is kill.
			this.getFTPServerController().getClientManager().killClient(this.getFTPClientConnection());
			
		} catch (IOException e) {
			ErrorManager.writeError(e);
		}
	}

}
