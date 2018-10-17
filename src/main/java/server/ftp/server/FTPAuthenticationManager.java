package server.ftp.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import server.ftp.exception.authentication.AlreadyAuthenticatedClientException;
import server.ftp.exception.authentication.AlreadyPreparedException;
import server.ftp.exception.authentication.AuthenticationFailedException;
import server.ftp.exception.authentication.FailToDeauthenticateException;
import server.ftp.exception.authentication.FailToUpdateAuthenticationException;
import server.ftp.exception.authentication.NotAuthenticatedClientException;
import server.ftp.exception.authentication.NotPreparedException;
import server.ftp.exception.authentication.PrepareAuthenticationFailException;
import server.ftp.exception.authentication.WrongPasswordException;
import server.ftp.exception.authentication.WrongPseudoException;
import server.ftp.exception.authentication.WrongPseudoFormatException;
import server.ftp.manipulator.ClientManipulator;
import server.ftp.manipulator.ClientManipulator.TypeEntry;
import server.ftp.sql.dao.AuthenticationDAO;
import server.ftp.sql.dao.ClientDAO;
import server.ftp.sql.dao.DAO;
import server.ftp.sql.dao.DAOFactory;
import server.ftp.sql.table_row.AuthenticationRow;
import server.ftp.sql.table_row.ClientRow;
import server.sql.connection.SQLConnectionManager;
import server.util.ErrorManager;
import server.util.exception.ManipulatorException;

public class FTPAuthenticationManager {

	// Constants.

	private static final FTPAuthenticationManager AUTHENTICATION_MANAGER = new FTPAuthenticationManager();

	private static final int DEFAULT_CAPACITY = 15;

	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private static final int DEFAULT_CONCURRENCY_LEVEL = 5;

	// Variables.

	private final ConcurrentHashMap<FTPClientConnection, String> hashClientConnectionPseudo = new ConcurrentHashMap<>(
			DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);

	/**
	 * Link Authentication ID and ClientConnection.
	 */
	private final ConcurrentHashMap<FTPClientConnection, Long> hashClientConnectionID = new ConcurrentHashMap<>(
			DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);

	private AuthenticationDAO authenticationDAO = (AuthenticationDAO) DAOFactory.newAuthenticationDAO();

	private ClientDAO clientDAO = (ClientDAO) DAOFactory.newClientDAO();

	// Constructors.

	private FTPAuthenticationManager() {
	}

	// Methods.

	public static FTPAuthenticationManager getInstance() {
		return AUTHENTICATION_MANAGER;
	}

	public boolean isAlreadyPrepare(FTPClientConnection ftpClientConnection) {
		return this.hashClientConnectionPseudo.get(ftpClientConnection) != null;
	}

	public boolean prepareAuthentication(FTPClientConnection ftpClientConnection, String pseudo)
			throws AlreadyPreparedException, WrongPseudoFormatException, PrepareAuthenticationFailException,
			WrongPseudoException {

		if (!this.isAlreadyPrepare(ftpClientConnection)) {

			if (pseudo != null && !pseudo.isEmpty()) {

				Connection SQLConnection = SQLConnectionManager.getConnection();

				try {
					ClientRow client = this.clientDAO.findByPseudo(pseudo, SQLConnection);

					if (client != null) {

						this.hashClientConnectionPseudo.put(ftpClientConnection, pseudo);

						return true;

					} else {
						throw new WrongPseudoException();
					}

				} catch (SQLException e) {

					throw new PrepareAuthenticationFailException(e);

				} finally {
					SQLConnectionManager.evictConnection(SQLConnection);
				}
			} else {
				throw new WrongPseudoFormatException("Pseudo = " + pseudo);
			}

		} else {
			throw new AlreadyPreparedException();
		}
	}

	public String getPseudo(FTPClientConnection ftpClientConnection) {
		return this.hashClientConnectionPseudo.get(ftpClientConnection);
	}

	public boolean finalizeAuthentication(FTPClientConnection ftpClientConnection, String password)
			throws NotPreparedException, AlreadyAuthenticatedClientException, WrongPasswordException,
			AuthenticationFailedException {

		if (this.isAlreadyPrepare(ftpClientConnection)) {

			String pseudo = this.hashClientConnectionPseudo.get(ftpClientConnection);

			return this.tryAuthentication(ftpClientConnection, pseudo, password);

		} else {
			throw new NotPreparedException();
		}
	}

	/**
	 * 
	 * Search in the hash map if the ip address of the clientConnection is a ip
	 * authenticated.
	 * 
	 * @param clientConnection
	 * @return the key find if the clientConnection is already authenticated else
	 *         null.
	 */
	public boolean isAlreadyAuthenticated(FTPClientConnection clientConnection) {
		return this.hashClientConnectionID.get(clientConnection) != null;
	}

	public Long getKey(FTPClientConnection ftpClientConnection) {
		return this.hashClientConnectionID.get(ftpClientConnection);
	}

	public FTPClientConnection getClientConnection(Long id) {
		for (Entry<FTPClientConnection, Long> entry : this.hashClientConnectionID.entrySet()) {

			if (id == entry.getValue()) {
				return entry.getKey();
			}
		}

		return null;

	}

	public List<FTPClientConnection> getListClientConnection(List<Long> listID) {

		List<FTPClientConnection> listFTPClientConnection = new Vector<>(listID.size());

		for (long key : listID) {
			listFTPClientConnection.add(this.getClientConnection(key));
		}

		return listFTPClientConnection;
	}

	public boolean isAlreadyAuthenticated(Long id) {

		for (Entry<FTPClientConnection, Long> entry : this.hashClientConnectionID.entrySet()) {

			if (id == entry.getValue()) {
				return true;
			}
		}

		return false;
	}

	public boolean tryAuthentication(FTPClientConnection ftpClientConnection, String pseudo, String password)
			throws AlreadyAuthenticatedClientException, WrongPasswordException, AuthenticationFailedException {
		try {
			return this.tryAuthentication(ftpClientConnection, password,
					new ClientManipulator(pseudo, TypeEntry.PSEUDO));
		} catch (ManipulatorException e) {
			throw new AuthenticationFailedException(e);
		}
	}

	public boolean tryAuthentication(FTPClientConnection ftpClientConnection, String password,
			ClientManipulator clientManipulator)
			throws AlreadyAuthenticatedClientException, WrongPasswordException, AuthenticationFailedException {

		if (!this.isAlreadyAuthenticated(ftpClientConnection)) {

			if (clientManipulator.getPassword().equals(password)) {

				AuthenticationRow authenticationRow = new AuthenticationRow(null, clientManipulator.getIdClient(),
						new Date(), null, ftpClientConnection.getHostAddress());

				Connection SQLConnection = SQLConnectionManager.getConnection();

				try {
					this.authenticationDAO.create(authenticationRow, SQLConnection, true);

					this.hashClientConnectionID.put(ftpClientConnection, authenticationRow.getIdAuthentication());

					this.updateQueryDoneNoAuthenticationVerification(ftpClientConnection);

					return true;

				} catch (SQLException e) {

					try {
						SQLConnection.rollback();
					} catch (SQLException e1) {
						SQLException e2 = new SQLException("FAIL TO ROLLBACK!!", e);
						throw new AuthenticationFailedException(e2);
					}

					throw new AuthenticationFailedException(e);
				} catch (FailToUpdateAuthenticationException e) {

					throw new AuthenticationFailedException(e);
				} finally {
					SQLConnectionManager.evictConnection(SQLConnection);
				}

			} else {
				throw new WrongPasswordException();
			}

		} else {
			throw new AlreadyAuthenticatedClientException();
		}
	}

	public void updateQueryDoneNoAuthenticationVerification(FTPClientConnection ftpClientConnection)
			throws FailToUpdateAuthenticationException {
		Long key = this.getKey(ftpClientConnection);

		StringBuilder query = new StringBuilder("UPDATE ");

		query.append(AuthenticationRow.TABLE_NAME);
		query.append(" SET ");
		query.append(AuthenticationRow.LAST_QUERY_DATE);
		query.append(" = ");
		query.append(DAO.toDateTimeSQLString(new Date()));
		query.append(" WHERE ");
		query.append(AuthenticationRow.ID_AUTHENTICATION);
		query.append(" = ");
		query.append(DAO.quoteEncapsulation(key));

		Connection SQLConnection = SQLConnectionManager.getConnection();

		Statement statement = null;

		try {

			statement = SQLConnection.createStatement();

			statement.executeUpdate(query.toString());

			SQLConnection.commit();

		} catch (SQLException e) {

			try {
				SQLConnection.rollback();
			} catch (SQLException e1) {
				SQLException e2 = new SQLException("FAIL TO ROLLBACK!!", e);
				throw new FailToUpdateAuthenticationException(e2);
			}

			throw new FailToUpdateAuthenticationException(e);
		} finally {
			try {

				if (statement != null)
					statement.close();

			} catch (SQLException e) {
				ErrorManager.writeError(e);
			}

			SQLConnectionManager.evictConnection(SQLConnection);
		}
	}

	public void updateQueryDone(FTPClientConnection ftpClientConnection)
			throws NotAuthenticatedClientException, FailToUpdateAuthenticationException {

		Long key = null;

		if ((key = this.getKey(ftpClientConnection)) != null) {

			StringBuilder query = new StringBuilder("UPDATE ");

			query.append(AuthenticationRow.TABLE_NAME);
			query.append(" SET ");
			query.append(AuthenticationRow.LAST_QUERY_DATE);
			query.append(" = ");
			query.append(DAO.toDateTimeSQLString(new Date()));
			query.append(" WHERE ");
			query.append(AuthenticationRow.ID_AUTHENTICATION);
			query.append(" = ");
			query.append(DAO.quoteEncapsulation(key));

			Connection SQLConnection = SQLConnectionManager.getConnection();

			Statement statement = null;

			try {

				statement = SQLConnection.createStatement();

				statement.executeUpdate(query.toString());

				SQLConnection.commit();

			} catch (SQLException e) {

				try {
					SQLConnection.rollback();
				} catch (SQLException e1) {
					SQLException e2 = new SQLException("FAIL TO ROLLBACK!!", e);
					throw new FailToUpdateAuthenticationException(e2);
				}

				throw new FailToUpdateAuthenticationException(e);
			} finally {
				try {

					if (statement != null)
						statement.close();

				} catch (SQLException e) {
					ErrorManager.writeError(e);
				}

				SQLConnectionManager.evictConnection(SQLConnection);
			}

		} else {
			throw new NotAuthenticatedClientException();
		}
	}

	public void deauthentication(FTPClientConnection ftpClientConnection) throws FailToDeauthenticateException {

		Long key = this.hashClientConnectionID.remove(ftpClientConnection);

		String pseudo = this.hashClientConnectionPseudo.remove(ftpClientConnection);

		if (key != null) {

			Connection SQLConnection = SQLConnectionManager.getConnection();

			try {

				this.authenticationDAO.delete(key, SQLConnection, true);

			} catch (SQLException e) {
				// We abort the authentication.

				try {
					SQLConnection.rollback();
				} catch (SQLException e1) {
					SQLException e2 = new SQLException("BIG PROBLEME; FAIL ROLLBACK", e);
					throw new FailToDeauthenticateException(e2);
				}

				this.hashClientConnectionID.put(ftpClientConnection, key);
				this.hashClientConnectionPseudo.put(ftpClientConnection, pseudo);
				throw new FailToDeauthenticateException(e);

			} finally {

				SQLConnectionManager.evictConnection(SQLConnection);
			}
		}
	}

	public void deauthentication(Long key) throws FailToDeauthenticateException {

		FTPClientConnection clientConnection = this.getClientConnection(key);

		if (clientConnection != null) {

			this.hashClientConnectionID.remove(clientConnection);

			String pseudo = this.hashClientConnectionPseudo.remove(clientConnection);

			Connection SQLConnection = SQLConnectionManager.getConnection();

			try {

				this.authenticationDAO.delete(key, SQLConnection, true);

			} catch (SQLException e) {
				// We abort the authentication.

				try {
					SQLConnection.rollback();
				} catch (SQLException e1) {
					SQLException e2 = new SQLException("BIG PROBLEME; FAIL ROLLBACK", e);
					throw new FailToDeauthenticateException(e2);
				}

				this.hashClientConnectionID.put(clientConnection, key);
				this.hashClientConnectionPseudo.put(clientConnection, pseudo);
				throw new FailToDeauthenticateException(e);

			} finally {

				SQLConnectionManager.evictConnection(SQLConnection);
			}
		}
	}
}
