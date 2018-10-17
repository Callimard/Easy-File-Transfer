package server.ftp.server;

import java.io.IOException;

import server.ftp.exception.FatalErrorException;
import server.ftp.exception.file_manager.RootUserDirectoryAlreadyExistsException;
import server.ftp.manipulator.ClientManipulator;
import server.ftp.sql.table_row.ClientInformationRow;
import server.ftp.sql.table_row.ClientRow;
import server.util.ErrorManager;
import server.util.exception.ManipulatorException;

public class FTPClientCreator {

	// Constants.

	// Variables.

	private FTPServerController ftpServerController;

	// Constructors.

	public FTPClientCreator(FTPServerController ftpServerController) {
		this.ftpServerController = ftpServerController;
	}

	// Methods.

	public boolean createClient(ClientRow client, ClientInformationRow clientInformation) {

		ClientManipulator clientManipulator = new ClientManipulator(client, clientInformation);

		if (!this.ftpServerController.getFTPFileManager().isClient(client.getPseudo())) {

			try {
				clientManipulator.create();

				this.ftpServerController.getFTPFileManager().createGroup(client.getPseudo());

				this.ftpServerController.getFTPFileManager().refresh();

				this.ftpServerController.getFTPFileManager().createWorkspaceDirectory(client.getPseudo());

				return true;

			} catch (ManipulatorException e) {
				ErrorManager.writeError(e);
				return false;
			} catch (RootUserDirectoryAlreadyExistsException e) {
				ErrorManager.writeError(e);
				return false;
			} catch (IOException e) {
				ErrorManager.writeError(e);
				return false;
			} catch (FatalErrorException e) {
				ErrorManager.writeError(e);
				return false;
			}

		} else {
			return false;
		}
	}

	// Getters and Setters.

	public FTPServerController getFTPServerController() {
		return this.ftpServerController;
	}

}
