package server.ftp.task.executable;

import java.io.IOException;

import server.ftp.debug.Report;
import server.ftp.server.FTPExecutableDataConnection;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPDataTransfertManager.DataConnection;
import server.util.Tools;
import server.ftp.server.FTPServerController;
import server.util.ErrorManager;

public class ArrayStringSender extends FTPExecutableDataConnection {

	// Variables.

	private String[] arrayString;

	// Public

	public ArrayStringSender(FTPClientConnection ftpClientConnection, String[] arrayString,
			DataConnection dataConnection, FTPServerController ftpServerController) {
		super(ftpClientConnection, dataConnection, ftpServerController);

		Report report = new Report("ArrayStringSender");

		report.offerAction("FTPClientConnection : " + ftpClientConnection);

		this.arrayString = arrayString;

		report.offerAction("ArrayString a envoyer = " + Tools.toStringArray(this.arrayString));

		this.getFTPServerController().getFTPActionReminder().addReportFor(ftpClientConnection, report);
	}

	// Methods.

	@Override
	public void run() {

		try {

			for (String string : this.arrayString) {
				this.getDataConnection().writeDataLine(string);
				this.getDataConnection().flush();
			}

			this.updateFinishListener();

			// Kill if the client connection is kill.
			this.getFTPServerController().getClientManager().killClient(this.getFTPClientConnection());

		} catch (IOException e) {
			ErrorManager.writeError(e);
		}

	}

}
