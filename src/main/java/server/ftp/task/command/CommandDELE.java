package server.ftp.task.command;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.exception.file_manager.NoAccessToThisFileException;
import server.ftp.exception.file_manager.PathIsNotFileException;
import server.ftp.exception.file_manager.RemotePahtWrongFormatException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandDELE extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "DELE";

	// Constructors.

	public CommandDELE(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}
	
	// Methods.

	@Override
	public void run() {
		
		// TODO Verifier les droits.

		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		report.offerAction("Current working directory = " + this.getClientConnection().getCurrentWorkingDirectory());

		FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

		FTPFileManager ftpFileManager = this.getFTPServerController().getFTPFileManager();

		try {

			if (ftpAuthenticationManager.isAlreadyAuthenticated(this.getClientConnection())) {

				if (args != null && args.length >= 1) {

					String stringPath = "";

					for (int i = 0; i < args.length; i++) {
						if (i < args.length - 1) {
							stringPath += args[i] + " ";
						} else {
							stringPath += args[i];
						}
					}

					Path argPath = Paths.get(stringPath);

					Path workingPath = null;

					try {
						// Absolute Path
						if (argPath.getRoot() != null) {

							workingPath = ftpFileManager.convertToLocalPath(argPath);

						} else {

							workingPath = Paths.get(this.getClientConnection().getCurrentWorkingDirectory().toString(),
									stringPath);

						}
						ftpFileManager.deleteFile(workingPath, this.getClientConnection());

						this.getClientConnection().write("250 file has been deleted\n");
					} catch (IOException e) {

						report.offerAction("Error = " + e);
						report.setFailed();

						this.getClientConnection().write("550 file probleme\n");
					} catch (PathIsNotFileException e) {

						report.offerAction("Le path n'est pas un fichier. Error = " + e);
						report.setFailed();

						this.getClientConnection().write("550 path is not a file\n");
					} catch (LocalPathIsNotServerPathException e) {
						report.offerAction("Erreur de path qui n'est pas dans la bonne zone. ArgPath = " + workingPath);
						report.setFailed();

						this.getClientConnection().write("550 access denied\n");

						ErrorManager.writeError(e);
					} catch (NoAccessToThisFileException e) {
						report.offerAction("Acces refuse.");
						report.setFailed();

						this.getClientConnection().write("550 access denied\n");
					} catch (RemotePahtWrongFormatException e) {

						report.offerAction(
								"Probl�me de format lors de la convertion en path local. ArgPath = " + argPath);
						report.offerAction("Error : " + e);
						report.setFailed();

						this.getClientConnection().write("550 parent file not exists.");
					}

				} else {
					report.offerAction("Erreur d'argument. Il faut un seul argument.");
					report.setFailed();

					this.getClientConnection().write("501 argument error\n");
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
