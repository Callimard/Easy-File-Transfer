package server.ftp.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import server.ftp.exception.authentication.DeauthenticatorAlreaydyStartedException;
import server.ftp.exception.authentication.DeauthenticatorAlreaydyStoppedException;
import server.ftp.sql.table_row.AuthenticationRow;
import server.sql.connection.SQLConnectionManager;
import server.util.ErrorManager;

public class FTPDeauthenticator {

	// Constants.

	public static final int DEFAULT_TIMER = 30;
	public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTE;

	// Variables.

	private FTPServerController ftpServerController;

	private FTPAuthenticationManager authenticationManager = FTPAuthenticationManager.getInstance();

	private int timerValue = DEFAULT_TIMER;
	private TimeUnit timeUnit = DEFAULT_TIME_UNIT;

	private boolean isStopped = true;

	private Thread thread;

	// Constructors.

	public FTPDeauthenticator(FTPServerController ftpServerController) {
		this(DEFAULT_TIMER, DEFAULT_TIME_UNIT, ftpServerController);
	}

	public FTPDeauthenticator(int timerValue, TimeUnit enumTimeUnit, FTPServerController ftpServerController) {

		this.ftpServerController = ftpServerController;

		if (timerValue > 0)
			this.timerValue = timerValue;

		if (enumTimeUnit != null)
			this.timeUnit = enumTimeUnit;
	}

	// Methods.

	private void action() {
		while (!this.isStopped) {

			Connection SQLConnection1 = SQLConnectionManager.getConnection();

			Statement statement1 = null;

			ResultSet result = null;

			try {
				// All 30 sec
				Thread.sleep(30_000);

				StringBuilder query = null;

				query = new StringBuilder("SELECT ");
				query.append(AuthenticationRow.ID_AUTHENTICATION);
				query.append(" FROM ");
				query.append(AuthenticationRow.TABLE_NAME);
				query.append(" WHERE DATE_SUB(NOW(), INTERVAL ");
				query.append(this.timerValue);
				query.append(" ");
				query.append(this.timeUnit);
				query.append(") > ");
				query.append(AuthenticationRow.LAST_QUERY_DATE);

				// System.out.println("Query = " + query);

				statement1 = SQLConnection1.createStatement();

				result = statement1.executeQuery(query.toString());

				List<Long> listKey = new ArrayList<>(result.getFetchSize());

				while (result.next()) {
					listKey.add(result.getLong(1));
				}

				if (!listKey.isEmpty()) {
					// for (Long key : listKey) {
					// try {
					// this.authenticationManager.deauthentication(key);
					// } catch (FailToDeauthenticateException e) {
					// ErrorManager.writeError(e);
					// }
					// }

					List<FTPClientConnection> listFTPClientConnection = this.authenticationManager
							.getListClientConnection(listKey);

					for (FTPClientConnection ftpClientConnection : listFTPClientConnection) {
						this.ftpServerController.getClientManager().preparekillClient(ftpClientConnection);
					}

				}

				SQLConnection1.commit();

			} catch (InterruptedException e) {
				// We stop the thread.
			} catch (SQLException e) {
				ErrorManager.writeError(e);

				if (SQLConnection1 != null)
					try {
						SQLConnection1.rollback();
					} catch (SQLException e1) {
						ErrorManager.writeError(new SQLException("FAIL ROLLBACK!!!!!", e1));
					}

			} finally {
				try {

					if (statement1 != null)
						statement1.close();

					if (result != null)
						result.close();

				} catch (SQLException e) {
					ErrorManager.writeError(e);
				}

				SQLConnectionManager.evictConnection(SQLConnection1);
			}

		}
	}

	public synchronized void start() throws DeauthenticatorAlreaydyStartedException {
		if (this.isStopped) {
			this.isStopped = false;
			this.thread = new Thread(() -> {
				this.action();
			});

			this.thread.start();
		} else {
			throw new DeauthenticatorAlreaydyStartedException();
		}
	}

	public synchronized void stop() throws DeauthenticatorAlreaydyStoppedException {
		if (!this.isStopped) {
			this.isStopped = true;
			this.thread.interrupt();
		} else {
			throw new DeauthenticatorAlreaydyStoppedException();
		}
	}

	// Getters and Setters.

	public boolean isStopped() {
		return this.isStopped;
	}

	public FTPServerController getFTPServerController() {
		return this.ftpServerController;
	}

	// Enumeration.

	public enum TimeUnit {
		SECOND, MINUTE, HOUR, DAY, MONTH, YEAR;
	}
}
