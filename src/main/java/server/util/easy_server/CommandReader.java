package server.util.easy_server;

import java.util.concurrent.Callable;

import server.util.easy_server.exception.command_reader.FailToExtractCommandException;

public interface CommandReader<T extends ClientConnection> {

	/**
	 * <p>
	 * This methods extract all runnable command read in the socket
	 * </p>
	 * 
	 * <p>
	 * In function of the implementation, <b>this method can block.</b>
	 * </p>
	 * 
	 * @param clientConnection
	 * 
	 * @return an array which contains all runnable command read in the socket. If
	 *         there is no runnable command, return null.
	 */
	public Runnable[] extractCommandRunnable(T clientConnection) throws FailToExtractCommandException;

	/**
	 * *
	 * <p>
	 * This methods extract all callable command read in the socket
	 * </p>
	 * 
	 * <p>
	 * In function of the implementation, <b>this method can block.</b>
	 * </p>
	 * 
	 * @param clientConnection
	 * 
	 * @return an array which contans all callable command read in the socket. If
	 *         there is no callable command, return null.
	 */
	public Callable<?>[] extractCommandCallable(T clientConnection) throws FailToExtractCommandException;

}
