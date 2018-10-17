package server.ftp.task.command;

import java.io.IOException;

import server.ftp.debug.Report;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.exception.file_manager.NameForFileNotCorrectException;
import server.ftp.exception.file_manager.NoAccessToThisFileException;
import server.ftp.server.FTPAuthenticationManager;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPCommand;
import server.ftp.server.FTPFileManager;
import server.ftp.server.FTPServerController;
import server.util.Tools;
import server.util.ErrorManager;

public class CommandRNTO extends FTPCommand {

	// Constants.

	public static final String NAME_CODE = "RNTO";

	// Constructors.

	public CommandRNTO(FTPClientConnection clientConnection, FTPServerController ftpServerController, String... args) {
		super(NAME_CODE, clientConnection, ftpServerController, args);
	}

	// Methods.
	
	@Override
	public void run() {
		Report report = new Report("Command : " + NAME_CODE);

		report.offerAction("Args = " + Tools.toStringArray(args));

		report.offerAction("Current working directory = " + this.getClientConnection().getCurrentWorkingDirectory());

		FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();

		FTPFileManager ftpFileManager = this.getFTPServerController().getFTPFileManager();

		// TODO Verifier les droits.

		try {

			if (ftpAuthenticationManager.isAlreadyAuthenticated(this.getClientConnection())) {

				if (args != null && args.length >= 1) {

					if (this.getClientConnection().hasPathWhichWillBeRenamed()) {

						String newFileName = "";

						for (int i = 0; i < args.length; i++) {
							if (i < args.length - 1) {
								newFileName += args[i] + " ";
							} else {
								newFileName += args[i];
							}
						}

						report.offerAction("Nouveau nom de fichier = " + newFileName);

						try {
							try {
								ftpFileManager.renameFile(this.getClientConnection().getPathWhichWillBeRenamed(), newFileName,
										this.getClientConnection());

								this.getClientConnection().pathHasBeenRenamed();

								this.getClientConnection().write("250 rename done\n");

							} catch (LocalPathIsNotServerPathException e) {

								// Normaly never append

								report.offerAction("Mauvaise convertion de NewFileName = "
										+ this.getClientConnection().getPathWhichWillBeRenamed());
								report.offerAction("Error = " + e);
								report.setFailed();

								ErrorManager.writeError(e);

								this.getClientConnection().write("421 big error. Closing connection.\n");

								this.getClientConnection().flush();

								this.getFTPServerController().getClientManager().preparekillClient(this.getClientConnection());

								return;
							} catch (NoAccessToThisFileException e) {

								// Normaly never append

								report.offerAction("Access refuser pour NewFileName = "
										+ this.getClientConnection().getPathWhichWillBeRenamed());
								report.offerAction("Error = " + e);
								report.setFailed();

								ErrorManager.writeError(e);

								this.getClientConnection().write("421 big error\n");

								this.getClientConnection().flush();

								this.getFTPServerController().getClientManager().preparekillClient(this.getClientConnection());

								return;
							}

						} catch (NameForFileNotCorrectException e) {
							report.offerAction("Nouveau nom de fichier pas correct");

							this.getClientConnection().write("553 file name not allowed\n");
						}

					} else {
						report.offerAction("RNFR n'a pas ete fait. Erreur de sequence.");

						this.getClientConnection().write("503 RNFR not be done before\n");
					}

				} else {
					this.getClientConnection().write("501 argument error\n");

					report.offerAction("Erreure d'arguments.");
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
