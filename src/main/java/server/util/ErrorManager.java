package server.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import server.ftp.sql.dao.DAO;

public class ErrorManager {

	// Constants.

	public static final File DIRECTORY_ERRORS = new File(new String("errors"));
	public static final File FILE_ERRORS = new File(new String("errors/errors_report.txt"));

	private static final Object LOCK = new Object();

	private volatile static ErrorManager SINGLE = new ErrorManager();

	// Variables.

	private volatile PrintStream WRITER;

	// Constructors.

	private ErrorManager() {
		this.createFILE_ERRORS();
		try {
			if (this.WRITER == null)
				this.WRITER = new PrintStream(FILE_ERRORS);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Private methods.

	private void createFILE_ERRORS() {
		if (!DIRECTORY_ERRORS.exists()) {
			DIRECTORY_ERRORS.mkdirs();
			if (!FILE_ERRORS.exists()) {
				try {
					FILE_ERRORS.createNewFile();
					this.WRITER = new PrintStream(FILE_ERRORS);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Public methods.

	public static void writeError(Exception e) {
		synchronized (LOCK) {
			e.printStackTrace();

			SimpleDateFormat dateFormat = new SimpleDateFormat(DAO.DATE_TIME_FORMAT);
			
			SINGLE.createFILE_ERRORS();

			SINGLE.WRITER.print("At " + dateFormat.format(new Date()) + " : ");
			e.printStackTrace(SINGLE.WRITER);
		}
	}

}
