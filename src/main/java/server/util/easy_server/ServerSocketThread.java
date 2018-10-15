package server.util.easy_server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

import server.util.ErrorManager;
import server.util.easy_server.exception.server_socket_thread.LaunchingTreatmentFailedException;
import server.util.easy_server.exception.server_socket_thread.ServerSocketThreadAlreadyStartException;
import server.util.easy_server.exception.server_socket_thread.ServerSocketThreadAlreadyStoppedException;
import server.util.easy_server.exception.server_socket_thread.StoppingTreatmentFailedException;

/**
 * <p>
 * This class looks like {@link ServerSocket}, is delegate all methods of it.
 * </p>
 * <p>
 * But in plus, this class encapsulate a thread that you can controll (start and
 * stop). The creation of the the ServerSocket and is closing is manage inside
 * the class, you do not have to worry about it.
 * </p>
 * <p>
 * You can also controll actions which must be done at the start and the stop of
 * the thread whith {@link ServerSocketThread#launchingTreatment()} and
 * {@link ServerSocketThread#stoppingTreatment()} methods.
 * </p>
 * <p>
 * But, the most interesting is that you can controll the action of the thread
 * during is execution with {@link ServerSocketThread#serverAction()} method.
 * Currently in this method you implment the treatment done when a client is
 * accept by the ServerSocket, but you can don't do this and you can also add
 * features!
 * 
 * The code :
 * 
 * while (!stopped) {
 * 
 * 		this.serverAction();
 * 
 * }
 * 
 * </p>
 * 
 * @author Callimard
 *
 */
public abstract class ServerSocketThread {

	// Variables.

	private ServerSocket serverSocket;

	private Thread serverThread;

	private boolean isStopped = true;

	// Constructors.

	public ServerSocketThread(int port) throws IOException {

		this.serverSocket = new ServerSocket(port);
	}

	public ServerSocketThread(int port, int backlog) throws IOException {

		this.serverSocket = new ServerSocket(port, backlog);
	}

	public ServerSocketThread(int port, int backlog, InetAddress bindAddr) throws IOException {

		this.serverSocket = new ServerSocket(port, backlog, bindAddr);
	}

	// Methods.

	/**
	 * <p>
	 * This mehtods is the method that the developper can implements to control what
	 * the server must done when it is launch.
	 * </p>
	 * 
	 * <p>
	 * Normally, it is in this mehtod that you call the
	 * {@link ServerSocketThread#accept()} method.
	 * </p>
	 * 
	 * @throws Exception
	 */
	protected abstract void serverAction() throws Exception;

	/**
	 * <p>
	 * The treatment performed when the method
	 * {@link ServerSocketThread#startServerThread()} is call.
	 * </p>
	 * 
	 * <p>
	 * It is useless to verify the state of the server socket thread, it is done in
	 * the <b>launchServerThread</b> method.
	 * </p>
	 * 
	 * <p>
	 * This method can do nothing.
	 * </p>
	 * 
	 * @throws Exception
	 */
	protected abstract void launchingTreatment() throws Exception;

	/**
	 * <p>
	 * The treatment performed when the method
	 * {@link ServerSocketThread#stopServerThread()} is call.
	 * </p>
	 * 
	 * <p>
	 * It is useless to verify the state of the server socket thread, it is done in
	 * the <b>stopServerThread</b> method.
	 * </p>
	 * 
	 * <p>
	 * This method can do nothing.
	 * </p>
	 * 
	 * @throws Exception
	 */
	protected abstract void stoppingTreatment() throws Exception;

	private void runServerSocketThread() throws Exception {
		while (!this.isStopped) {

			try {
				this.serverAction();
			} catch (Exception e) {
				if (this.isStopped) {
					// We don't care because it's when the server is close and finish.
					continue;
				} else {
					throw e;
				}
			}
		}
	}

	/**
	 * <p>
	 * Launch the server.
	 * </p>
	 * 
	 * @throws ServerSocketThreadAlreadyStartException
	 * @throws LaunchingTreatmentFailedException
	 */
	public final synchronized void startServerThread()
			throws ServerSocketThreadAlreadyStartException, LaunchingTreatmentFailedException {
		if (this.isStopped) {

			try {
				this.launchingTreatment();
			} catch (Exception e) {
				throw new LaunchingTreatmentFailedException(e);
			}

			this.serverThread = new Thread(() -> {
				try {
					this.runServerSocketThread();
				} catch (Exception e) {
					ErrorManager.writeError(e);
				}
			});

			this.isStopped = false;

			this.serverThread.start();

		} else {
			throw new ServerSocketThreadAlreadyStartException();
		}
	}

	/**
	 * <p>
	 * Stop the server.
	 * </p>
	 * 
	 * @throws ServerSocketThreadAlreadyStoppedException
	 * @throws IOException
	 *             - can occurred when we close the server socket;
	 * @throws StoppingTreatmentFailedException
	 */
	public final synchronized void stopServerThread()
			throws ServerSocketThreadAlreadyStoppedException, IOException, StoppingTreatmentFailedException {
		if (!this.isStopped) {
			// The order of instruction is very important.
			try {
				this.stoppingTreatment();
			} catch (Exception e) {
				throw new StoppingTreatmentFailedException(e);
			}

			this.isStopped = true;
			this.serverSocket.close();
		} else {
			throw new ServerSocketThreadAlreadyStoppedException();
		}
	}

	/**
	 * @see ServerSocket#accept()
	 * @throws IOException
	 */
	public Socket accept() throws IOException {
		return serverSocket.accept();
	}

	/**
	 * @see ServerSocket#bind(SocketAddress, int)
	 * 
	 * @param endpoint
	 * @param backlog
	 * @throws IOException
	 */
	public void bind(SocketAddress endpoint, int backlog) throws IOException {
		serverSocket.bind(endpoint, backlog);
	}

	/**
	 * @see ServerSocket#bind(SocketAddress)
	 * 
	 * @param endpoint
	 * @throws IOException
	 */
	public void bind(SocketAddress endpoint) throws IOException {
		serverSocket.bind(endpoint);
	}

	// Getters and Setters.

	public boolean isStopped() {
		return this.isStopped;
	}

	/**
	 * @see ServerSocket#getChannel()
	 */
	public ServerSocketChannel getChannel() {
		return serverSocket.getChannel();
	}

	/**
	 * @see ServerSocket#getInetAddress()
	 */
	public InetAddress getInetAddress() {
		return serverSocket.getInetAddress();
	}

	/**
	 * @see ServerSocket#getLocalPort()
	 */
	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	/**
	 * @see ServerSocket#getLocalSocketAddress()
	 */
	public SocketAddress getLocalSocketAddress() {
		return serverSocket.getLocalSocketAddress();
	}

	/**
	 * @see ServerSocket#getReceiveBufferSize()
	 * 
	 * @throws SocketException
	 */
	public int getReceiveBufferSize() throws SocketException {
		return serverSocket.getReceiveBufferSize();
	}

	/**
	 * @see ServerSocket#getReuseAddress()
	 * 
	 * @throws SocketException
	 */
	public boolean getReuseAddress() throws SocketException {
		return serverSocket.getReuseAddress();
	}

	/**
	 * @see ServerSocket#getSoTimeout()
	 * 
	 * @throws IOException
	 */
	public int getSoTimeout() throws IOException {
		return serverSocket.getSoTimeout();
	}

	/**
	 * @see ServerSocket#isBound()
	 */
	public boolean isBound() {
		return serverSocket.isBound();
	}

	/**
	 * @see ServerSocket#isClosed()
	 */
	public boolean isClosed() {
		return serverSocket.isClosed();
	}

	/**
	 * @see ServerSocket#setPerformancePreferences(int, int, int)
	 * 
	 * @param connectionTime
	 * @param latency
	 * @param bandwidth
	 */
	public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
		serverSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
	}

	/**
	 * @see ServerSocket#setReceiveBufferSize(int)
	 * 
	 * @param size
	 * @throws SocketException
	 */
	public void setReceiveBufferSize(int size) throws SocketException {
		serverSocket.setReceiveBufferSize(size);
	}

	/**
	 * @see ServerSocket#setReuseAddress(boolean)
	 * 
	 * @param on
	 * @throws SocketException
	 */
	public void setReuseAddress(boolean on) throws SocketException {
		serverSocket.setReuseAddress(on);
	}

	/**
	 * @see ServerSocket#setSoTimeout(int)
	 * 
	 * @param timeout
	 * @throws SocketException
	 */
	public void setSoTimeout(int timeout) throws SocketException {
		serverSocket.setSoTimeout(timeout);
	}

}
