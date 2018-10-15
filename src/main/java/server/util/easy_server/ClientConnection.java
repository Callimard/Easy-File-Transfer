package server.util.easy_server;

import java.io.Closeable;

import server.util.CharReaderWriter;

public interface ClientConnection extends CharReaderWriter, Closeable {

	/**
	 * Verify if the Client Connection is closed or not.
	 * 
	 * @return true if the connection is close, else false.
	 */
	public boolean isClosed();

	/**
	 * Returns the IP address string in textual presentation.
	 * 
	 * @return a hash code value for this IP address.
	 */
	public String getHostAddress();

	/**
	 * 
	 * @return the host port of the connection.
	 */
	public int getPort();
}
