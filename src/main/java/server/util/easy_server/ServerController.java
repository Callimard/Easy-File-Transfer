package server.util.easy_server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.naming.OperationNotSupportedException;

import server.util.easy_server.exception.client_manager.ClientManagerAlreadyManagingClientException;
import server.util.easy_server.exception.client_manager.ClientManagerNotManagingClientException;
import server.util.easy_server.exception.client_manager.StartingClientManagingFailedException;
import server.util.easy_server.exception.client_manager.StoppingClientManagingFailedException;
import server.util.easy_server.exception.server_controller.ServerControllerAlreadyStartedException;
import server.util.easy_server.exception.server_controller.ServerControllerAlreadyStoppedException;
import server.util.easy_server.exception.server_controller.ServerControllerFailedToStartException;
import server.util.easy_server.exception.server_controller.ServerControllerFailedToStopException;
import server.util.easy_server.exception.server_socket_thread.LaunchingTreatmentFailedException;
import server.util.easy_server.exception.server_socket_thread.ServerSocketThreadAlreadyStartException;
import server.util.easy_server.exception.server_socket_thread.ServerSocketThreadAlreadyStoppedException;
import server.util.easy_server.exception.server_socket_thread.StoppingTreatmentFailedException;

/**
 * <p>
 * This class represent a server which works with three elements:
 * <ul>
 * <li>The {@link ServerSocketThread} which is the object which manage the
 * thread which accept connection.</li>
 * <li>The {@link ClientManager} which is the object which manage all client
 * which connect to the server.</li>
 * <li>The {@link ExecutorService} which is the object which execute all
 * actions, features which the server will need to execute.</li>
 * </ul>
 * </p>
 * <p>
 * You can add features to the server and if these features need to be started
 * or stopped, you can do this with {@link ServerController#inStart()} method
 * and {@link ServerController#inStop()} method. These methods are respectivly
 * use in the beginning of {@link ServerController#start()} method,
 * {@link ServerController#stopAll()} method and
 * {@link ServerController#stopAllNow()}
 * </p>
 * 
 * 
 * @author Callimard
 *
 * @param <T>
 */
public abstract class ServerController<T extends ClientConnection> {

	// Constants.

	private static final int DEFAULT_FIXED_THREAD_NUMBER = 5;

	// Variables.

	private ServerSocketThread serverSocketThread;

	private ClientManager<T> clientManager;

	/**
	 * <p>
	 * The object which whill execute any task which teh server must do.
	 * </p>
	 * <p>
	 * But some concurrent problem can be create if all task are execute in this
	 * executor. So in the implementation on your server, maybye you will need to
	 * create an other interface which will execute task in a different way.
	 * </p>
	 */
	private ExecutorService executor;

	private boolean isStopped = true;

	// Constructors.

	/**
	 * <p>
	 * Create a server controller without ServerSocketThread and ClientManager. They
	 * must be added after.
	 * </p>
	 * <p>
	 * Use {@link ServerController#setServerSocketThread(ServerSocketThread)} and
	 * {@link ServerController#setClientManager(ClientManager)}
	 * </p>
	 * <p>
	 * The command executor is by default a
	 * {@link Executors#newFixedThreadPool(int)} with pool size =
	 * {@link ServerController#DEFAULT_FIXED_THREAD_NUMBER} </p
	 */
	protected ServerController() {
		this(null, null, Executors.newFixedThreadPool(DEFAULT_FIXED_THREAD_NUMBER));
	}

	/**
	 * <p>
	 * Create a server controller with ServerSocketThread and ClientManager.
	 * </p>
	 * <p>
	 * The command executor is by default a
	 * {@link Executors#newFixedThreadPool(int)} with pool size =
	 * {@link ServerController#DEFAULT_FIXED_THREAD_NUMBER}
	 * </p>
	 * 
	 * @param serverSocketThread
	 * @param clientManager
	 */
	protected ServerController(ServerSocketThread serverSocketThread, ClientManager<T> clientManager) {
		this(serverSocketThread, clientManager, Executors.newFixedThreadPool(DEFAULT_FIXED_THREAD_NUMBER));
	}

	/**
	 * <p>
	 * Create a server controller with ServerSocketThread and ClientManager and
	 * ExecutorService.
	 * </p>
	 * 
	 * @param serverSocketThread
	 * @param clientManager
	 * @param executor
	 */
	protected ServerController(ServerSocketThread serverSocketThread, ClientManager<T> clientManager,
			ExecutorService executor) {
		this.serverSocketThread = serverSocketThread;

		this.clientManager = clientManager;

		this.executor = executor;
	}

	// Methods.

	public void add(Socket socket) {
		clientManager.add(socket);
	}

	public void addAll(Collection<Socket> collection) {
		clientManager.addAll(collection);
	}

	public void remove(Socket socket) throws OperationNotSupportedException {
		clientManager.remove(socket);
	}

	public void removeAll(Collection<Socket> collection) throws OperationNotSupportedException {
		clientManager.removeAll(collection);
	}

	/**
	 * <p>
	 * The methods add features when we start the server.
	 * </p>
	 * <p>
	 * If there is no feature to add, just do nothin but <b>DON'T THROW
	 * {@link UnsupportedOperationException} or other Exception</b>
	 * </p>
	 */
	protected abstract void inStart();

	/**
	 * <p>
	 * Start all services of the ServerController.
	 * </p>
	 * <p>
	 * The executor is started.
	 * </p>
	 * <p>
	 * The serverSocketThread is started.
	 * </p>
	 * <p>
	 * The client manager is started.
	 * </p>
	 * 
	 * @throws ServerControllerAlreadyStartedException
	 * @throws ServerControllerFailedToStartException
	 */
	public synchronized void start()
			throws ServerControllerAlreadyStartedException, ServerControllerFailedToStartException {
		if (this.isStopped) {

			this.inStart();

			try {
				if (this.serverSocketThread != null)
					this.serverSocketThread.startServerThread();
			} catch (ServerSocketThreadAlreadyStartException | LaunchingTreatmentFailedException e) {
				throw new ServerControllerFailedToStartException(e);
			}

			try {
				if (this.clientManager != null)
					this.clientManager.startClientManaging();
			} catch (ClientManagerAlreadyManagingClientException | StartingClientManagingFailedException e) {
				throw new ServerControllerFailedToStartException(e);
			}

			this.isStopped = false;
		} else {
			throw new ServerControllerAlreadyStartedException();
		}
	}

	/**
	 * <p>
	 * The methods add features when we stop the server.
	 * </p>
	 * <p>
	 * If there is no feature to add, just do nothin but <b>DON'T THROW
	 * {@link UnsupportedOperationException} or other Exception</b>
	 * </p>
	 * 
	 * @see ServerController#stopAll()
	 */
	protected abstract void inStop();

	/**
	 * <p>
	 * Stop all services of the server.
	 * </p>
	 * <p>
	 * The executor is stopped.
	 * </p>
	 * <p>
	 * The serverSocketThread is stopped.
	 * </p>
	 * <p>
	 * The client manager is stopped.
	 * </p>
	 * 
	 * @throws ServerControllerAlreadyStoppedException
	 * @throws ServerControllerFailedToStopException
	 */
	public synchronized void stopAll()
			throws ServerControllerAlreadyStoppedException, ServerControllerFailedToStopException {
		if (!this.isStopped) {

			this.inStop();

			if (this.executor != null)
				this.executor.shutdown();

			try {
				if (this.serverSocketThread != null)
					this.serverSocketThread.stopServerThread();
			} catch (ServerSocketThreadAlreadyStoppedException | StoppingTreatmentFailedException | IOException e) {
				throw new ServerControllerFailedToStopException(e);
			}

			try {
				if (this.clientManager != null)
					this.clientManager.stopClientManaging();
			} catch (ClientManagerNotManagingClientException | StoppingClientManagingFailedException e) {
				throw new ServerControllerFailedToStopException(e);
			}

			this.isStopped = true;
		} else {
			throw new ServerControllerAlreadyStoppedException();
		}
	}

	public synchronized List<Runnable> stopAllNow()
			throws ServerControllerAlreadyStoppedException, ServerControllerFailedToStopException {
		if (!this.isStopped) {

			this.inStop();

			List<Runnable> listRunnable = null;
			if (this.executor != null)
				listRunnable = this.executor.shutdownNow();

			try {
				if (this.serverSocketThread != null)
					this.serverSocketThread.stopServerThread();
			} catch (ServerSocketThreadAlreadyStoppedException | StoppingTreatmentFailedException | IOException e) {
				throw new ServerControllerFailedToStopException(e);
			}

			if (this.clientManager != null)
				try {
					this.clientManager.stopClientManaging();
				} catch (ClientManagerNotManagingClientException | StoppingClientManagingFailedException e) {
					throw new ServerControllerFailedToStopException(e);
				}

			return listRunnable;
		} else {
			throw new ServerControllerAlreadyStoppedException();
		}
	}

	public boolean awaitExecutorTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return this.executor.awaitTermination(timeout, unit);
	}

	public boolean executorIsShutdown() {
		return this.executor.isShutdown();
	}

	public boolean executorIsTerminated() {
		return this.executor.isTerminated();
	}

	public boolean executorAwaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return this.executor.awaitTermination(timeout, unit);
	}

	public void executorExecute(Runnable command) {
		this.executor.execute(command);
	}

	public List<Future<T>> executorInvokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return this.executor.invokeAll(tasks, timeout, unit);
	}

	public List<Future<T>> executorInvokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return this.executor.invokeAll(tasks);
	}

	public T executorInvokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return this.executor.invokeAny(tasks, timeout, unit);
	}

	public T executorInvokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return this.executor.invokeAny(tasks);
	}

	public Future<T> executorSubmit(Callable<T> command) {
		return this.executor.submit(command);
	}

	public Future<T> executorSubmit(Runnable command, T result) {
		return this.executor.submit(command, result);
	}

	public Future<?> executorSubmit(Runnable command) {
		return this.executor.submit(command);
	}

	public boolean serverThreadIsStopped() {
		return serverSocketThread.isStopped();
	}

	public void bind(SocketAddress endpoint, int backlog) throws IOException {
		serverSocketThread.bind(endpoint, backlog);
	}

	public void bind(SocketAddress endpoint) throws IOException {
		serverSocketThread.bind(endpoint);
	}

	public ServerSocketChannel getChannel() {
		return serverSocketThread.getChannel();
	}

	public InetAddress getInetAddress() {
		return serverSocketThread.getInetAddress();
	}

	public int getLocalPort() {
		return serverSocketThread.getLocalPort();
	}

	public SocketAddress getLocalSocketAddress() {
		return serverSocketThread.getLocalSocketAddress();
	}

	public int getReceiveBufferSize() throws SocketException {
		return serverSocketThread.getReceiveBufferSize();
	}

	public boolean getReuseAddress() throws SocketException {
		return serverSocketThread.getReuseAddress();
	}

	public int getSoTimeout() throws IOException {
		return serverSocketThread.getSoTimeout();
	}

	public boolean isBound() {
		return serverSocketThread.isBound();
	}

	public boolean isClosed() {
		return serverSocketThread.isClosed();
	}

	public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
		serverSocketThread.setPerformancePreferences(connectionTime, latency, bandwidth);
	}

	public void setReceiveBufferSize(int size) throws SocketException {
		serverSocketThread.setReceiveBufferSize(size);
	}

	public void setReuseAddress(boolean on) throws SocketException {
		serverSocketThread.setReuseAddress(on);
	}

	public void setSoTimeout(int timeout) throws SocketException {
		serverSocketThread.setSoTimeout(timeout);
	}

	public boolean isStopped() {
		return this.isStopped;
	}

	public ServerSocketThread getServerSocketThread() {
		return serverSocketThread;
	}

	/**
	 * Can be set only if the server controller isStopped.
	 * 
	 * @param serverSocketThread
	 */
	public void setServerSocketThread(ServerSocketThread serverSocketThread) {
		if (!this.isStopped)
			this.serverSocketThread = serverSocketThread;
	}

	public ClientManager<T> getClientManager() {
		return clientManager;
	}

	/**
	 * Can be set only if the server controller isStopped.
	 * 
	 * @param clientManager
	 */
	public void setClientManager(ClientManager<T> clientManager) {
		if (!this.isStopped)
			this.clientManager = clientManager;
	}

	public ExecutorService getExecutor() {
		return this.executor;
	}

	/**
	 * Can be set only if the server controller isStopped.
	 * 
	 * @param executor
	 */
	public void setExecutor(ExecutorService executor) {
		if (!this.isStopped)
			this.executor = executor;
	}
}
