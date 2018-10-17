
package server.ftp.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import server.ftp.exception.data_transfert_manager.DataConnectionAlreadyOpenForClientConnectionException;
import server.ftp.exception.data_transfert_manager.EndPortLessThanBeginPortException;
import server.ftp.exception.data_transfert_manager.FailToOpenDataConnectionException;
import server.ftp.exception.data_transfert_manager.NoDataPortFreeException;
import server.util.ErrorManager;
import server.util.NetworkTools;

public class FTPDataTransfertManager {

	// Constants.

	private final Object LOCK = new Object();

	// Variables.

	@SuppressWarnings("unused")
	private FTPServerController ftpServerController;

	private ConcurrentHashMap<FTPClientConnection, Vector<DataConnection>> hashDataConnection = new ConcurrentHashMap<>();

	private Vector<DataConnectionWaiting> listThread = new Vector<>();

	private Vector<Integer> listUnusedPort;
	private Vector<Integer> listUsedPort;

	private int beginPort;
	private int endPort;

	// Constructors.

	public FTPDataTransfertManager(int beginPort, int endPort, FTPServerController ftpServerController)
			throws EndPortLessThanBeginPortException {

		this.ftpServerController = ftpServerController;

		if (endPort <= beginPort)
			throw new EndPortLessThanBeginPortException();

		this.beginPort = beginPort;
		this.endPort = endPort;

		this.listUnusedPort = new Vector<>();

		synchronized (this.LOCK) {

			for (int i = beginPort; i <= this.endPort; i++) {
				this.listUnusedPort.add(i);
			}

		}

		this.listUsedPort = new Vector<>();

	}

	// Methods.

	/**
	 * 
	 * @return the number port attributed if there is a port enable.
	 * @throws NoDataPortFreeException
	 */
	private int attributePort() throws NoDataPortFreeException {
		synchronized (this.LOCK) {
			if (!this.listUnusedPort.isEmpty()) {
				int port = this.listUnusedPort.remove(this.listUnusedPort.size() - 1);

				this.listUsedPort.add(port);

				return port;

			} else {
				throw new NoDataPortFreeException();
			}
		}
	}

	private void freeDataConnectionPort(int port) {
		synchronized (this.LOCK) {

			boolean removed = this.listUsedPort.remove(new Integer(port));

			if (removed) {
				this.listUnusedPort.add(port);

				System.err.println("Port free = " + port);
			}
		}
	}

	/**
	 * <p>
	 * Search a data connection associated to the ftp client connection which is
	 * unused.
	 * </p>
	 * 
	 * <p>
	 * Return a data connection which is free, but the data connection which is
	 * return becomes not free and no one can get him after.
	 * </p>
	 * 
	 * <p>
	 * This method can return because the client is not connected but he will do it
	 * after. To wait the client connection, used
	 * {@link FTPDataTransfertManager#getWaitFreeDataConnectionFor(FTPClientConnection)}
	 * </p>
	 * 
	 * @see FTPDataTransfertManager#getWaitFreeDataConnectionFor(FTPClientConnection)
	 * 
	 * @param ftpClientConnection
	 * @return a data which is not used for the moment. If there is no Data
	 *         Connection free, return null.
	 */
	private DataConnection getFreeDataConnectionFor(FTPClientConnection ftpClientConnection) {
		Vector<DataConnection> listDataConnection = this.hashDataConnection.get(ftpClientConnection);

		if (listDataConnection != null) {

			for (DataConnection dataConnection : listDataConnection) {
				boolean success = dataConnection.getToUsed();

				if (success) {
					return dataConnection;
				}
			}

		}

		return null;
	}

	/**
	 * 
	 * <p>
	 * Verify if the ftp client connection has data connection.
	 * </p>
	 * <p>
	 * This method is not synchronized with the asking of data connection, so if a
	 * data connection is create during this method, maybye this method will not see
	 * it.
	 * </p>
	 * 
	 * @param ftpClientConnection
	 * @return true if the ftp client connection has one or more data connection.
	 */
	public boolean ftpClientConnectionHasDataConnection(FTPClientConnection ftpClientConnection) {

		Vector<DataConnection> listDataConnection = this.hashDataConnection.get(ftpClientConnection);

		boolean presence = false;

		for (DataConnectionWaiting dataConnectionWaiting : this.listThread) {
			if (dataConnectionWaiting.getFTPClientConnection() == ftpClientConnection) {
				presence = true;
			}
		}

		if (!presence) {
			if (listDataConnection != null) {
				if (!listDataConnection.isEmpty()) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * 
	 * @param ftpClientConnection
	 * 
	 * @return the number port use for the data connection.
	 * 
	 * @throws DataConnectionAlreadyOpenForClientConnectionException
	 * @throws NoDataPortFreeException
	 * @throws FailToOpenDataConnectionException
	 */
	public int openDataConnectionFor(FTPClientConnection ftpClientConnection) throws NoDataPortFreeException {

		int port = this.attributePort();

		DataConnectionWaiting dataConnectionWaiting = new DataConnectionWaiting(ftpClientConnection, port);

		// The order is very important.
		// We need always add befor start the thread
		// Because the thread will remove him after is execution.

		this.listThread.add(dataConnectionWaiting);

		dataConnectionWaiting.start();

		return port;
	}

	/**
	 * <p>
	 * Search a data connection associated to the ftp client connection which is
	 * unused.
	 * </p>
	 * 
	 * <p>
	 * Return a data connection which is free, but the data connection which is
	 * return becomes not free and no one can get him after.
	 * </p>
	 * 
	 * 
	 * @param ftpClientConnection
	 * @returna data which is not used for the moment. Can't return null and wait
	 *          until a notify by the creation of a free data connection.
	 */
	public DataConnection getWaitFreeDataConnectionFor(FTPClientConnection ftpClientConnection) {
		DataConnection dataConnection = null;
		synchronized (ftpClientConnection) {
			while ((dataConnection = this.getFreeDataConnectionFor(ftpClientConnection)) == null) {
				try {
					ftpClientConnection.wait();
				} catch (InterruptedException e) {
					ErrorManager.writeError(e);
				}
			}

		}

		return dataConnection;

	}

	public void closeDataConnection(DataConnection dataConnection) {
		if (dataConnection != null) {
			FTPClientConnection ftpClientConnection = dataConnection.ftpClientConnection;

			Vector<DataConnection> listDataConnection = this.hashDataConnection.get(ftpClientConnection);

			synchronized (ftpClientConnection) {

				if (listDataConnection != null) {

					boolean success = listDataConnection.remove(dataConnection);

					if (success) {
						this.freeDataConnectionPort(dataConnection.getPort());

						if (!dataConnection.isClosed()) {
							try {
								dataConnection.close();
							} catch (IOException e) {
								ErrorManager.writeError(e);
							}
						}
					}
				}
			}
		}

	}

	public void stopAllThreadWaitingDataConnection() {
		this.listThread.forEach((dataConnectionWaiting) -> {
			dataConnectionWaiting.interrupt();
		});

		this.listThread.clear();
	}

	public void closeAllDataConnection() {
		Set<Entry<FTPClientConnection, Vector<DataConnection>>> set = this.hashDataConnection.entrySet();

		for (Entry<FTPClientConnection, Vector<DataConnection>> entry : set) {
			Vector<DataConnection> listDataConnection = entry.getValue();

			listDataConnection.forEach((dataConnection) -> {

				this.freeDataConnectionPort(dataConnection.clientSocket.getLocalPort());

				if (!dataConnection.isClosed()) {
					try {
						dataConnection.close();
					} catch (IOException e) {
						ErrorManager.writeError(e);
					}
				}

			});

			listDataConnection.clear();
		}
	}

	public void stopAll() {
		this.stopAllThreadWaitingDataConnection();
		this.closeAllDataConnection();
	}

	// Getters and Setters.

	public int getBeginPort() {
		return this.beginPort;
	}

	public int getEndPort() {
		return this.endPort;
	}

	// Private class.

	private class DataConnectionWaiting extends Thread {

		// Constants.

		private final int DEFAULT_SERVER_SOCKET_BACKLOG = 10;

		// Variables.

		private FTPClientConnection ftpClientConnection;

		private int port;

		private ServerSocket serverSocket;

		// Constructors.

		public DataConnectionWaiting(FTPClientConnection ftpClientConnection, int port) {
			this.ftpClientConnection = ftpClientConnection;
			this.port = port;
		}

		// Methods.

		@Override
		public void run() {
			Socket s = null;

			this.serverSocket = null;
			try {

				this.serverSocket = new ServerSocket(this.port, DEFAULT_SERVER_SOCKET_BACKLOG,
						InetAddress.getByName(NetworkTools.getBoxIpAddress()));

				do {
					s = this.serverSocket.accept();

//					System.out.println("Socket data = " + s);

				} while (!s.getInetAddress().getHostAddress().equals(this.ftpClientConnection.getHostAddress()));

				DataConnection dataConnection = FTPDataTransfertManager.this.new DataConnection(s,
						this.ftpClientConnection);

				synchronized (this.ftpClientConnection) {

					Vector<DataConnection> listDataConnection = FTPDataTransfertManager.this.hashDataConnection
							.get(this.ftpClientConnection);

					if (listDataConnection == null) {
						listDataConnection = new Vector<>();
						FTPDataTransfertManager.this.hashDataConnection.put(this.ftpClientConnection,
								listDataConnection);
					}

					listDataConnection.add(dataConnection);

					// Order very important.

					FTPDataTransfertManager.this.listThread.remove(Thread.currentThread());

					this.ftpClientConnection.notifyAll();
				}

			} catch (IOException e) {
				// We do nothing because the server socket has been close manually.
			} finally {

				if (this.serverSocket != null)
					try {
						this.serverSocket.close();
					} catch (IOException e) {
						ErrorManager.writeError(e);
					}
			}
		}

		@Override
		public void interrupt() {
			if (!this.serverSocket.isClosed())
				try {
					this.serverSocket.close();
				} catch (IOException e) {
					ErrorManager.writeError(e);
				}

			super.interrupt();
		}

		public FTPClientConnection getFTPClientConnection() {
			return this.ftpClientConnection;
		}
	}

	// Public class.

	public class DataConnection {

		// Variables.

		private FTPClientConnection ftpClientConnection;

		private Socket clientSocket;

		private BufferedReader reader;
		private BufferedWriter writer;

		private boolean isUsed = false;

		// Constructors.

		private DataConnection(Socket clientSocket, FTPClientConnection ftpClientConnection) throws IOException {

			this.ftpClientConnection = ftpClientConnection;

			this.clientSocket = clientSocket;

			this.reader = new BufferedReader(
					new InputStreamReader(this.clientSocket.getInputStream(), StandardCharsets.ISO_8859_1));
			this.writer = new BufferedWriter(
					new OutputStreamWriter(this.clientSocket.getOutputStream(), StandardCharsets.ISO_8859_1));
		}

		// Methods.

		/**
		 * 
		 * @return true if you get the data connection. If you get the data connection,
		 *         it becomes used and it can re become not used.
		 */
		public boolean getToUsed() {
			if (!this.isUsed) {
				this.isUsed = true;
				return true;
			} else {
				return false;
			}
		}

		public void writeData(int b) throws IOException {
			this.writer.write(b);
		}

		public void writeData(char[] cbuf) throws IOException {
			this.writer.write(cbuf);
		}

		public void writeData(String data) throws IOException {
			this.writer.write(data);
		}

		public void writeDataLine(String data) throws IOException {
			this.writer.write(data + "\n");
		}

		public void flush() throws IOException {
			this.writer.flush();
		}

		/**
		 * Read a line of the stream, the method block the thread.
		 * 
		 * @return the line readed, null if the end of the stream is reach.
		 * @throws IOException
		 */
		public String readDataLineBlock() throws IOException {
			return this.reader.readLine();
		}

		/**
		 * 
		 * Read a line of the stream, the method does not block the thread.
		 * 
		 * @return the line readed, null if the end of the stream is reach or if the
		 *         reader is not ready.
		 * @throws IOException
		 */
		public String readDataLineUnBlock() throws IOException {
			if (this.reader.ready()) {
				return this.reader.readLine();
			} else {
				return null;
			}
		}

		public int readCharBufBlock(char[] cbuf) throws IOException {
			return this.reader.read(cbuf);
		}

		public int readCharBufBlock(char[] cbuf, int b, int len) throws IOException {
			return this.reader.read(cbuf, b, len);
		}

		public boolean isClosed() {
			return this.clientSocket.isClosed();
		}

		public void close() throws IOException {
			this.clientSocket.close();
		}

		// Getters and Setters.

		public FTPClientConnection getFTPClientConnection() {
			return this.ftpClientConnection;
		}

		public boolean isUsed() {
			return this.isUsed;
		}

		public int getPort() {
			return this.clientSocket.getLocalPort();
		}
	}

}
