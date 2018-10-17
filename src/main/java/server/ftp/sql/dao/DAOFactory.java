package server.ftp.sql.dao;

import server.ftp.sql.table_row.AuthenticationRow;
import server.ftp.sql.table_row.ClientInformationRow;
import server.ftp.sql.table_row.ClientRow;

public class DAOFactory {

	// Constructors.

	/**
	 * We can't instantiate this class.
	 */
	private DAOFactory() {
	}

	// Methods.

	public static DAO<ClientRow> newClientDAO() {
		return new ClientDAO();
	}

	public static DAO<ClientInformationRow> newClientInformationDAO() {
		return new ClientInformationDAO();
	}

	public static DAO<AuthenticationRow> newAuthenticationDAO() {
		return new AuthenticationDAO();
	}

}
