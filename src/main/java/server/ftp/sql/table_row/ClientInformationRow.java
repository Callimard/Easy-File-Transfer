package server.ftp.sql.table_row;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import server.ftp.sql.dao.DAO;
import server.sql.table_row.TableRow;
import server.util.ErrorManager;

public class ClientInformationRow extends TableRow {

	// Constants.

	public static final String TABLE_NAME = "ClientInformation";

	public static final int ADDRESS_MAX_SIZE = 45;
	public static final int CITY_MAX_SIZE = 25;
	public static final int COUNTRY_MAX_SIZE = 15;

	public static final String ID_CLIENT_INFORMATION = "id_client_information";
	public static final String ID_CLIENT = "id_client";
	public static final String TEL = "tel";
	public static final String ADDRESS = "address";
	public static final String CITY = "city";
	public static final String COUNTRY = "country";
	public static final String BIRTHDAY = "birthday";

	/**
	 * 0b0000_0000_0001
	 */
	public static final int FLAG_ID_CLIENT_INFORMATION = 0b0000_0000_0001;

	/**
	 * 0b0000_0000_0010
	 */
	public static final int FLAG_ID_CLIENT = 0b0000_0000_0010;

	/**
	 * 0b0000_0000_0100
	 */
	public static final int FLAG_TEL = 0b0000_0000_0100;

	/**
	 * 0b0000_0000_1000
	 */
	public static final int FLAG_ADDRESS = 0b0000_0000_1000;

	/**
	 * 0b0000_0001_0000
	 */
	public static final int FLAG_CITY = 0b0000_0001_0000;

	/**
	 * 0b0000_0010_0000
	 */
	public static final int FLAG_COUNTRY = 0b0000_0010_0000;

	/**
	 * 0b0000_0100_0000
	 */
	public static final int FLAG_BIRTHDAY = 0b0000_0100_0000;

	// Variables.

	private Long idClientInformation;
	private Long idClient;
	private String tel;
	private String address;
	private String city;
	private String country;
	private Date birthday;

	// Constructors.

	public ClientInformationRow(Long idClientInformation, Long idClient, String tel, String address, String city,
			String country, int year, int month, int day) {
		this();
		this.idClientInformation = idClientInformation;
		this.idClient = idClient;
		this.tel = tel;
		this.address = address;
		this.city = city;
		this.country = country;

		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_FORMAT);

		StringBuilder builder = new StringBuilder();

		builder.append(year);
		builder.append("-");
		builder.append(month);
		builder.append("-");
		builder.append(day);

		try {
			this.birthday = dateFormat.parse(builder.toString());
		} catch (ParseException e) {
			ErrorManager.writeError(e);
			this.birthday = null;
		}

		this.reformAll();
	}

	public ClientInformationRow(Long idClientInformation, Long idClient, String tel, String address, String city,
			String country, Date birthday) {
		this();
		this.idClientInformation = idClientInformation;
		this.idClient = idClient;
		this.tel = tel;
		this.address = address;
		this.city = city;
		this.country = country;
		this.birthday = birthday;

		this.reformAll();
	}

	public ClientInformationRow() {
		super(TABLE_NAME, 7, 1);
	}

	// Methods.

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null)
			return false;
		if (object instanceof ClientInformationRow) {
			ClientInformationRow cI = (ClientInformationRow) object;

			if (cI.idClientInformation == this.idClientInformation)
				return true;
			else
				return false;
		} else
			return false;
	}

	@Override
	public ClientInformationRow clone() {
		return new ClientInformationRow(new Long(this.idClientInformation), new Long(this.idClient),
				new String(this.tel), new String(this.address), new String(this.city), new String(this.country),
				this.birthday == null ? null : (Date) this.birthday.clone());
	}

	@Override
	public boolean isReadyToBeCreated() {
		return this.idClient != null && this.idClient > 0 && verifyTel(this.tel) && verifyAddress(this.address)
				&& verifyCity(this.city) && verifyCountry(this.country);
	}

	@Override
	public boolean isReadyToBeUpdated() {
		return this.idClientInformation != null && this.idClientInformation > 0 && this.idClient != null
				&& this.idClient > 0 && verifyTel(this.tel) && verifyAddress(this.address) && verifyCity(this.city)
				&& verifyCountry(this.country);
	}

	@Override
	public boolean isReadyToBeDeleted() {
		return this.idClientInformation != null && this.idClientInformation > 0;
	}

	@Override
	protected int[] initialiazeTabColumnFlag() {
		return new int[] { FLAG_ID_CLIENT_INFORMATION, FLAG_ID_CLIENT, FLAG_TEL, FLAG_ADDRESS, FLAG_CITY, FLAG_COUNTRY,
				FLAG_BIRTHDAY };
	}

	@Override
	protected void refineArgument() {
		this.idClientInformation = this.idClientInformation == null || this.idClientInformation <= 0 ? new Long(-1)
				: this.idClientInformation;
		this.idClient = this.idClient == null || this.idClient <= 0 ? new Long(-1) : this.idClient;
		this.tel = this.tel == null ? new String("") : this.tel;
		this.address = this.address == null ? new String("") : this.address;
		this.city = this.city == null ? new String("") : this.city;
		this.country = this.country == null ? new String("") : this.country;
		// The birthday can be null or not is not a problem.
	}

	@Override
	protected void updateCompletedColumnFlag() {
		this.completedColumnFlag = 0;

		if (this.idClientInformation != null && this.idClientInformation >= 0)
			this.completedColumnFlag |= FLAG_ID_CLIENT_INFORMATION;

		if (this.idClient != null && this.idClient >= 0)
			this.completedColumnFlag |= FLAG_ID_CLIENT;

		if (this.tel != null && !this.tel.isEmpty())
			this.completedColumnFlag |= FLAG_TEL;

		if (this.address != null && !this.address.isEmpty())
			this.completedColumnFlag |= FLAG_ADDRESS;

		if (this.city != null && !this.city.isEmpty())
			this.completedColumnFlag |= FLAG_CITY;

		if (this.country != null && !this.country.isEmpty())
			this.completedColumnFlag |= FLAG_COUNTRY;

		if (this.birthday != null)
			this.completedColumnFlag |= FLAG_BIRTHDAY;
	}

	@Override
	public String toString() {
		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_FORMAT);

		StringBuilder builder = new StringBuilder("[" + TABLE_NAME + " : ");

		builder.append(this.idClientInformation);
		builder.append(", ");
		builder.append(this.idClient);
		builder.append(", ");
		builder.append(this.tel);
		builder.append(", ");
		builder.append(this.address);
		builder.append(", ");
		builder.append(this.city);
		builder.append(", ");
		builder.append(this.country);
		builder.append(", ");
		builder.append(this.birthday != null ? dateFormat.format(this.birthday) : null);
		builder.append("]");

		return builder.toString();
	}

	/**
	 * <p>
	 * <strong>The telephone can be null.</strong>
	 * </p>
	 * <p>
	 * The format is different in function of the localization.
	 * </p>
	 * 
	 * @param tel
	 * @return true if the telephone can be put in the DB like this.
	 */
	public static boolean verifyTel(String tel) {
		// TODO restriction on the format in function of the localization.
		return true;
	}

	/**
	 * <p>
	 * <strong>The address can be null.</strong>
	 * </p>
	 * <p>
	 * if it not null, the address length must be less or equal
	 * {@link ClientInformationRow#ADDRESS_MAX_SIZE}
	 * </p>
	 * 
	 * @param address
	 * @return true if the address can be put in the DB like this.
	 */
	public static boolean verifyAddress(String address) {
		return address == null ? true : address.length() <= ADDRESS_MAX_SIZE;
	}

	/**
	 * <p>
	 * <strong>The city can be null.</strong>
	 * </p>
	 * <p>
	 * if it not null, the city length must be less or equal
	 * {@link ClientInformationRow#CITY_MAX_SIZE}
	 * </p>
	 * 
	 * @param city
	 * @return true if the city can be put in the DB like this.
	 */
	public static boolean verifyCity(String city) {
		return city == null ? true : city.length() <= CITY_MAX_SIZE;
	}

	/**
	 * <p>
	 * <strong>The country can be null.</strong>
	 * </p>
	 * <p>
	 * if it not null, the country length must be less or equal
	 * {@link ClientInformationRow#COUNTRY_MAX_SIZE}
	 * </p>
	 * 
	 * @param country
	 * @return true if the city can be put in the DB like this.
	 */
	public static boolean verifyCountry(String country) {
		return country == null ? true : country.length() <= CITY_MAX_SIZE;
	}

	// Getters and Setters.

	public Long getIdClientInformation() {
		return idClientInformation;
	}

	public void setIdClientInformation(Long idClientInformation) {
		this.idClientInformation = idClientInformation;
		this.reformAll();
	}

	public Long getIdClient() {
		return idClient;
	}

	public void setIdClient(Long idClient) {
		this.idClient = idClient;
		this.reformAll();
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
		this.reformAll();
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
		this.reformAll();
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
		this.reformAll();
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
		this.reformAll();
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
		this.reformAll();
	}

	public void setBirthday(int year, int month, int day) {
		DateFormat dateFormat = new SimpleDateFormat(DAO.DATE_FORMAT);

		StringBuilder builder = new StringBuilder();

		builder.append(year);
		builder.append("-");
		builder.append(month);
		builder.append("-");
		builder.append(day);

		try {
			this.birthday = dateFormat.parse(builder.toString());
		} catch (ParseException e) {
			ErrorManager.writeError(e);
			this.birthday = null;
		}
		
		this.reformAll();
	}
}
