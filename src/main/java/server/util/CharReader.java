package server.util;

import java.io.IOException;

public interface CharReader {

	/**
	 * Reads a single character.
	 * 
	 * @return The character read, as an integer in the range 0 to 65535
	 *         (0x00-0xffff), or -1 if the end of the stream has been reached
	 */
	public int read() throws IOException;

	/**
	 * Reads characters into an array. This method will block until some input is
	 * available, an I/O error occurs, or the end of the stream is reached.
	 * 
	 * @param cbuf
	 * @return The number of characters read, or -1 if the end of the stream has
	 *         been reached
	 * @throws IOException
	 */
	public int read(char[] cbuf) throws IOException;

	/**
	 * Reads characters into a portion of an array. This method implements the
	 * general contract of the corresponding read method of the Reader class. As an
	 * additional convenience, it attempts to read as many characters as possible by
	 * repeatedly invoking the read method of the underlying stream. This iterated
	 * read continues until one of the following conditions becomes true:
	 * 
	 * The specified number of characters have been read, The read method of the
	 * underlying stream returns -1, indicating end-of-file, or The ready method of
	 * the underlying stream returns false, indicating that further input requests
	 * would block. If the first read on the underlying stream returns -1 to
	 * indicate end-of-file then this method returns -1. Otherwise this method
	 * returns the number of characters actually read. Subclasses of this class are
	 * encouraged, but not required, to attempt to read as many characters as
	 * possible in the same fashion.
	 * 
	 * Ordinarily this method takes characters from this stream's character buffer,
	 * filling it from the underlying stream as necessary. If, however, the buffer
	 * is empty, the mark is not valid, and the requested length is at least as
	 * large as the buffer, then this method will read characters directly from the
	 * underlying stream into the given array. Thus redundant BufferedReaders will
	 * not copy data unnecessarily.
	 * 
	 * @param cbuf
	 * @param off
	 * @param len
	 * @return The number of characters read, or -1 if the end of the stream has
	 *         been reached
	 * @throws IOException
	 */
	public int read(char[] cbuf, int off, int len) throws IOException;

	/**
	 * Reads a line of text. A line is considered to be terminated by any one of a
	 * line feed ('\n'), a carriage return ('\r'), or a carriage return followed
	 * immediately by a linefeed
	 * 
	 * @return A String containing the contents of the line, not including any
	 *         line-termination characters, or null if the end of the stream has
	 *         been reached
	 * @throws IOException
	 */
	public String readLine() throws IOException;

	/**
	 * Skips characters.
	 * 
	 * @param n
	 * @return The number of characters actually skipped
	 * @throws IOException
	 */
	public long skip(long n) throws IOException;

	/**
	 * Tells whether this stream is ready to be read. A buffered character stream is
	 * ready if the buffer is not empty, or if the underlying character stream is
	 * ready.
	 * 
	 * @return True if the next read() is guaranteed not to block for input, false
	 *         otherwise. Note that returning false does not guarantee that the next
	 *         read will block.
	 * @throws IOException
	 */
	public boolean ready() throws IOException;
}
