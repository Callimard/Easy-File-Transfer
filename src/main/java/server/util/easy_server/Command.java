package server.util.easy_server;

/**
 * <p>
 * This class represent a command which a client as ask.
 * </p>
 * <p>
 * Currently you create a command with is constructors and after you call the
 * {@link Command#execute(ClientConnection, String...)} methods in a thread
 * which will use the command.
 * </p>
 * 
 * @author Callimard
 *
 * @param <T>
 */
public abstract class Command<T extends ClientConnection> implements Runnable {

	// Constants.

	// Variables.

	private final T clientConnection;
	
	private final String commandName;

	// Constructors.

	protected Command(String commandName, T clientConnection) {
		this.commandName = commandName;
		
		this.clientConnection = clientConnection;
	}

	// Methods.

	// Getters and Setters.

	public String getCommandName() {
		return this.commandName;
	}

	public T getClientConnection() {
		return this.clientConnection;
	}
}
