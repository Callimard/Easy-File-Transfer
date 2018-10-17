package server.ftp.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

import javax.naming.OperationNotSupportedException;

import server.ftp.exception.authentication.FailToDeauthenticateException;
import server.ftp.server.FTPDeauthenticator.TimeUnit;
import server.util.ErrorManager;
import server.util.easy_server.ClientManager;
import server.util.easy_server.exception.command_reader.FailToExtractCommandException;
import server.util.easy_server.exception.runner_thread.RunnerThreadAlreadyStarted;
import server.util.easy_server.exception.runner_thread.RunnerThreadAlreadyStopped;

public class FTPClientManager extends ClientManager<FTPClientConnection> {

	// Constants.

	public static final int DEFAULT_NUMBER_THREAD_EXECUTOR = 5;

	public static final int DEFAULT_NUMBER_SUB_LIST = 2;

	public static final int DEFAULT_DEAUTHENTICATOR_TIMER = 30;

	public static final TimeUnit DEFAULT_DEAUTHENTICATION_TIMER_TIME_UNIT = TimeUnit.SECOND;

	// Variables.

	private FTPServerController ftpServerController;

	/**
	 * The object which verify authentication of client. If a client is
	 * authenticated but don't launch query, he is deauthenticated.s
	 */
	private FTPDeauthenticator deauthenticator;

	/**
	 * The number of client socket sub list.
	 */
	public final int NUMBER_SUB_LIST;

	public final int NUMBER_THREAD_EXECUTOR;

	private ConcurrentHashMap<Socket, SubClientQueue> hashClientSubAueue = new ConcurrentHashMap<>();

	/**
	 * The array which contains all client socket sub list.
	 */
	private final SubClientQueue[] ARRAY_SUB_LIST;

	private final RunnerThread[] ARRAY_RUNNER_THREAD;

	/**
	 * The current index of the {@link ClientManager#ARRAY_SUB_LIST}. This index
	 * show which is the next client socket sub list which will add the next client
	 * socket.
	 */
	private int indexAddSocket;

	/**
	 * Boolean use to know if runner thread have been launch or not.
	 */
	private boolean runnerThreadLaunch = false;

	// Constructors.

	public FTPClientManager(int numberSubList, int numberThreadExecutor, FTPServerController ftpServerController) {
		super(new FTPCommandReader(ftpServerController));

		this.ftpServerController = ftpServerController;

		this.NUMBER_SUB_LIST = numberSubList < DEFAULT_NUMBER_SUB_LIST ? DEFAULT_NUMBER_SUB_LIST : numberSubList;

		this.NUMBER_THREAD_EXECUTOR = numberThreadExecutor < DEFAULT_NUMBER_THREAD_EXECUTOR
				? DEFAULT_NUMBER_THREAD_EXECUTOR
				: numberThreadExecutor;

		this.ARRAY_SUB_LIST = new SubClientQueue[this.NUMBER_SUB_LIST];

		for (int i = 0; i < this.NUMBER_SUB_LIST; i++) {
			this.ARRAY_SUB_LIST[i] = new SubClientQueue();
		}

		this.ARRAY_RUNNER_THREAD = new RunnerThread[this.NUMBER_SUB_LIST];

		this.deauthenticator = new FTPDeauthenticator(DEFAULT_DEAUTHENTICATOR_TIMER,
				DEFAULT_DEAUTHENTICATION_TIMER_TIME_UNIT, this.ftpServerController);
	}

	public FTPClientManager(int numberThreadExecutor, FTPServerController FTPServerController) {
		this(DEFAULT_NUMBER_SUB_LIST, numberThreadExecutor, FTPServerController);
	}

	// Methods.

	@Override
	public synchronized void add(Socket socket) {
		FTPClient client = new FTPClient(socket);

		SubClientQueue subClientQueue = this.ARRAY_SUB_LIST[this.indexAddSocket];
		subClientQueue.offer(client);
		this.indexAddSocket = (this.indexAddSocket + 1) % DEFAULT_NUMBER_SUB_LIST;

		this.hashClientSubAueue.put(socket, subClientQueue);

		// For the frame.

		this.ftpServerController.getMainFrame().addFTPClientConnection(client);
	}

	@Override
	public void addAll(Collection<Socket> collection) {
		for (Socket socket : collection) {
			this.add(socket);
		}

	}

	/**
	 * <b>Remove the socket of the file manager and close the connection.</b>
	 * 
	 * @throws OperationNotSupportedException
	 * 
	 * @param socket
	 */
	@Override
	public void remove(Socket socket) throws OperationNotSupportedException {

		SubClientQueue subClientQueue = this.hashClientSubAueue.get(socket);

		if (subClientQueue != null) {
			subClientQueue.removeSocket(socket);
		}

	}

	/**
	 * <p>
	 * Prepare to kill the client.
	 * </p>
	 * <p>
	 * If the client can be kill, he is kill, but if the client has data transfert
	 * he can't be killed for the moment so he is prepare to killa dn kill after the
	 * data transfert finish.
	 * </p>
	 * 
	 * @param ftpClientConnection
	 */
	@Override
	public void preparekillClient(FTPClientConnection ftpClientConnection) {

		if (this.ftpServerController.getFTPDataTransfertManager()
				.ftpClientConnectionHasDataConnection(ftpClientConnection)) {

			ftpClientConnection.kill();
		} else {
			ftpClientConnection.kill();

			this.killClient(ftpClientConnection);
		}
	}

	@Override
	public void killClient(FTPClientConnection ftpClientConnection) {
		if (ftpClientConnection.isKill()) {
			try {
				this.remove(ftpClientConnection.getSocket());

				FTPAuthenticationManager ftpAuthenticationManager = FTPAuthenticationManager.getInstance();
				ftpAuthenticationManager.deauthentication(ftpClientConnection);

				try {
					ftpClientConnection.close();
				} catch (IOException e) {
					ErrorManager.writeError(e);
				}

			} catch (OperationNotSupportedException e) {
				ErrorManager.writeError(e);
			} catch (FailToDeauthenticateException e) {
				ErrorManager.writeError(e);
			}
		}
	}

	/**
	 * <b>OperationNotSupportedException!</b>
	 * 
	 * @throws OperationNotSupportedException
	 * 
	 * @param collection
	 */
	@Override
	public void removeAll(Collection<Socket> collection) throws OperationNotSupportedException {
		for (Socket socket : collection) {
			this.remove(socket);
		}
	}

	@Override
	protected void startClientManagingTreatment() throws Exception {
		this.startRunnerThread();
		// TODO this.deauthenticator.start();
	}

	@Override
	protected void stopClientManagingTreatment() throws Exception {
		this.stopRunnerThread();
		// TODO this.deauthenticator.stop();
	}

	/**
	 * <p>
	 * <b>MUST BE USE BY ONLY ONE THREAD.</b>
	 * </p>
	 * 
	 * <p>
	 * Return the next Client in the Sub queue. This method block if there is no
	 * Client in the sub client queue and wait until the subCommandWaitingLineQueue
	 * add an element.
	 * </p>
	 * <p>
	 * Return null if the sub queue is unlock and it does has element.
	 * </p>
	 * 
	 * @param subCommandWaitingLineQueue
	 * @return the next command of the next CommandWaitingLine in the Sub queue. If
	 *         the CommandWaitingLine has no element, return null.
	 */
	private FTPClient getClientFrom(SubClientQueue subClientQueue) {
		// We take the element at the head of the queue.
		FTPClient client = subClientQueue.poll();

		return client;
	}

	private void putClientAtTail(FTPClient client, SubClientQueue subClientQueue) {
		subClientQueue.offer(client);
	}

	/**
	 * Start all Runner Thread for all client socket sub list. If they are already
	 * launch, throw an exception.
	 * 
	 * @throws RunnerThreadAlreadyStarted
	 */
	private synchronized void startRunnerThread() throws RunnerThreadAlreadyStarted {
		if (!this.runnerThreadLaunch) {

			this.runnerThreadLaunch = true;

			for (int i = 0; i < this.ARRAY_RUNNER_THREAD.length; i++) {
				this.ARRAY_RUNNER_THREAD[i] = new RunnerThread(this.ARRAY_SUB_LIST[i]);
				Thread t = new Thread(this.ARRAY_RUNNER_THREAD[i]);
				t.start();
			}

		} else {
			throw new RunnerThreadAlreadyStarted();
		}
	}

	/**
	 * Stop all Runner Thread. If they are already stop throw an exception.
	 * 
	 * @throws RunnerThreadAlreadyStopped
	 */
	private synchronized void stopRunnerThread() throws RunnerThreadAlreadyStopped {
		if (this.runnerThreadLaunch) {

			this.runnerThreadLaunch = false;

			for (int i = 0; i < this.ARRAY_RUNNER_THREAD.length; i++) {
				this.ARRAY_RUNNER_THREAD[i].stop();
			}

		} else {
			throw new RunnerThreadAlreadyStopped();
		}
	}

	// Private class.

	/**
	 * This class is the sub list browse by only one runner thread. It list store a
	 * part of all client socket which are connect to the server. We can only browse
	 * the list and access to the element with an iterator.
	 * 
	 * @author Guillaume
	 *
	 */
	private class SubClientQueue {

		// Constants.

		private final Object LOCK = new Object();

		// Variables.

		private LinkedList<FTPClient> queueClient = new LinkedList<>();

		// Public methods.

		/**
		 * 
		 * <p>
		 * Insert the client at the tail of the queue. Return always true.
		 * </p>
		 * <p>
		 * Notify all thread which are waiting that the list add an element.
		 * </p>
		 * 
		 * @param e
		 * @return true
		 */
		public boolean offer(FTPClient e) {
			synchronized (this.LOCK) {
				this.queueClient.offer(e);

				this.LOCK.notifyAll();

				return true;
			}
		}

		@SuppressWarnings("unlikely-arg-type")
		public void removeSocket(Socket clientSocket) {

			this.queueClient.remove(clientSocket);

			// List<FTPClientConnection> listClientFind = new Vector<>();
			//
			// this.queueClient.forEach((ftpClientConnection) -> {
			// if (ftpClientConnection.getSocket() == clientSocket)
			// listClientFind.add(ftpClientConnection);
			// });
			//
			// for (FTPClientConnection ftpClientConnection : listClientFind) {
			// this.queueClient.remove(ftpClientConnection);
			// }
		}

		/**
		 * 
		 * <p>
		 * Return the next Client. If the queue is empty, this method block until one
		 * element is offer.
		 * </p>
		 * 
		 * @return the next Client.
		 */
		public FTPClient poll() {
			synchronized (this.LOCK) {

				if (this.queueClient.isEmpty()) {
					try {
						System.err.println("Runner thread de FTPClientManager BLOQUE.");
						this.LOCK.wait();
						System.err.println("Runner thread de FTPClientManager DE BLOQUE");
					} catch (InterruptedException e) {
						ErrorManager.writeError(e);
					}
				}

				return this.queueClient.poll();
			}
		}

		public void unLock() {
			synchronized (this.LOCK) {
				this.LOCK.notifyAll();
			}
		}

	}

	// Private class.

	/**
	 * This object is the thread which browse one of client socket sub list of the
	 * {@link ClientManager}. It look each client socket and read if there are
	 * queries or not. It disconnect and remove all client sockets which are closed.
	 * 
	 * @author guilr
	 *
	 */
	private class RunnerThread implements Runnable {

		// Constants.

		// Variables.

		private boolean isStopped = false;

		private SubClientQueue subClientQueue;

		// Constructors.

		public RunnerThread(SubClientQueue subClientQueue) {
			this.subClientQueue = subClientQueue;
		}

		// Public methods.

		@Override
		public void run() {
			while (!this.isStopped) {
				// We take the head of the sub queue.
				FTPClient client = FTPClientManager.this.getClientFrom(this.subClientQueue);

				if (client != null && !client.isClosed()) {
					Runnable[] commandReceived;
					try {
						commandReceived = FTPClientManager.this.extractCommandRunnable(client);

						if (commandReceived != null) {
							for (int i = 0; i < commandReceived.length; i++) {
								try {
									FTPClientManager.this.ftpServerController.commandExecutorExecuteFor(client,
											commandReceived[i]);
								} catch (NullPointerException e) {
									ErrorManager.writeError(e);
								} catch (RejectedExecutionException e) {

									ErrorManager.writeError(e);

									// We close the socket.
									try {
										client.close();
									} catch (IOException e1) {
										ErrorManager.writeError(e1);
									}
								}
							}
						}
					} catch (FailToExtractCommandException e) {
						ErrorManager.writeError(e);
					}

					if (!client.isClosed()) {
						FTPClientManager.this.putClientAtTail(client, this.subClientQueue);
					}
				}

			}

			System.err.println("Fin du Runner Thread dans FTPClientManager");
		}

		/**
		 * Stop the RunnerThread. It will finish his loop turn and after it will out of
		 * the loop.
		 */
		public void stop() {
			// ORDER VERY IMPORTANT.
			this.isStopped = true;
			this.subClientQueue.unLock();
		}

	}

}
