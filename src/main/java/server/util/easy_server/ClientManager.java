package server.util.easy_server;

import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.Callable;

import javax.naming.OperationNotSupportedException;

import server.util.easy_server.exception.client_manager.ClientManagerAlreadyManagingClientException;
import server.util.easy_server.exception.client_manager.ClientManagerNotManagingClientException;
import server.util.easy_server.exception.client_manager.StartingClientManagingFailedException;
import server.util.easy_server.exception.client_manager.StoppingClientManagingFailedException;
import server.util.easy_server.exception.command_reader.FailToExtractCommandException;

/**
 * <p>
 * This class manage all client which is connected to the server. It is this
 * class which look all clients read them stream to know if the client send
 * something or not. The object manage also them disconnection.
 * </p>
 * 
 * @author Callimard
 *
 * @param <T>
 */
public abstract class ClientManager<T extends ClientConnection> {

	// Private.

	private CommandReader<T> commandExtractor;

	private boolean isManagingClient = false;

	// Constructors.

	protected ClientManager(CommandReader<T> commandExtractor) {

		this.commandExtractor = commandExtractor;
	}

	// Methods.

	public abstract void add(Socket socket);

	public abstract void addAll(Collection<Socket> collection);

	public abstract void remove(Socket socket) throws OperationNotSupportedException;

	public abstract void removeAll(Collection<Socket> collection) throws OperationNotSupportedException;

	public abstract void preparekillClient(T client);

	public abstract void killClient(T client);

	/**
	 * <p>
	 * Treatment done during the call of {@link ClientManager#startClientManaging}
	 * method.
	 * </p>
	 * <p>
	 * This function allow to class which implements {@link ClientManager} class to
	 * add treatment during the <b>startClientManaging</b> method.
	 * </p>
	 * 
	 * @throws Exception
	 */
	protected abstract void startClientManagingTreatment() throws Exception;

	/**
	 * <p>
	 * Treatment done during the call of {@link ClientManager#stopClientManaging}
	 * and {@link ClientManager#stopClientManagingNow()} method.
	 * </p>
	 * 
	 * <p>
	 * This function allow to class which implements {@link ClientManager} class to
	 * add treatment during the <b>stopClientManaging</b> and
	 * <b>stopClientManagingNow</b> method.
	 * </p>
	 * 
	 * <p>
	 * <b> You should not shut down the command executor in this method.</b>
	 * </p>
	 * 
	 * @throws Exception
	 */
	protected abstract void stopClientManagingTreatment() throws Exception;

	/**
	 * <p>
	 * The client manager can begin manage client. You can add client before start
	 * the client managing, then client which are add before the start will be
	 * managed after the start.
	 * </p>
	 * 
	 * @throws ClientManagerAlreadyManagingClientException
	 * @throws StartingClientManagingFailedException
	 */
	public final synchronized void startClientManaging()
			throws ClientManagerAlreadyManagingClientException, StartingClientManagingFailedException {
		if (!this.isManagingClient) {
			try {
				this.startClientManagingTreatment();
			} catch (Exception e) {
				throw new StartingClientManagingFailedException(e);
			}

			this.isManagingClient = true;
		} else {
			throw new ClientManagerAlreadyManagingClientException();
		}
	}

	/**
	 * <p>
	 * Stop the client managing. Finish all commands which have been give to the
	 * command executor.
	 * </p>
	 * 
	 * @throws ClientManagerNotManagingClientException
	 * @throws StoppingClientManagingFailedException
	 */
	public final synchronized void stopClientManaging()
			throws ClientManagerNotManagingClientException, StoppingClientManagingFailedException {
		if (this.isManagingClient) {

			try {
				this.stopClientManagingTreatment();
			} catch (Exception e) {
				throw new StoppingClientManagingFailedException(e);
			}

			this.isManagingClient = false;
		} else {
			throw new ClientManagerNotManagingClientException();
		}
	}

	public Runnable[] extractCommandRunnable(T clientConnection) throws FailToExtractCommandException {
		return commandExtractor.extractCommandRunnable(clientConnection);
	}

	public Callable<?>[] extractCommandCallable(T clientConnection) throws FailToExtractCommandException {
		return commandExtractor.extractCommandCallable(clientConnection);
	}

	// Getters and Setters.

	public boolean isManagingClient() {
		return this.isManagingClient;
	}
}
