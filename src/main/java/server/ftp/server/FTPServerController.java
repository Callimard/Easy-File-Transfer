package server.ftp.server;

import java.io.IOException;

import server.ftp.exception.FatalErrorException;
import server.ftp.exception.command_executor.RunnerThreadAlreadyStartedException;
import server.ftp.exception.command_executor.RunnerThreadAlreadyStoppedException;
import server.ftp.exception.data_transfert_manager.EndPortLessThanBeginPortException;
import server.ftp.window.MainWindow;
import server.util.ErrorManager;
import server.util.easy_server.ServerController;

public class FTPServerController extends ServerController<FTPClientConnection> {

	// Constants.

	public static final char C_EOF = 26;
	public static final char C_EOR = 4;

	public static final int EOF = 0b1111_1111_0000_0010;
	public static final int EOR = 0b1111_1111_0000_0001;

	private final int CONNECTION_PORT;

	private final int BEGIN_AUTHORIZED_DATA_TRANSFERT_PORT;
	private final int END_AUTHORIZED_DATA_TRANSFERT_PORT;

	// Variables.

	private final MainWindow mainFrame;

	private final FTPClientCreator ftpClientCreator;
	private final FTPFileManager ftpFileManager;
	private final FTPDataTransfertManager ftpDataTransfertManager;
	private final FTPCommandExecutor ftpCommandExecutor;
	private final FTPActionReminder ftpActionReminder;

	// Constructors.

	public FTPServerController(int connectionPort, int beginAuthorizedDataPort, int endAuthorizedDataPort,
			MainWindow mainFrame) throws IOException, EndPortLessThanBeginPortException, FatalErrorException {

		this.mainFrame = mainFrame;

		this.CONNECTION_PORT = connectionPort;

		this.BEGIN_AUTHORIZED_DATA_TRANSFERT_PORT = beginAuthorizedDataPort;
		this.END_AUTHORIZED_DATA_TRANSFERT_PORT = endAuthorizedDataPort;

		this.ftpClientCreator = new FTPClientCreator(this);

		this.ftpFileManager = new FTPFileManager("E:\\Users\\Callimard\\FTP_server_file");

		this.ftpDataTransfertManager = new FTPDataTransfertManager(this.BEGIN_AUTHORIZED_DATA_TRANSFERT_PORT,
				this.END_AUTHORIZED_DATA_TRANSFERT_PORT, this);

		this.ftpCommandExecutor = new FTPCommandExecutor();

		this.setClientManager(new FTPClientManager(5, this));
		this.setServerSocketThread(new FTPServerSocketThread(this.CONNECTION_PORT, this));

		this.ftpActionReminder = new FTPActionReminder();
	}

	// Methods.

	@Override
	protected void inStart() {
		try {
			this.ftpFileManager.chargeFromFileAccess();
			this.ftpCommandExecutor.startRunnerThread();
		} catch (RunnerThreadAlreadyStartedException e) {
			ErrorManager.writeError(e);
		} catch (IOException e) {
			ErrorManager.writeError(e);
		}
	}

	@Override
	protected void inStop() {
		this.ftpDataTransfertManager.stopAll();
		try {
			this.ftpFileManager.saveFileAccess();
			this.ftpCommandExecutor.stopAllRunnerThread();
		} catch (RunnerThreadAlreadyStoppedException e) {
			ErrorManager.writeError(e);
		} catch (IOException e) {
			ErrorManager.writeError(e);
		}
	}

	public void commandExecutorExecuteFor(FTPClientConnection ftpClientConnection, Runnable command) {
		this.ftpCommandExecutor.executeCommandFor(ftpClientConnection, command);
	}

	// Getters and Setters.

	public MainWindow getMainFrame() {
		return this.mainFrame;
	}

	public int getConnectionPort() {
		return this.CONNECTION_PORT;
	}

	public int getBeginAuthorizedDataPort() {
		return this.BEGIN_AUTHORIZED_DATA_TRANSFERT_PORT;
	}

	public int getEndAuthorizedDataPort() {
		return this.END_AUTHORIZED_DATA_TRANSFERT_PORT;
	}

	public FTPClientCreator getFTPClientCreator() {
		return this.ftpClientCreator;
	}

	public FTPFileManager getFTPFileManager() {
		return this.ftpFileManager;
	}

	public FTPDataTransfertManager getFTPDataTransfertManager() {
		return this.ftpDataTransfertManager;
	}

	public FTPCommandExecutor getFTPCommandExecutor() {
		return this.ftpCommandExecutor;
	}

	public FTPActionReminder getFTPActionReminder() {
		return this.ftpActionReminder;
	}

}
