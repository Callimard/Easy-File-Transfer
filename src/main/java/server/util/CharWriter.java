package server.util;

import java.io.IOException;

public interface CharWriter {

	/**
	 * Writes an array of characters
	 * 
	 * @param cbuf
	 * @throws IOException
	 */
	public void write(char[] cbuf) throws IOException;

	/**
	 * Writes a portion of an array of characters. Ordinarily this method stores
	 * characters from the given array into this stream's buffer, flushing the
	 * buffer to the underlying stream as needed. If the requested length is at
	 * least as large as the buffer, however, then this method will flush the buffer
	 * and write the characters directly to the underlying stream. Thus redundant
	 * BufferedWriters will not copy data unnecessarily.
	 * 
	 * @param cbuf
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	public void write(char[] cbuf, int off, int len) throws IOException;

	/**
	 * Writes a single character.
	 * 
	 * @param c
	 * @throws IOException
	 */
	public void write(int c) throws IOException;

	/**
	 * Writes a string.
	 * 
	 * @param string
	 * @throws IOException
	 */
	public void write(String string) throws IOException;

	/**
	 * Writes a portion of a String. If the value of the len parameter is negative
	 * then no characters are written. This is contrary to the specification of this
	 * method in the superclass, which requires that an IndexOutOfBoundsException be
	 * thrown.
	 * 
	 * @param s
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	public void write(String s, int off, int len) throws IOException;

	/**
	 * Writes a line separator. The line separator string is defined by the system
	 * property line.separator, and is not necessarily a single newline ('\n')
	 * character.
	 * 
	 * @throws IOException
	 */
	public void newLine() throws IOException;

	/**
	 * Flushes the stream.
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException;

}
