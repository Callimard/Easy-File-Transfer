package server.ftp.server;

import java.util.Vector;

import server.ftp.server.FTPDataTransfertManager.DataConnection;
import server.util.FinishListener;

/**
 * <p>
 * This class execute treatment on the data connection.
 * </p>
 * <p>
 * This class can read or write on the data connection and can also close it
 * etc.
 * </p>
 * 
 * @author Callimard
 *
 */
public abstract class FTPExecutableDataConnection implements Runnable {

	// Variables.

	private FTPClientConnection ftpClientConnection;

	private FTPServerController ftpServerController;

	private DataConnection dataConnection;

	private Vector<FinishListener> listFinishListener = new Vector<>();

	// Constructors.

	public FTPExecutableDataConnection(FTPClientConnection ftpClientConnection, DataConnection dataConnection,
			FTPServerController ftpServerController) {
		this.ftpClientConnection = ftpClientConnection;
		this.ftpServerController = ftpServerController;
		this.dataConnection = dataConnection;
	}

	// Methods.

	protected void updateFinishListener() {
		for (FinishListener finishListener : this.listFinishListener) {
			finishListener.finish();
		}
	}

	protected void updateFinishErrorListener() {
		for (FinishListener finishListener : this.listFinishListener) {
			finishListener.finishError();
		}
	}

	public void addFinishListener(FinishListener finishListener) {
		this.listFinishListener.add(finishListener);
	}

	public void removeFinishListener(FinishListener finishListener) {
		this.listFinishListener.remove(finishListener);
	}

	// Getters and Setters.

	public FTPClientConnection getFTPClientConnection() {
		return this.ftpClientConnection;
	}

	public FTPServerController getFTPServerController() {
		return this.ftpServerController;
	}

	public DataConnection getDataConnection() {
		return this.dataConnection;
	}
}
