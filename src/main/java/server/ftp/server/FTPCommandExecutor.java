package server.ftp.server;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import server.ftp.exception.command_executor.RunnerThreadAlreadyStartedException;
import server.ftp.exception.command_executor.RunnerThreadAlreadyStoppedException;
import server.util.ErrorManager;

public class FTPCommandExecutor {

	// Constants.

	private final Object LOCK = new Object();

	private static final int MINIMAL_SUB_QUEUE = 3;

	// Variables.

	private ConcurrentHashMap<FTPClientConnection, CommandWaitingLine> hashCommandWaitingLine = new ConcurrentHashMap<>();

	private final SubCommandWaitingLineQueue[] ARRAY_SUB_QUEUE;

	private final RunnerThread[] ARRAY_RUNNER_THREAD;

	private final int NUMBER_SUB_LIST;

	private int indexSubQueue = 0;

	private boolean runnerThreadAreStarted = false;

	// Constructors.

	public FTPCommandExecutor(int nbSubQueue) {
		if (nbSubQueue < MINIMAL_SUB_QUEUE)
			this.NUMBER_SUB_LIST = MINIMAL_SUB_QUEUE;
		else
			this.NUMBER_SUB_LIST = nbSubQueue;

		this.ARRAY_SUB_QUEUE = new SubCommandWaitingLineQueue[this.NUMBER_SUB_LIST];

		for (int i = 0; i < this.ARRAY_SUB_QUEUE.length; i++) {
			this.ARRAY_SUB_QUEUE[i] = new SubCommandWaitingLineQueue();
		}

		this.ARRAY_RUNNER_THREAD = new RunnerThread[this.NUMBER_SUB_LIST];
	}

	public FTPCommandExecutor() {
		this(MINIMAL_SUB_QUEUE);
	}

	// Methods.

	private void addCommandWaitingLine(CommandWaitingLine commandWaitingLine) {
		synchronized (this.LOCK) {
			SubCommandWaitingLineQueue subCommandWaitingLineQueue = this.ARRAY_SUB_QUEUE[this.indexSubQueue];

			subCommandWaitingLineQueue.offer(commandWaitingLine);

			this.indexSubQueue = (this.indexSubQueue + 1) % this.NUMBER_SUB_LIST;
		}
	}

	/**
	 * <p>
	 * <b>MUST BE USE BY ONLY ONE THREAD.</b>
	 * </p>
	 * 
	 * <p>
	 * Return the next command of the next CommandWaitingLine in the Sub queue. This
	 * method block if there is no CommandWaitingLine in the
	 * subCommandWaitingLineQueue until the subCommandWaitingLineQueue add an
	 * element.
	 * </p>
	 * <p>
	 * Return null if the current CommandWaitingLine has no command for the moment.
	 * </p>
	 * 
	 * @param subCommandWaitingLineQueue
	 * @return the next command of the next CommandWaitingLine in the Sub queue. If
	 *         the CommandWaitingLine has no element, return null.
	 */
	private Runnable getNextCommandFrom(SubCommandWaitingLineQueue subCommandWaitingLineQueue) {

		// We take the element at the head of the queue.
		CommandWaitingLine commandWaitingLine = subCommandWaitingLineQueue.poll();

		Runnable command = null;
		if (commandWaitingLine != null) {
			command = commandWaitingLine.poll();

			// We make the element at the tail of the queue.
			subCommandWaitingLineQueue.offer(commandWaitingLine);
		}

		return command;
	}

	public void executeCommandFor(FTPClientConnection ftpClientConnection, Runnable command) {

		synchronized (this.LOCK) {

			CommandWaitingLine commandWaitingLine = this.hashCommandWaitingLine.get(ftpClientConnection);

			if (commandWaitingLine == null) {

				commandWaitingLine = new CommandWaitingLine();
				this.addCommandWaitingLine(commandWaitingLine);
				this.hashCommandWaitingLine.put(ftpClientConnection, commandWaitingLine);

			}

			commandWaitingLine.offer(command);

		}

	}

	public synchronized void startRunnerThread() throws RunnerThreadAlreadyStartedException {
		if (!this.runnerThreadAreStarted) {

			this.runnerThreadAreStarted = true;

			for (int i = 0; i < this.ARRAY_RUNNER_THREAD.length; i++) {
				this.ARRAY_RUNNER_THREAD[i] = new RunnerThread(this.ARRAY_SUB_QUEUE[i]);
				Thread t = new Thread(this.ARRAY_RUNNER_THREAD[i]);
				t.start();
			}

		} else {
			throw new RunnerThreadAlreadyStartedException();
		}
	}

	public synchronized void stopAllRunnerThread() throws RunnerThreadAlreadyStoppedException {
		if (this.runnerThreadAreStarted) {

			this.runnerThreadAreStarted = false;

			for (int i = 0; i < this.ARRAY_RUNNER_THREAD.length; i++) {
				RunnerThread runnerThread = this.ARRAY_RUNNER_THREAD[i];
				runnerThread.stop();
			}

		} else {
			throw new RunnerThreadAlreadyStoppedException();
		}
	}

	// Intern class.

	private class CommandWaitingLine {

		// Variables.

		private ConcurrentLinkedQueue<Runnable> queueCommandTask = new ConcurrentLinkedQueue<>();

		// Constructors.

		// Methods.

		public boolean offer(Runnable e) {
			return this.queueCommandTask.offer(e);
		}

		public Runnable poll() {
			return this.queueCommandTask.poll();
		}

	}

	private class SubCommandWaitingLineQueue {

		// Constants.

		private final Object LOCK = new Object();

		// Variables.

		private final LinkedList<CommandWaitingLine> queueCommandWaitingLine = new LinkedList<>();

		// Methods.

		/**
		 * 
		 * <p>
		 * Insert the command waiting line at the tail of the queue. Return always true.
		 * </p>
		 * <p>
		 * Notify all thread which are waiting that the list add an element.
		 * </p>
		 * 
		 * @param e
		 * @return true
		 */
		public boolean offer(CommandWaitingLine e) {
			synchronized (this.LOCK) {
				this.queueCommandWaitingLine.offer(e);

				this.LOCK.notifyAll();

				return true;
			}
		}

		/**
		 * 
		 * <p>
		 * Return the next Command Waiting Line. If the queue is empty, this method
		 * block until one element is offer.
		 * </p>
		 * 
		 * @return the next Command Waiting Line.
		 */
		public CommandWaitingLine poll() {
			synchronized (this.LOCK) {

				if (this.queueCommandWaitingLine.isEmpty()) {
					try {
						System.err.println("Runner thread de FTPCommandExecutor BLOQUE.");
						this.LOCK.wait();
					} catch (InterruptedException e) {
						ErrorManager.writeError(e);
					}
				}

				return this.queueCommandWaitingLine.poll();
			}
		}

		public void unLock() {
			synchronized (this.LOCK) {
				this.LOCK.notifyAll();
			}
		}

	}

	private class RunnerThread implements Runnable {

		// Variables.

		private boolean isStopped = false;

		private SubCommandWaitingLineQueue subCommandWaitingLineQueue;

		// Constructors.

		public RunnerThread(SubCommandWaitingLineQueue subCommandWaitingLineQueue) {
			this.subCommandWaitingLineQueue = subCommandWaitingLineQueue;
		}

		// Methods.

		@Override
		public void run() {

			while (!this.isStopped) {

				Runnable command = FTPCommandExecutor.this.getNextCommandFrom(this.subCommandWaitingLineQueue);

				if (command != null)
					// We execute the command.
					command.run();
			}

			System.err.println("Fin du Runner Thread dans FTPCommandExecutor");

		}

		// Getters and Setters.

		public void stop() {
			// ORDER VERY IMPORTANT.
			this.isStopped = true;
			this.subCommandWaitingLineQueue.unLock();
		}

	}

}
