package server.ftp.sql.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import server.sql.table_row.TableRow;

public abstract class DAO<T extends TableRow> {

	// Constants.

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String SQL_DATE_FORMAT = "%Y-%m-%d";

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static final String SQL_DATE_TIME_FORMAT = "%Y-%m-%d %H:%i:%s";

	// Variables.

	// Constructors.

	// Abstract methods.

	/**
	 * <p>
	 * Create the table in the data base.
	 * </p>
	 * <p>
	 * Update the table row with the primary key generated
	 * </p>
	 * <p>
	 * Can throw a SQLException.
	 * </p>
	 * 
	 * @param tableRow
	 *            - the references table row
	 * @param SQLConnection
	 *            - the SQL connection
	 * @param commit
	 *            - boolean to know if we commit or not
	 * 
	 * 
	 * @throws SQLException
	 */
	public abstract void create(T tableRow, Connection SQLConnection, boolean commit) throws SQLException;

	/**
	 * <p>
	 * Update the table row.
	 * </p>
	 * 
	 * @param tableRow
	 *            - the references table row
	 * @param SQLConnection
	 *            - the SQL connection
	 * @param commit
	 *            - boolean to know if we commit or not
	 * 
	 * @throws SQLException
	 */
	public abstract void update(T tableRow, Connection SQLConnection, boolean commit) throws SQLException;

	/**
	 * <p>
	 * Delete the table row.
	 * </p>
	 * 
	 * @param tableRow
	 *            - the references table row
	 * @param SQLConnection
	 *            - the SQL connection
	 * @param commit
	 *            - boolean to know if we commit or not
	 * 
	 * @throws SQLException
	 */
	public abstract void delete(T tableRow, Connection SQLConnection, boolean commit) throws SQLException;

	/**
	 * <p>
	 * Delete the table row.
	 * </p>
	 * 
	 * @param id
	 *            - the id of the row
	 * @param SQLConnection
	 *            - the SQL connection
	 * @param commit
	 *            - boolean to know if we commit or not
	 * 
	 * @throws SQLException
	 */
	public abstract void delete(Long id, Connection SQLConnection, boolean commit) throws SQLException;

	/**
	 * <p>
	 * Find the table row corresponding with the id. If there is no row return null.
	 * </p>
	 * 
	 * @param id
	 *            - the id of the table row
	 * @param SQLConnection
	 *            - the SQL connection
	 * 
	 * @return the table row corresponding with the id, if there is no row return
	 *         null.
	 * 
	 * @throws SQLException
	 */
	public abstract T find(long id, Connection SQLConnection) throws SQLException;
	
	// Methods.

	public static String toDateSQLString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		StringBuilder builder = new StringBuilder("STR_TO_DATE(");

		builder.append('\'');
		builder.append(dateFormat.format(date));
		builder.append('\'');

		builder.append(", ");

		builder.append('\'');
		builder.append(SQL_DATE_FORMAT);
		builder.append('\'');

		builder.append(")");

		return builder.toString();
	}

	public static String toDateTimeSQLString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);

		StringBuilder builder = new StringBuilder("STR_TO_DATE(");

		builder.append('\'');
		builder.append(dateFormat.format(date));
		builder.append('\'');

		builder.append(", ");

		builder.append('\'');
		builder.append(SQL_DATE_TIME_FORMAT);
		builder.append('\'');

		builder.append(")");

		return builder.toString();
	}

	/**
	 * Format the id for a SQL query. If the id is null return null else if the id
	 * is less than 0 return null else return the id.
	 * 
	 * @param id
	 *            - the id to formated
	 * @return the id formated for a SQL query.
	 */
	public static Long formatIdForSQLQuery(Long id) {
		if (id != null)
			return id <= 0 ? null : id;
		else
			return null;
	}

	public static String quoteEncapsulation(String string) {
		if (string != null && string.isEmpty())
			string = null;

		StringBuilder builder = new StringBuilder("\"");
		builder.append(string);
		builder.append("\"");

		return builder.toString();
	}

	public static String quoteEncapsulation(Object o) {
		StringBuilder builder = new StringBuilder("\"");
		builder.append(o);
		builder.append("\"");

		return builder.toString();
	}
}
