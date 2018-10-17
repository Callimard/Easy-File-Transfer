package server.ftp.sql.table_row;

import server.sql.table_row.TableRow;

public class ClientRow extends TableRow {

	// Constants.

	public static final String TABLE_NAME = "Client";

	public static final int PSEUDO_MAX_SIZE = 17;
	public static final int PASSWORD_MAX_SIZE = 45;
	public static final int FIRST_NAME_MAX_SIZE = 23;
	public static final int LAST_NAME_MAX_SIZE = 30;
	public static final int MAIL_MAX_SIZE = 55;

	public static final String ID_CLIENT = "id_client";
	public static final String PSEUDO = "pseudo";
	public static final String PASSWORD = "password";
	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String MAIL = "mail";
	public static final String ID_CLIENT_INFORMATION = "id_client_information";

	/**
	 * 0b0000_0000_0001
	 */
	public static final int FLAG_ID_CLIENT = 0b0000_0000_0001;

	/**
	 * 0b0000_0000_0010
	 */
	public static final int FLAG_PSEUDO = 0b0000_0000_0010;

	/**
	 * 0b0000_0000_0100
	 */
	public static final int FLAG_PASSWORD = 0b0000_0000_0100;

	/**
	 * 0b0000_0000_1000
	 */
	public static final int FLAG_FIRST_NAME = 0b0000_0000_1000;

	/**
	 * 0b0000_0001_0000
	 */
	public static final int FLAG_LAST_NAME = 0b0000_0001_0000;

	/**
	 * 0b0000_0010_0000
	 */
	public static final int FLAG_MAIL = 0b0000_0010_0000;

	/**
	 * 0b0000_0100_0000
	 */
	public static final int FLAG_ID_CLIENT_INFORMATION = 0b0000_0100_0000;

	// Variables.

	private Long idClient;
	private String pseudo;
	private String password;
	private String firstName;
	private String lastName;
	private String mail;
	private Long idClientInformation;

	// Constructors.

	public ClientRow(Long idClient, String pseudo, String password, String firstName, String lastName, String mail,
			Long idClientInformation) {
		this();
		this.idClient = idClient;
		this.pseudo = pseudo;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.mail = mail;
		this.idClientInformation = idClientInformation;

		this.reformAll();
	}

	public ClientRow() {
		super(TABLE_NAME, 5, 1);
	}

	// Methods.

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null)
			return false;
		if (object instanceof ClientRow) {
			ClientRow c = (ClientRow) object;

			if (c.idClient == this.idClient)
				return true;
			else
				return false;
		} else {
			return false;
		}
	}

	@Override
	public ClientRow clone() {
		return new ClientRow(new Long(this.idClient), new String(this.pseudo), new String(this.password),
				new String(this.firstName), new String(this.lastName), new String(this.mail),
				new Long(this.idClientInformation));
	}

	@Override
	public boolean isReadyToBeCreated() {
		return verifyPseudo(this.pseudo) && verifyPassword(this.password) && verifyFirstName(this.firstName)
				&& verifyLastName(this.lastName) && verifyMail(this.mail);
	}

	@Override
	public boolean isReadyToBeUpdated() {
		return this.idClient != null && this.idClient > 0 && verifyPseudo(this.pseudo) && verifyPassword(this.password)
				&& verifyFirstName(this.firstName) && verifyLastName(this.lastName) && verifyMail(this.mail);
	}

	@Override
	public boolean isReadyToBeDeleted() {
		return this.idClient != null && this.idClient > 0;
	}

	@Override
	protected int[] initialiazeTabColumnFlag() {
		return new int[] { FLAG_ID_CLIENT, FLAG_PASSWORD, FLAG_FIRST_NAME, FLAG_LAST_NAME, FLAG_MAIL,
				FLAG_ID_CLIENT_INFORMATION };
	};

	@Override
	protected void refineArgument() {
		this.idClient = this.idClient == null || this.idClient <= 0 ? new Long(-1) : this.idClient;
		this.pseudo = this.pseudo == null ? new String("") : this.pseudo;
		this.password = this.password == null ? new String("") : this.password;
		this.firstName = this.firstName == null ? new String("") : this.firstName;
		this.lastName = this.lastName == null ? new String("") : this.lastName;
		this.mail = this.mail == null ? new String("") : this.mail;
		this.idClientInformation = this.idClientInformation == null || this.idClientInformation <= 0 ? new Long(-1)
				: this.idClientInformation;
	}

	@Override
	protected void updateCompletedColumnFlag() {
		this.completedColumnFlag = 0;

		if (this.idClient != null && this.idClient > 0)
			this.completedColumnFlag |= FLAG_ID_CLIENT;

		if (this.pseudo != null && !this.pseudo.isEmpty())
			this.completedColumnFlag |= FLAG_PSEUDO;

		if (this.password != null && !this.password.isEmpty())
			this.completedColumnFlag |= FLAG_PASSWORD;

		if (this.firstName != null && !this.firstName.isEmpty())
			this.completedColumnFlag |= FLAG_FIRST_NAME;

		if (this.lastName != null && !this.lastName.isEmpty())
			this.completedColumnFlag |= FLAG_LAST_NAME;

		if (this.mail != null && !this.mail.isEmpty())
			this.completedColumnFlag |= FLAG_MAIL;

		if (this.idClientInformation != null && this.idClientInformation > 0)
			this.completedColumnFlag |= FLAG_ID_CLIENT_INFORMATION;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[" + TABLE_NAME + " : ");

		builder.append(this.idClient);
		builder.append(", ");
		builder.append(this.pseudo);
		builder.append(", ");
		builder.append(this.password);
		builder.append(", ");
		builder.append(this.firstName);
		builder.append(", ");
		builder.append(this.lastName);
		builder.append(", ");
		builder.append(this.mail);
		builder.append(", ");
		builder.append(this.idClientInformation);
		builder.append("]");

		return builder.toString();
	}

	/**
	 * <p>
	 * <strong>The pseudo can't be null.</strong>
	 * </p>
	 * 
	 * <p>
	 * It should be not empty and is size must be less or equal to
	 * {@link ClientRow#PSEUDO_MAX_SIZE}
	 * </p>
	 * 
	 * @param pseudo
	 * @return true if the pseudo can be put in the DB like this.
	 */
	public static boolean verifyPseudo(String pseudo) {
		// TODO regex verification.
		return pseudo != null && !pseudo.isEmpty() && pseudo.length() <= PSEUDO_MAX_SIZE;
	}

	/**
	 * <p>
	 * <strong>The password can't be null.</strong>
	 * </p>
	 * 
	 * <p>
	 * It should be not empty and is size must be less or equal to
	 * {@link ClientRow#PASSWORD_MAX_SIZE}
	 * </p>
	 * 
	 * @param password
	 * @return true if the password can be put in the DB like this.
	 */
	public static boolean verifyPassword(String password) {
		// TODO regex verification.
		return password != null && !password.isEmpty() && password.length() <= PASSWORD_MAX_SIZE;
	}

	/**
	 * <p>
	 * <strong>The firstName can't be null.</strong>
	 * </p>
	 * <p>
	 * It should be not empty and is size must be less or equal to
	 * {@link ClientRow#FIRST_NAME_MAX_SIZE}
	 * </p>
	 * 
	 * @param firstName
	 * @return true if the firstName can be put in the DB like this.
	 */
	public static boolean verifyFirstName(String firstName) {
		return firstName != null && !firstName.isEmpty() && firstName.length() <= FIRST_NAME_MAX_SIZE;
	}

	/**
	 * <p>
	 * <strong>The lastName can't be null.</strong>
	 * </p>
	 * <p>
	 * It should be not empty and is size must be less or equal to
	 * {@link ClientRow#LAST_NAME_MAX_SIZE}
	 * </p>
	 * 
	 * @param lastName
	 * @return true if the lastName can be put in the DB like this.
	 */
	public static boolean verifyLastName(String lastName) {
		return lastName != null && !lastName.isEmpty() && lastName.length() <= LAST_NAME_MAX_SIZE;
	}

	/**
	 * <p>
	 * <strong>The mail can't be null.</strong>
	 * </p>
	 * <p>
	 * It should be not empty and is size must be less or equal to
	 * {@link ClientRow#MAIL_MAX_SIZE}
	 * </p>
	 * 
	 * @param mail
	 * @return true if the mail can be put in the DB like this.
	 */
	public static boolean verifyMail(String mail) {
		// TODO make the regex verification.
		return mail != null && !mail.isEmpty() && mail.length() <= MAIL_MAX_SIZE;
	}

	// Getters and Setters.

	public Long getIdClient() {
		return idClient;
	}

	public void setIdClient(Long idClient) {
		this.idClient = idClient;
		this.reformAll();
	}

	public String getPseudo() {
		return this.pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
		this.reformAll();
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
		this.reformAll();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
		this.reformAll();
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
		this.reformAll();
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
		this.reformAll();
	}

	public Long getIdClientInformation() {
		return idClientInformation;
	}

	public void setIdClientInformation(Long idClientInformation) {
		this.idClientInformation = idClientInformation;
		this.reformAll();
	}

}
