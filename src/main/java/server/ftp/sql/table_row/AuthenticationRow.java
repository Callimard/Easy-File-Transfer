package server.ftp.sql.table_row;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import server.ftp.sql.dao.DAO;
import server.sql.table_row.TableRow;

public class AuthenticationRow extends TableRow {

	// Constants.

	public static final String TABLE_NAME = "Authentication";

	public static final int CLIENT_IP_MAX_SIZE = 15;

	public static final String ID_AUTHENTICATION = "id_authentication";
	public static final String ID_CLIENT = "id_client";
	public static final String AUTHENTICATION_DATE = "authentication_date";
	public static final String LAST_QUERY_DATE = "last_query_date";
	public static final String CLIENT_IP = "client_ip";

	/**
	 * 0b0000_0000_0001
	 */
	public static final int FLAG_ID_AUTHENTICATION = 0b0000_0000_0001;

	/**
	 * 0b0000_0000_0010
	 */
	public static final int FLAG_ID_CLIENT = 0b0000_0000_0010;

	/**
	 * 0b0000_0000_0100
	 */
	public static final int FLAG_AUTHENTICATION_DATE = 0b0000_0000_0100;

	/**
	 * 0b0000_0000_1000
	 */
	public static final int FLAG_LAST_QUERY_DATE = 0b0000_0000_1000;

	/**
	 * 0b0000_0001_0000
	 */
	public static final int FLAG_CLIENT_IP = 0b0000_0001_0000;

	// Variables.

	private Long idAuthentication;
	private Long idClient;
	private Date authenticationDate;
	private Date lastQueryDate;
	private String clientIp;

	// Constructors.

	public AuthenticationRow(Long idAuthentication, Long idClient, Date authenticationDate, Date lastQueryDate,
			String clientIp) {
		this();
		this.idAuthentication = idAuthentication;
		this.idClient = idClient;
		this.authenticationDate = authenticationDate;
		this.lastQueryDate = lastQueryDate;
		this.clientIp = clientIp;
		
		this.reformAll();
	}

	public AuthenticationRow() {
		super(TABLE_NAME, 5, 1);
	}

	// Methods.

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null)
			return false;
		if (object instanceof AuthenticationRow) {
			AuthenticationRow a = (AuthenticationRow) object;

			if (a.idAuthentication == this.idAuthentication)
				return true;
			else
				return false;
		} else
			return false;
	}

	@Override
	public AuthenticationRow clone() {
		return new AuthenticationRow(new Long(this.idAuthentication), new Long(this.idClient),
				this.authenticationDate == null ? null : (Date) this.authenticationDate.clone(),
				this.lastQueryDate == null ? null : (Date) this.lastQueryDate.clone(), new String(this.clientIp));
	}

	@Override
	public boolean isReadyToBeCreated() {
		return this.idClient != null && this.idClient > 0 && this.authenticationDate != null
				&& verifyClientIp(this.clientIp);
	}

	@Override
	public boolean isReadyToBeUpdated() {
		return this.idAuthentication != null && this.idAuthentication > 0 && this.idClient != null && this.idClient > 0
				&& this.authenticationDate != null && verifyClientIp(this.clientIp);
	}

	@Override
	public boolean isReadyToBeDeleted() {
		return this.idAuthentication != null && this.idAuthentication > 0;
	}

	@Override
	protected int[] initialiazeTabColumnFlag() {
		return new int[] { FLAG_ID_AUTHENTICATION, FLAG_ID_CLIENT, FLAG_AUTHENTICATION_DATE, FLAG_LAST_QUERY_DATE,
				FLAG_CLIENT_IP };
	}

	@Override
	protected void refineArgument() {
		this.idAuthentication = this.idAuthentication == null || this.idAuthentication <= 0 ? new Long(-1)
				: this.idAuthentication;
		this.idClient = this.idClient == null || this.idClient <= 0 ? new Long(-1) : this.idClient;
		// No problem for authenticationDate and lastQueryDate.
		this.clientIp = this.clientIp == null ? new String("") : this.clientIp;
	}

	@Override
	protected void updateCompletedColumnFlag() {
		this.completedColumnFlag = 0;

		if (this.idAuthentication != null && this.idAuthentication >= 0)
			this.completedColumnFlag |= FLAG_ID_AUTHENTICATION;

		if (this.idClient != null && this.idClient >= 0)
			this.completedColumnFlag |= FLAG_ID_CLIENT;

		if (this.authenticationDate != null)
			this.completedColumnFlag |= FLAG_AUTHENTICATION_DATE;

		if (this.lastQueryDate != null)
			this.completedColumnFlag |= FLAG_LAST_QUERY_DATE;

		if (this.clientIp != null && !this.clientIp.isEmpty())
			this.completedColumnFlag |= FLAG_CLIENT_IP;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[" + TABLE_NAME + " : ");

		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_TIME_FORMAT);
		
		builder.append(this.idAuthentication);
		builder.append(", ");
		builder.append(this.idClient);
		builder.append(", ");
		builder.append(dateFormat.format(this.authenticationDate));
		builder.append(", ");
		builder.append(this.lastQueryDate != null ? dateFormat.format(this.lastQueryDate) : null);
		builder.append(", ");
		builder.append(this.clientIp);
		builder.append("]");

		return builder.toString();
	}

	public static boolean verifyClientIp(String clientIp) {
		// TODO make the regex verification.
		return clientIp != null && !clientIp.isEmpty() && clientIp.length() <= CLIENT_IP_MAX_SIZE;
	}

	// Getters and Setters.

	public Long getIdAuthentication() {
		return idAuthentication;
	}

	public void setIdAuthentication(Long idAuthentication) {
		this.idAuthentication = idAuthentication;
		this.reformAll();
	}

	public Long getIdClient() {
		return idClient;
	}

	public void setIdClient(Long idClient) {
		this.idClient = idClient;
		this.reformAll();
	}

	public Date getAuthenticationDate() {
		return authenticationDate;
	}

	public void setAuthenticationDate(Date authenticationDate) {
		this.authenticationDate = authenticationDate;
		this.reformAll();
	}

	public Date getLastQueryDate() {
		return lastQueryDate;
	}

	public void setLastQueryDate(Date lastQueryDate) {
		this.lastQueryDate = lastQueryDate;
		this.reformAll();
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
		this.reformAll();
	}
}
