package server.ftp.server;

import java.net.Socket;
import java.nio.file.Path;

import server.ftp.exception.UnRecognizedModeException;
import server.ftp.exception.UnRecognizedStructureException;
import server.ftp.listener.CWDListener;
import server.ftp.listener.PseudoListener;
import server.util.easy_server.ClientConnection;

public interface FTPClientConnection extends ClientConnection {

	public String getPseudo();

	public void setPseudo(String pseudo);

	public String getMode();

	public String getStructure();

	public void setStructure(String structure) throws UnRecognizedStructureException;

	public void setMode(String mode) throws UnRecognizedModeException;

	public Path getCurrentWorkingDirectory();

	public void setCurrentWorkingDirectory(Path path);

	public void addPseudoListener(PseudoListener pseudoListener);

	public void removePseudoListener(PseudoListener pseudoListener);

	public void pseudoChange(String newPseudo);

	public void addCWDListener(CWDListener cwdListener);

	public void removeCWDListener(CWDListener cwdListener);

	public void cwdChange(String newCWD);

	public boolean hasPathWhichWillBeRenamed();

	public Path getPathWhichWillBeRenamed();

	public void setPathWhichWillBeRenamed(Path pathWichWillBeRenamed);

	public void pathHasBeenRenamed();

	public Socket getSocket();

	public void kill();

	public boolean isKill();

}
