package server.ftp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import server.util.easy_server.ServerSocketThread;

public class FTPServerSocketThread extends ServerSocketThread {

	// Variables.

	private FTPServerController serverController;

	// Constructors.

	public FTPServerSocketThread(int port, FTPServerController serverController) throws IOException {
		super(port);

		this.serverController = serverController;
	}

	public FTPServerSocketThread(int port, int backlog, FTPServerController serverController) throws IOException {
		super(port, backlog);

		this.serverController = serverController;
	}

	public FTPServerSocketThread(int port, int backlog, InetAddress bindAddr, FTPServerController serverController)
			throws IOException {
		super(port, backlog, bindAddr);

		this.serverController = serverController;
	}

	@Override
	protected void serverAction() throws Exception {
		Socket socket = this.accept();

		System.out.println("Entre du client " + socket + " c'est connecte.");

		if (socket != null) {

			socket.getOutputStream().write(new byte[] { '2', '2', '0', ' ', 'b', 'o', 'n', 'j', 'o', 'u', 'r', '\n' });
			socket.getOutputStream().flush();

			this.serverController.add(socket);
		}
	}

	@Override
	protected void launchingTreatment() throws Exception {
		// NOTHING.
	}

	@Override
	protected void stoppingTreatment() throws Exception {
		// NOTHING.
	}

}
