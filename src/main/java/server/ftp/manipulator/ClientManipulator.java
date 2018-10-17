package server.ftp.manipulator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import server.ftp.sql.dao.ClientDAO;
import server.ftp.sql.dao.ClientInformationDAO;
import server.ftp.sql.dao.DAOFactory;
import server.ftp.sql.table_row.ClientInformationRow;
import server.ftp.sql.table_row.ClientRow;
import server.sql.connection.SQLConnectionManager;
import server.util.ErrorManager;
import server.util.Manipulator;
import server.util.exception.ManipulatorException;

public class ClientManipulator extends Manipulator {

	// Constants.

	// Variables.

	private ClientRow client;
	private ClientInformationRow clientInformation;

	private ClientDAO clientDAO = (ClientDAO) DAOFactory.newClientDAO();
	private ClientInformationDAO clientInformationDAO = (ClientInformationDAO) DAOFactory.newClientInformationDAO();

	// Constructors.

	/**
	 * <p>
	 * Initialize the ClientManipulator with a client <b>(which can't be null)</b>
	 * and a client information <b>(which can be null)</b>.
	 * </p>
	 * <p>
	 * Each argument are clone.
	 * </p>
	 * 
	 * @param client
	 *            - the client <b>CAN'T BE NULL</b>
	 * @param clientInformation
	 *            - the client information <b>CAN BE NULL</b>
	 */
	public ClientManipulator(ClientRow client, ClientInformationRow clientInformation) {
		this(client, clientInformation, false);
	}

	/**
	 *
	 * <p>
	 * Initialize the ClientManipulator with a client <b>(which can't be null)</b>
	 * and a client information <b>(which can be null)</b>.
	 * </p>
	 * 
	 * <p>
	 * The state is determined by the boolean isAlreadyCreate. If you put true, the
	 * state is create and you can directly use update and delete but not create.
	 * </p>
	 * <p>
	 * Put true can be dangerous if you don't verify that client and client
	 * information are correctly linked together.
	 * </p>
	 * 
	 * @see Manipulator#Manipulator(boolean)
	 * 
	 * @param client
	 *            - the client
	 * @param clientInformation
	 *            - the client information
	 * @param isAlreadyCreate
	 *            - boolean which said if the state is create or not
	 */
	public ClientManipulator(ClientRow client, ClientInformationRow clientInformation, boolean isAlreadyCreate) {
		super(isAlreadyCreate);

		if (client == null)
			throw new IllegalArgumentException("Client null");

		this.client = client.clone();

		if (clientInformation != null)
			this.clientInformation = clientInformation.clone();
		else
			this.clientInformation = null;
	}

	/**
	 * <p>
	 * Initialize the manipulator with the client who is associated with the
	 * mail/pseudo.
	 * </p>
	 * <p>
	 * With this constructor, the manipulator pass directly to the "create" state.
	 * It mine that you cannot call the create method, but you can update or delete
	 * the manipulator.
	 * </p>
	 * <p>
	 * If you call the delete method, you can after call the created method but not
	 * before.
	 * </p>
	 * <p>
	 * if no client is associated with the mail, throw a ManipulatorException.
	 * </p>
	 * 
	 * @param entry
	 *            - the mail or the pseudo associated to the client.
	 * @param typeEntry
	 *            - the type of entry (if it's a mail or a pseudo)
	 * 
	 * @throws ManipulatorException
	 *             if there is no client associated to the mail/pseudo or if there
	 *             is a SQLException.
	 */
	public ClientManipulator(String entry, TypeEntry typeEntry) throws ManipulatorException {
		super(true);

		Connection SQLConnection = SQLConnectionManager.getConnection();

		try {

			if (typeEntry == TypeEntry.MAIL)
				this.client = this.clientDAO.findByMail(entry, SQLConnection);
			else if (typeEntry == TypeEntry.PSEUDO)
				this.client = this.clientDAO.findByPseudo(entry, SQLConnection);

			// If there is no client associate at this mail.
			// Manipulator exception.
			if (this.client == null) {
				throw new ManipulatorException("Client non trouv�");
			}

			this.clientInformation = this.clientInformationDAO.findByIdClient(this.client.getIdClient(), SQLConnection);

		} catch (SQLException e) {
			ErrorManager.writeError(e);
			throw new ManipulatorException("MySQL Connection Probl�me.", e);
		} finally {
			SQLConnectionManager.evictConnection(SQLConnection);
		}

	}

	// Methods.

	@Override
	protected void createProtected() throws ManipulatorException {
		if (this.client.isReadyToBeCreated()) {
			Connection SQLConnection = SQLConnectionManager.getConnection();

			try {
				// Client creation.
				this.clientDAO.create(this.client, SQLConnection, false);

				// If there is client information, creation of the client information.
				if (this.clientInformation != null) {

					// We put the id client to the ClientInformation.
					this.clientInformation.setIdClient(this.client.getIdClient());

					if (this.clientInformation.isReadyToBeCreated()) {

						// ClientInformation creation.
						// Update the client Row to link it with the client information.
						this.clientInformationDAO.create(this.clientInformation, SQLConnection, false);

						// We link the client to the ClientInformation row.
						this.client.setIdClientInformation(this.clientInformation.getIdClientInformation());

						// Success! We commit
						SQLConnection.commit();

					} else {
						// Fail!
						SQLConnection.rollback();
						throw new ManipulatorException(
								"ClientInformation not ready to be created. The client has not been created. ClientInformation = "
										+ this.clientInformation);
					}
				} else {
					// The client has no information but can be create.
					// Success! We commit
					SQLConnection.commit();
				}

			} catch (SQLException e) {
				try {
					SQLConnection.rollback();
				} catch (SQLException e1) {
					throw new ManipulatorException("SQLException occurred. Failed rollback!!!", e1);
				}
				throw new ManipulatorException("SQLException occurred.", e);
			} finally {
				SQLConnectionManager.evictConnection(SQLConnection);
			}
		} else {
			throw new ManipulatorException("Client not ready to be created. Client = " + this.client);
		}
	}

	@Override
	protected void updateProtected() throws ManipulatorException {
		// If the client is ready to update.
		if (this.client.isReadyToBeUpdated()) {
			Connection SQLConnection = SQLConnectionManager.getConnection();

			try {
				this.clientDAO.update(this.client, SQLConnection, true);

				// Maybe the client has been created without information.
				// and now we want add to the client this information.
				// We search the client information.

				ClientInformationRow cI = this.clientInformationDAO.findByIdClient(this.client.getIdClient(),
						SQLConnection);

				if (this.clientInformation != null) {

					// If there is not client information.
					if (cI == null) {
						// We link client and client information.
						this.clientInformation.setIdClient(this.client.getIdClient());

						if (this.clientInformation.isReadyToBeCreated()) {

							// We create the client information (link with client row and client information
							// is done in the creation)
							this.clientInformationDAO.create(this.clientInformation, SQLConnection, true);

							// We link the client to the ClientInformation row.
							this.client.setIdClientInformation(this.clientInformation.getIdClientInformation());
							
						} else {
							throw new ManipulatorException(
									"Client Information is not ready be created in the UPDATE . ClientInformation = "
											+ this.clientInformation);
						}
					} else {

						// We work on the same client information row.
						this.clientInformation.setIdClientInformation(cI.getIdClientInformation());
						this.clientInformation.setIdClient(cI.getIdClient());

						// There already is client information.
						if (this.clientInformation.isReadyToBeUpdated()) {

							this.clientInformationDAO.update(this.clientInformation, SQLConnection, true);

						} else {
							throw new ManipulatorException(
									"Client Information is not ready be updated. ClientInformation = "
											+ this.clientInformation);
						}
					}

					// TODO

				} else {

					// The clientInformation must been delete.
					if (cI != null) {
						this.clientInformationDAO.delete(cI, SQLConnection, false);
					}

					// Success!
					// We commit
					SQLConnection.commit();
				}

			} catch (SQLException e) {
				try {
					SQLConnection.rollback();
				} catch (SQLException e1) {
					throw new ManipulatorException("SQLException occurred. Failed rollback!!!", e1);
				}
				throw new ManipulatorException("SQLException occurred.", e);

			} finally {
				SQLConnectionManager.evictConnection(SQLConnection);
			}

		} else {
			throw new ManipulatorException("Client not ready to be updated. Client = " + this.client);
		}
	}

	@Override
	protected void deleteProtected() throws ManipulatorException {
		if (this.client.isReadyToBeDeleted()) {

			Connection SQLConnection = SQLConnectionManager.getConnection();

			try {
				// Delete the client and the client information.
				this.clientDAO.delete(this.client, SQLConnection, true);

				this.client = new ClientRow(null, this.client.getPseudo(), this.client.getPassword(),
						this.client.getFirstName(), this.client.getLastName(), this.client.getMail(), null);

				if (this.clientInformation != null) {
					this.clientInformation = new ClientInformationRow(null, null, this.clientInformation.getTel(),
							this.clientInformation.getAddress(), this.clientInformation.getCity(),
							this.clientInformation.getCountry(), this.clientInformation.getBirthday());
				}
			} catch (SQLException e) {
				throw new ManipulatorException("Delete not success for the client and the client information. Client = "
						+ this.client + " ClientInformation = " + this.clientInformation, e);
			} finally {
				SQLConnectionManager.evictConnection(SQLConnection);
			}
		} else {
			throw new ManipulatorException("Client is not ready to be deleted. Client = " + this.client);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Client Manipulator : [");

		builder.append(this.client);
		builder.append(", ");
		builder.append(this.clientInformation);
		builder.append("]");

		return builder.toString();
	}

	/**
	 * Delete only the client information
	 */
	public void deleteClientInformation() throws ManipulatorException {
		if (this.isAlreadyCreate()) {

			if (this.clientInformation.isReadyToBeDeleted()) {

				Connection SQLConnection = SQLConnectionManager.getConnection();

				try {
					this.clientInformationDAO.delete(this.clientInformation, SQLConnection, true);

					this.clientInformation = new ClientInformationRow(null, null, this.clientInformation.getTel(),
							this.clientInformation.getAddress(), this.clientInformation.getCity(),
							this.clientInformation.getCountry(), this.clientInformation.getBirthday());
				} catch (SQLException e) {
					throw new ManipulatorException(
							"Delete not success for the ClientInformation." + this.clientInformation, e);
				} finally {
					SQLConnectionManager.evictConnection(SQLConnection);
				}

			} else {
				throw new ManipulatorException(
						"ClientInformation is not ready to be deleted. ClientInformation = " + this.clientInformation);
			}
		} else {
			throw new ManipulatorException("The manipulator was not created. DELETE CLIENT INFORMATION imposible.");
		}
	}

	// Getters and Setters.

	public void setNoClientInformation() {
		this.clientInformation = null;
	}

	/**
	 * 
	 * @return the current clone of the client.
	 */
	public ClientRow getClient() {
		return this.client.clone();
	}

	/**
	 * 
	 * @return the current clone of the client information.
	 */
	public ClientInformationRow getClientInformation() {
		return this.clientInformation.clone();
	}
	
	public Long getIdClient() {
		return this.client.getIdClient();
	}

	public Long getIdClientInformation() {
		return this.client.getIdClientInformation();
	}

	public String getPseudo() {
		return this.client.getPseudo();
	}

	public void setPseudo(String pseudo) {
		this.client.setPseudo(pseudo);
	}

	public String getPassword() {
		return this.client.getPassword();
	}

	public void setPassword(String password) {
		this.client.setPassword(password);
	}

	public String getFirstName() {
		return client.getFirstName();
	}

	public void setFirstName(String firstName) {
		client.setFirstName(firstName);
	}

	public String getLastName() {
		return client.getLastName();
	}

	public void setLastName(String lastName) {
		client.setLastName(lastName);
	}

	public String getMail() {
		return client.getMail();
	}

	public void setMail(String mail) {
		client.setMail(mail);
	}

	public String getTel() {
		return clientInformation.getTel();
	}

	/**
	 * <strong>Create a ClientInformation if the ClientInformation is null.</strong>
	 * 
	 * @param tel
	 */
	public void setTel(String tel) {
		if (this.clientInformation == null)
			this.clientInformation = new ClientInformationRow();

		clientInformation.setTel(tel);
	}

	public String getAddress() {
		return clientInformation.getAddress();
	}

	/**
	 * <strong>Create a ClientInformation if the ClientInformation is null.</strong>
	 * 
	 * @param address
	 */
	public void setAddress(String address) {
		if (this.clientInformation == null)
			this.clientInformation = new ClientInformationRow();

		clientInformation.setAddress(address);
	}

	public String getCity() {
		return clientInformation.getCity();
	}

	/**
	 * <strong>Create a ClientInformation if the ClientInformation is null.</strong>
	 * 
	 * @param city
	 */
	public void setCity(String city) {
		if (this.clientInformation == null)
			this.clientInformation = new ClientInformationRow();

		clientInformation.setCity(city);
	}

	public String getCountry() {
		return clientInformation.getCountry();
	}

	/**
	 * <strong>Create a ClientInformation if the ClientInformation is null.</strong>
	 * 
	 * @param country
	 */
	public void setCountry(String country) {
		if (this.clientInformation == null)
			this.clientInformation = new ClientInformationRow();

		clientInformation.setCountry(country);
	}

	public Date getBirthday() {
		return clientInformation.getBirthday();
	}

	/**
	 * <strong>Create a ClientInformation if the ClientInformation is null.</strong>
	 * 
	 * @param birthday
	 */
	public void setBirthday(Date birthday) {
		if (this.clientInformation == null)
			this.clientInformation = new ClientInformationRow();

		clientInformation.setBirthday(birthday);
	}

	/**
	 * <strong>Create a ClientInformation if the ClientInformation is null.</strong>
	 * 
	 * @param year
	 * @param mounth
	 * @param day
	 */
	public void setBirthday(int year, int mounth, int day) {
		if (this.clientInformation == null)
			this.clientInformation = new ClientInformationRow();

		clientInformation.setBirthday(year, mounth, day);
	}

	// Enumeration.

	public static enum TypeEntry {
		MAIL, PSEUDO;
	}

}
