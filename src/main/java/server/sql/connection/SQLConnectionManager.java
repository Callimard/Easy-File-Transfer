package server.sql.connection;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import server.util.ErrorManager;

public class SQLConnectionManager {

	// Constant.

	private volatile static SQLConnectionManager SINGLE = new SQLConnectionManager();

	// Variables.

	private HikariConfig hikariConfig;
	private HikariDataSource hikariDataSource;

	// Constructors.

	private SQLConnectionManager() {
		this.hikariConfig = new HikariConfig();
		this.hikariConfig.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/server");
		this.hikariConfig.setUsername("server_user");
		this.hikariConfig.setPassword("server_user_password");
		this.hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		this.hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		this.hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		this.hikariConfig.addDataSourceProperty("maximumPoolSize", "4");
		this.hikariConfig.addDataSourceProperty("verifyServerCertificate", "true");
		this.hikariConfig.addDataSourceProperty("useSSL", "true");
		this.hikariDataSource = new HikariDataSource(this.hikariConfig);
	}

	// Public methods.

	public static Connection getConnection() {
		try {
			Connection connection = SINGLE.hikariDataSource.getConnection();
			connection.setAutoCommit(false);
			return connection;
		} catch (SQLException e) {
			ErrorManager.writeError(e);
			return null;
		}
	}

	public static void evictConnection(Connection connection) {
		if (connection != null)
			SINGLE.hikariDataSource.evictConnection(connection);
	}
}
