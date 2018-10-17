package server.ftp.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Vector;

import server.ftp.exception.UnRecognizedModeException;
import server.ftp.exception.UnRecognizedStructureException;
import server.ftp.listener.CWDListener;
import server.ftp.listener.PseudoListener;
import server.util.ErrorManager;

public class FTPClient implements FTPClientConnection {

	// Variables.

	private String pseudo;

	private String mode = "S";

	private String structure = "F";

	private Socket socket;

	private Vector<PseudoListener> listPseudoListener = new Vector<>();
	private Vector<CWDListener> listCWDListener = new Vector<>();

	private BufferedReader reader;
	private BufferedWriter writer;

	private Path currentWorkingDirectory = null;

	private Path pathWichWillBeRenamed = null;

	private boolean isKill = false;

	// Constructors.

	public FTPClient(Socket socket) {
		this.socket = socket;

		try {
			this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
		} catch (IOException e) {
			ErrorManager.writeError(e);
		}
	}

	// Methods.

	@Override
	public String toString() {
		return "FTPConnection port : " + this.socket.getPort() + " CWD : " + this.currentWorkingDirectory;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj instanceof FTPClient) {
			FTPClient other = (FTPClient) obj;

			return other.getSocket().equals(this.getSocket());

		} else if (obj instanceof Socket) {
			return obj.equals(this.getSocket());
		} else {
			return false;
		}
	}

	@Override
	public int read() throws IOException {
		return this.reader.read();
	}

	@Override
	public int read(char[] cbuf) throws IOException {
		return this.reader.read(cbuf);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return this.reader.read(cbuf, off, len);
	}

	@Override
	public String readLine() throws IOException {
		return this.reader.readLine();
	}

	@Override
	public long skip(long n) throws IOException {
		return this.reader.skip(n);
	}

	@Override
	public boolean ready() throws IOException {
		return this.reader.ready();
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		this.writer.write(cbuf);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		this.writer.write(cbuf, off, len);
	}

	@Override
	public void write(int c) throws IOException {
		this.writer.write(c);
	}

	@Override
	public void write(String string) throws IOException {
		this.writer.write(string);
	}

	@Override
	public void write(String s, int off, int len) throws IOException {
		this.writer.write(s, off, len);
	}

	@Override
	public void newLine() throws IOException {
		this.writer.newLine();
	}

	@Override
	public void flush() throws IOException {
		this.writer.flush();
	}

	@Override
	public String getHostAddress() {
		return this.socket.getInetAddress().getHostAddress();
	}

	@Override
	public void close() throws IOException {
		this.socket.close();

		System.err.println("FTPClientConnection " + this + " close.");
	}

	@Override
	public boolean isClosed() {
		return this.socket.isClosed();
	}

	@Override
	public String getPseudo() {
		return this.pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
		this.pseudoChange(this.pseudo);
	}

	@Override
	public String getMode() {
		return this.mode;
	}

	@Override
	public void setMode(String mode) throws UnRecognizedModeException {
		if (mode.equals("S") || mode.equals("B") || mode.equals("C"))
			this.mode = mode;
		else
			throw new UnRecognizedModeException();
	}

	@Override
	public String getStructure() {
		return this.structure;
	}

	@Override
	public void setStructure(String structure) throws UnRecognizedStructureException {
		if (structure.equals("F") || structure.equals("R") || structure.equals("P"))
			this.structure = structure;
		else
			throw new UnRecognizedStructureException();
	}

	@Override
	public Path getCurrentWorkingDirectory() {
		return this.currentWorkingDirectory;
	}

	@Override
	public void setCurrentWorkingDirectory(Path path) {
		this.currentWorkingDirectory = path;
		this.cwdChange(this.currentWorkingDirectory.toString());
	}

	@Override
	public int getPort() {
		return this.socket.getPort();
	}

	@Override
	public void addPseudoListener(PseudoListener pseudoListener) {
		this.listPseudoListener.add(pseudoListener);
	}

	@Override
	public void removePseudoListener(PseudoListener pseudoListener) {
		this.listPseudoListener.remove(pseudoListener);
	}

	@Override
	public void pseudoChange(String newPseudo) {
		for (PseudoListener pseudoListener : this.listPseudoListener) {
			pseudoListener.pseudoChange(newPseudo);
		}
	}

	@Override
	public void addCWDListener(CWDListener cwdListener) {
		this.listCWDListener.add(cwdListener);
	}

	@Override
	public void removeCWDListener(CWDListener cwdListener) {
		this.listCWDListener.remove(cwdListener);
	}

	@Override
	public void cwdChange(String newCWD) {
		for (CWDListener cwdListener : this.listCWDListener) {
			cwdListener.cwdChange(newCWD);
		}
	}

	@Override
	public boolean hasPathWhichWillBeRenamed() {
		return this.pathWichWillBeRenamed != null;
	}

	@Override
	public Path getPathWhichWillBeRenamed() {
		return this.pathWichWillBeRenamed;
	}

	@Override
	public void setPathWhichWillBeRenamed(Path pathWichWillBeRenamed) {

		// TODO Verifie si y en a pas un deja ?

		this.pathWichWillBeRenamed = pathWichWillBeRenamed;
	}

	@Override
	public void pathHasBeenRenamed() {
		this.pathWichWillBeRenamed = null;
	}

	@Override
	public Socket getSocket() {
		return this.socket;
	}

	@Override
	public void kill() {
		this.isKill = true;
	}

	@Override
	public boolean isKill() {
		return this.isKill;
	}
}
