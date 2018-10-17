package server.ftp.sql.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import server.ftp.sql.table_row.ClientInformationRow;
import server.ftp.sql.table_row.ClientRow;

public class ClientInformationDAO extends DAO<ClientInformationRow> {

	// Variables.

	// Constructors.

	public ClientInformationDAO() {
	}

	// Methods.

	/**
	 * <p>
	 * Create the ClientInformation table.
	 * </p>
	 * <p>
	 * <strong>Update in the same time the Client Row which is link with this
	 * ClientInformation if and only if the Client exists.</strong>
	 * </p>
	 * <p>
	 * If the Client does not exist, throws a SQLException.
	 * </p>
	 */
	@Override
	public void create(ClientInformationRow tableRow, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("INSERT INTO ");
		query.append(tableRow.tableName);

		query.append(" (");

		query.append(ClientInformationRow.ID_CLIENT);
		query.append(", ");
		query.append(ClientInformationRow.TEL);
		query.append(", ");
		query.append(ClientInformationRow.ADDRESS);
		query.append(", ");
		query.append(ClientInformationRow.CITY);
		query.append(", ");
		query.append(ClientInformationRow.COUNTRY);
		query.append(", ");
		query.append(ClientInformationRow.BIRTHDAY);

		query.append(") VALUES (");

		query.append(DAO.formatIdForSQLQuery(tableRow.getIdClient()));
		query.append(", ");
		query.append(DAO.quoteEncapsulation(tableRow.getTel()));
		query.append(", ");
		query.append(DAO.quoteEncapsulation(tableRow.getAddress()));
		query.append(", ");
		query.append(DAO.quoteEncapsulation(tableRow.getCity()));
		query.append(", ");
		query.append(DAO.quoteEncapsulation(tableRow.getCountry()));
		query.append(", ");
		query.append(tableRow.getBirthday() != null ? DAO.toDateSQLString(tableRow.getBirthday()) : null);
		query.append(")");

//		System.out.println("Query = " + query);

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			statement.executeUpdate(query.toString(), Statement.RETURN_GENERATED_KEYS);

			result = statement.getGeneratedKeys();

			Long primaryKey = null;

			if (result.next())
				primaryKey = result.getLong(1);
			else
				throw new SQLException("No Primary key generated.");

			// We update the client.

			ClientDAO clientDAO = new ClientDAO();
			ClientRow client = clientDAO.find(tableRow.getIdClient(), SQLConnection);

			client.setIdClientInformation(primaryKey);
			clientDAO.update(client, SQLConnection, false);

			tableRow.setIdClientInformation(primaryKey);

			if (commit) {
				SQLConnection.commit();
			}

		} catch (SQLException e) {
			SQLConnection.rollback();
			throw new SQLException(e);
		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

	@Override
	public void update(ClientInformationRow tableRow, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("UPDATE ");
		query.append(tableRow.tableName);
		query.append(" SET ");

		query.append(ClientInformationRow.ID_CLIENT);
		query.append(" = ");
		query.append(DAO.formatIdForSQLQuery(tableRow.getIdClient()));
		query.append(", ");

		query.append(ClientInformationRow.TEL);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getTel()));
		query.append(", ");

		query.append(ClientInformationRow.ADDRESS);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getAddress()));
		query.append(", ");

		query.append(ClientInformationRow.CITY);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getCity()));
		query.append(", ");

		query.append(ClientInformationRow.COUNTRY);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getCountry()));
		query.append(", ");

		query.append(ClientInformationRow.BIRTHDAY);
		query.append(" = ");
		query.append(tableRow.getBirthday() != null ? DAO.toDateSQLString(tableRow.getBirthday()) : null);

		query.append(" WHERE ");

		query.append(ClientInformationRow.ID_CLIENT_INFORMATION);
		query.append(" = ");
		query.append(tableRow.getIdClientInformation());

//		System.out.println("Query = " + query);

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
	public void delete(ClientInformationRow tableRow, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("DELETE FROM ");
		query.append(tableRow.tableName);
		query.append(" WHERE ");

		query.append(ClientInformationRow.ID_CLIENT_INFORMATION);
		query.append(" = ");
		query.append(tableRow.getIdClientInformation());

//		System.out.println("Query : " + query);

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
		query.append(ClientInformationRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(ClientInformationRow.ID_CLIENT_INFORMATION);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(id));

//		System.out.println("Query : " + query);

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
	public ClientInformationRow find(long id, Connection SQLConnection) throws SQLException {
		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_FORMAT);

		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append(ClientInformationRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(ClientInformationRow.ID_CLIENT_INFORMATION);
		query.append(" = ");
		query.append(id);

//		System.out.println("Query : " + query);

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			if (result.next()) {
				ClientInformationRow clientInformation = new ClientInformationRow();
				clientInformation.setIdClientInformation(result.getLong(1));
				clientInformation.setIdClient(result.getLong(2));
				clientInformation.setTel(result.getString(3));
				clientInformation.setAddress(result.getString(4));
				clientInformation.setCity(result.getString(5));
				clientInformation.setCountry(result.getString(6));

				String birthday = result.getString(7);
				if (birthday != null)
					try {
						clientInformation.setBirthday(dateFormat.parse(birthday));
					} catch (ParseException e) {
						throw new SQLException("Bad format for the date.", e);
					}
				else
					clientInformation.setBirthday(null);

				return clientInformation;
			}

			return null;
		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

	public ClientInformationRow findByIdClient(Long idClient, Connection SQLConnection) throws SQLException {
		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_FORMAT);

		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append(ClientInformationRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(ClientInformationRow.ID_CLIENT);
		query.append(" = ");
		query.append(idClient);

//		System.out.println("Query : " + query);

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			if (result.next()) {
				ClientInformationRow clientInformation = new ClientInformationRow();
				clientInformation.setIdClientInformation(result.getLong(1));
				clientInformation.setIdClient(result.getLong(2));
				clientInformation.setTel(result.getString(3));
				clientInformation.setAddress(result.getString(4));
				clientInformation.setCity(result.getString(5));
				clientInformation.setCountry(result.getString(6));

				String birthday = result.getString(7);
				if (birthday != null)
					try {
						clientInformation.setBirthday(dateFormat.parse(birthday));
					} catch (ParseException e) {
						throw new SQLException("Bad format for the date.", e);
					}
				else
					clientInformation.setBirthday(null);

				return clientInformation;
			}

			return null;
		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

}
