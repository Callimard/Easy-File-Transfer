package server.ftp.sql.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import server.ftp.sql.table_row.AuthenticationRow;

public class AuthenticationDAO extends DAO<AuthenticationRow> {

	// Constants.

	// Variables.

	// Constructors.

	public AuthenticationDAO() {
	}

	// Methods.

	@Override
	public void create(AuthenticationRow tableRow, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("INSERT INTO ");
		query.append(tableRow.tableName);

		query.append(" (");

		query.append(AuthenticationRow.ID_CLIENT);
		query.append(", ");
		query.append(AuthenticationRow.AUTHENTICATION_DATE);
		query.append(", ");
		query.append(AuthenticationRow.LAST_QUERY_DATE);
		query.append(", ");
		query.append(AuthenticationRow.CLIENT_IP);

		query.append(") VALUES (");

		query.append(DAO.formatIdForSQLQuery(tableRow.getIdClient()));
		query.append(", ");
		query.append(
				tableRow.getAuthenticationDate() != null ? DAO.toDateTimeSQLString(tableRow.getAuthenticationDate())
						: null);
		query.append(", ");
		query.append(tableRow.getLastQueryDate() != null ? DAO.toDateTimeSQLString(tableRow.getLastQueryDate()) : null);
		query.append(", ");
		query.append(DAO.quoteEncapsulation(tableRow.getClientIp()));

		query.append(")");

		Statement statement = null;
		ResultSet result = null;

		try {

			statement = SQLConnection.createStatement();

			statement.executeUpdate(query.toString(), Statement.RETURN_GENERATED_KEYS);

			result = statement.getGeneratedKeys();

			if (result.next())
				tableRow.setIdAuthentication(result.getLong(1));
			else
				throw new SQLException("No Primary key generated.");

			if (commit) {
				SQLConnection.commit();
			}
		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

	@Override
	public void update(AuthenticationRow tableRow, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("UPDATE ");
		query.append(tableRow.tableName);
		query.append(" SET ");

		query.append(AuthenticationRow.ID_CLIENT);
		query.append(" = ");
		query.append(DAO.formatIdForSQLQuery(tableRow.getIdClient()));
		query.append(", ");

		query.append(AuthenticationRow.AUTHENTICATION_DATE);
		query.append(" = ");
		query.append(
				tableRow.getAuthenticationDate() != null ? DAO.toDateTimeSQLString(tableRow.getAuthenticationDate())
						: null);
		query.append(", ");

		query.append(AuthenticationRow.LAST_QUERY_DATE);
		query.append(" = ");
		query.append(tableRow.getLastQueryDate() != null ? DAO.toDateTimeSQLString(tableRow.getLastQueryDate()) : null);
		query.append(", ");

		query.append(AuthenticationRow.CLIENT_IP);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getClientIp()));

		query.append(" WHERE ");

		query.append(AuthenticationRow.ID_AUTHENTICATION);
		query.append(" = ");
		query.append(tableRow.getIdAuthentication());

		Statement statement = null;

		try {

			statement = SQLConnection.createStatement();

			statement.executeUpdate(query.toString());

			if (commit) {
				SQLConnection.commit();
			}

		} finally {
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void delete(AuthenticationRow tableRow, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("DELETE FROM ");
		query.append(tableRow.tableName);
		query.append(" WHERE ");

		query.append(AuthenticationRow.ID_AUTHENTICATION);
		query.append(" = ");
		query.append(tableRow.getIdAuthentication());

		Statement statement = null;

		try {

			statement = SQLConnection.createStatement();

			statement.executeUpdate(query.toString());

			if (commit) {
				SQLConnection.commit();
			}

		} finally {
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void delete(Long id, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("DELETE FROM ");
		query.append(AuthenticationRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(AuthenticationRow.ID_AUTHENTICATION);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(id));

		Statement statement = null;

		try {

			statement = SQLConnection.createStatement();

			statement.executeUpdate(query.toString());

			if (commit) {
				SQLConnection.commit();
			}

		} finally {
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public AuthenticationRow find(long id, Connection SQLConnection) throws SQLException {
		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_TIME_FORMAT);

		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append(AuthenticationRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(AuthenticationRow.ID_AUTHENTICATION);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(id));

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			if (result.next()) {
				AuthenticationRow authentication = new AuthenticationRow();
				authentication.setIdAuthentication(result.getLong(1));
				authentication.setIdClient(result.getLong(2));

				try {
					// Can't be null.
					authentication.setAuthenticationDate(dateFormat.parse(result.getString(3)));

					String lastQueryDate = result.getString(4);
					authentication.setLastQueryDate(lastQueryDate != null ? dateFormat.parse(lastQueryDate) : null);
				} catch (ParseException e) {
					throw new SQLException("Bad format for the date.", e);
				}

				authentication.setClientIp(result.getString(5));

				return authentication;
			}

			return null;

		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

	public List<AuthenticationRow> findByIp(String ip, Connection SQLConnection) throws SQLException {
		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_TIME_FORMAT);

		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append(AuthenticationRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(AuthenticationRow.CLIENT_IP);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(ip));

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			List<AuthenticationRow> listAuthentication = new ArrayList<>(result.getFetchSize());

			while (result.next()) {
				AuthenticationRow authentication = new AuthenticationRow();
				authentication.setIdAuthentication(result.getLong(1));
				authentication.setIdClient(result.getLong(2));

				try {
					// Can't be null.
					authentication.setAuthenticationDate(dateFormat.parse(result.getString(3)));

					String lastQueryDate = result.getString(4);
					authentication.setLastQueryDate(lastQueryDate != null ? dateFormat.parse(lastQueryDate) : null);
				} catch (ParseException e) {
					throw new SQLException("Bad format for the date.", e);
				}

				authentication.setClientIp(result.getString(5));

				listAuthentication.add(authentication);
			}

			return listAuthentication.isEmpty() ? null : listAuthentication;

		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

	public List<AuthenticationRow> findByIdClient(long idClient, Connection SQLConnection) throws SQLException {
		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_TIME_FORMAT);

		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append(AuthenticationRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(AuthenticationRow.ID_CLIENT);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(idClient));

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			List<AuthenticationRow> listAuthentication = new ArrayList<>(result.getFetchSize());

			while (result.next()) {
				AuthenticationRow authentication = new AuthenticationRow();
				authentication.setIdAuthentication(result.getLong(1));
				authentication.setIdClient(result.getLong(2));

				try {
					// Can't be null.
					authentication.setAuthenticationDate(dateFormat.parse(result.getString(3)));

					String lastQueryDate = result.getString(4);
					authentication.setLastQueryDate(lastQueryDate != null ? dateFormat.parse(lastQueryDate) : null);
				} catch (ParseException e) {
					throw new SQLException("Bad format for the date.", e);
				}

				authentication.setClientIp(result.getString(5));

				listAuthentication.add(authentication);
			}

			return listAuthentication.isEmpty() ? null : listAuthentication;

		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

	/**
	 * 
	 * @param SQLConnection
	 * @return all authentication row in the table, if there are not authentication
	 *         row, return null.
	 * @throws SQLException
	 */
	public List<AuthenticationRow> getAll(Connection SQLConnection) throws SQLException {
		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_TIME_FORMAT);

		StringBuilder query = new StringBuilder("SELECT * FROM ");

		query.append(AuthenticationRow.TABLE_NAME);

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			List<AuthenticationRow> listAuthentication = new Vector<>(result.getFetchSize());

			while (result.next()) {
				AuthenticationRow authentication = new AuthenticationRow();
				authentication.setIdAuthentication(result.getLong(1));
				authentication.setIdClient(result.getLong(2));

				try {
					// Can't be null.
					authentication.setAuthenticationDate(dateFormat.parse(result.getString(3)));

					String lastQueryDate = result.getString(4);
					authentication.setLastQueryDate(lastQueryDate != null ? dateFormat.parse(lastQueryDate) : null);
				} catch (ParseException e) {
					throw new SQLException("Bad format for the date.", e);
				}

				authentication.setClientIp(result.getString(5));

				listAuthentication.add(authentication);
			}

			return listAuthentication.isEmpty() ? null : listAuthentication;

		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

}
