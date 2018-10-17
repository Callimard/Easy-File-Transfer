package server.ftp.sql.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import server.ftp.sql.table_row.AuthenticationRow;
import server.ftp.sql.table_row.ClientRow;

public class ClientDAO extends DAO<ClientRow> {

	// Constants.

	// Variables.

	// Constructors.

	public ClientDAO() {
	}

	// Methods.

	/**
	 * <p>
	 * The column id_client_information is not use in the creation.
	 * </p>
	 * <p>
	 * To link a client with client information you need to create a
	 * ClientInformation with the DAO {@link ClientInformationDAO} and the link is
	 * done automatically in the method create.
	 * </p>
	 */
	@Override
	public void create(ClientRow tableRow, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("INSERT INTO ");
		query.append(tableRow.tableName);

		query.append(" (");

		query.append(ClientRow.PSEUDO);
		query.append(", ");
		query.append(ClientRow.PASSWORD);
		query.append(", ");
		query.append(ClientRow.FIRST_NAME);
		query.append(", ");
		query.append(ClientRow.LAST_NAME);
		query.append(", ");
		query.append(ClientRow.MAIL);

		query.append(") VALUES (");

		query.append(DAO.quoteEncapsulation(tableRow.getPseudo()));
		query.append(", ");
		query.append(DAO.quoteEncapsulation(tableRow.getPassword()));
		query.append(", ");
		query.append(DAO.quoteEncapsulation(tableRow.getFirstName()));
		query.append(", ");
		query.append(DAO.quoteEncapsulation(tableRow.getLastName()));
		query.append(", ");
		query.append(DAO.quoteEncapsulation(tableRow.getMail()));
		query.append(")");

		Statement statement = null;
		ResultSet result = null;

		try {

			statement = SQLConnection.createStatement();

			statement.executeUpdate(query.toString(), Statement.RETURN_GENERATED_KEYS);

			result = statement.getGeneratedKeys();

			if (result.next())
				tableRow.setIdClient(result.getLong(1));
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
	public void update(ClientRow tableRow, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("UPDATE ");
		query.append(tableRow.tableName);
		query.append(" SET ");

		query.append(ClientRow.PSEUDO);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getPseudo()));
		query.append(", ");

		query.append(ClientRow.PASSWORD);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getPassword()));
		query.append(", ");

		query.append(ClientRow.FIRST_NAME);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getFirstName()));
		query.append(", ");

		query.append(ClientRow.LAST_NAME);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getLastName()));
		query.append(", ");

		query.append(ClientRow.MAIL);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(tableRow.getMail()));
		query.append(", ");

		query.append(ClientRow.ID_CLIENT_INFORMATION);
		query.append(" = ");
		query.append(DAO.formatIdForSQLQuery(tableRow.getIdClientInformation()));

		query.append(" WHERE ");

		query.append(ClientRow.ID_CLIENT);
		query.append(" = ");
		query.append(tableRow.getIdClient());

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
	public void delete(ClientRow tableRow, Connection SQLConnection, boolean commit) throws SQLException {
		StringBuilder query = new StringBuilder("DELETE FROM ");
		query.append(tableRow.tableName);
		query.append(" WHERE ");

		query.append(ClientRow.ID_CLIENT);
		query.append(" = ");
		query.append(tableRow.getIdClient());

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
		query.append(ClientRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(ClientRow.ID_CLIENT);
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
	public ClientRow find(long id, Connection SQLConnection) throws SQLException {
		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append(ClientRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(ClientRow.ID_CLIENT);
		query.append(" = ");
		query.append(id);

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			if (result.next()) {
				ClientRow client = new ClientRow();
				client.setIdClient(result.getLong(1));
				client.setPseudo(result.getString(2));
				client.setPassword(result.getString(3));
				client.setFirstName(result.getString(4));
				client.setLastName(result.getString(5));
				client.setMail(result.getString(6));
				client.setIdClientInformation(result.getLong(7));

				return client;
			}

			return null;
		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

	public ClientRow findByMail(String mail, Connection SQLConnection) throws SQLException {
		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append(ClientRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(ClientRow.MAIL);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(mail));

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			if (result.next()) {
				ClientRow client = new ClientRow();
				client.setIdClient(result.getLong(1));
				client.setPseudo(result.getString(2));
				client.setPassword(result.getString(3));
				client.setFirstName(result.getString(4));
				client.setLastName(result.getString(5));
				client.setMail(result.getString(6));
				client.setIdClientInformation(result.getLong(7));

				return client;
			}

			return null;

		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

	public ClientRow findByPseudo(String pseudo, Connection SQLConnection) throws SQLException {
		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append(ClientRow.TABLE_NAME);
		query.append(" WHERE ");

		query.append(ClientRow.PSEUDO);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(pseudo));

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			if (result.next()) {
				ClientRow client = new ClientRow();
				client.setIdClient(result.getLong(1));
				client.setPseudo(result.getString(2));
				client.setPassword(result.getString(3));
				client.setFirstName(result.getString(4));
				client.setLastName(result.getString(5));
				client.setMail(result.getString(6));
				client.setIdClientInformation(result.getLong(7));

				return client;
			}

			return null;

		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

	public List<ClientRow> getAll(Connection SQLConnection) throws SQLException {

		StringBuilder query = new StringBuilder("SELECT * FROM ");

		query.append(ClientRow.TABLE_NAME);

		Statement statement = null;
		ResultSet result = null;

		try {

			statement = SQLConnection.createStatement();

			result = statement.executeQuery(query.toString());

			List<ClientRow> listClient = new Vector<>(result.getFetchSize());

			while (result.next()) {
				ClientRow client = new ClientRow();

				client.setIdClient(result.getLong(1));
				client.setPseudo(result.getString(2));
				client.setPassword(result.getString(3));
				client.setFirstName(result.getString(4));
				client.setLastName(result.getString(5));
				client.setMail(result.getString(6));
				client.setIdClientInformation(result.getLong(7));

				listClient.add(client);
			}

			return listClient.isEmpty() ? null : listClient;

		} finally {
			if (statement != null)
				statement.close();

			if (result != null)
				result.close();
		}
	}

}
