package server.ftp.server;

import server.util.easy_server.Command;

public abstract class FTPCommand extends Command<FTPClientConnection> {

	// Constants

	// Variables.

	protected FTPServerController ftpServerController;

	protected String[] args;
	
	// Constructors.

	protected FTPCommand(String commandName, FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(commandName, clientConnection);

		this.ftpServerController = ftpServerController;
		
		this.args = args;
	}

	// Methods.

	// Getters and Setters.

	public FTPServerController getFTPServerController() {
		return this.ftpServerController;
	}
	
	public String[] getArgs() {
		return this.args.clone();
	}

}
