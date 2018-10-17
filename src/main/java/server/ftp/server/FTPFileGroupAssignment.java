package server.ftp.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import server.ftp.exception.FatalErrorException;
import server.ftp.exception.file_assignment.GroupDoesNotExistsException;
import server.ftp.exception.file_assignment.MembreUnknownException;
import server.ftp.exception.file_assignment.RootGroupCantBeRemovedException;
import server.ftp.sql.dao.ClientDAO;
import server.ftp.sql.dao.DAOFactory;
import server.ftp.sql.table_row.ClientRow;
import server.sql.connection.SQLConnectionManager;
import server.util.ErrorManager;

public class FTPFileGroupAssignment {

	// Constants.

	public static final String ROOT_CLIENT = "root";

	public static final String ROOT_GROUP = "root";

	/**
	 * ;
	 */
	public static final String GROUP_SEPARATOR = ";";

	/**
	 * :
	 */
	public static final String GROUP_NAME_CLIENT_SEPARATOR = ":";

	/**
	 * ,
	 */
	public static final String GROUP_CLIENT_SEPARATOR = ",";

	private static final String STRING_CREATE_ROOT_GROUP = FTPFileGroupAssignment.ROOT_GROUP
			+ GROUP_NAME_CLIENT_SEPARATOR + FTPFileGroupAssignment.ROOT_CLIENT + GROUP_SEPARATOR;

	// Variables.

	private FTPFileManager ftpFileManager;

	/**
	 * The list of all user that the serve rcan accept.
	 */
	private Set<String> setServerUser;

	private ConcurrentHashMap<String, Set<String>> hashGroup;

	// Constructors.

	public FTPFileGroupAssignment(FTPFileManager ftpFileManager) throws FatalErrorException {

		this.ftpFileManager = ftpFileManager;

		this.initListServerUser();

		this.initHashGroup();

	}

	// Methods.

	/**
	 * <p>
	 * Append in the file the root group.
	 * </p>
	 * <p>
	 * root:root;
	 * </p>
	 * 
	 * @param pathGroupFile
	 * @throws IOException
	 */
	public static void writeRootGroup(Path pathGroupFile) throws IOException {
		BufferedWriter writer = null;

		try {

			writer = Files.newBufferedWriter(pathGroupFile, StandardOpenOption.APPEND);

			writer.write(STRING_CREATE_ROOT_GROUP);

		} finally {
			if (writer != null)
				writer.close();
		}
	}

	private void initListServerUser() {
		ClientDAO clientDAO = (ClientDAO) DAOFactory.newClientDAO();

		Connection SQLConnection = SQLConnectionManager.getConnection();

		this.setServerUser = new HashSet<>();

		this.setServerUser.add(ROOT_CLIENT);

		try {

			List<ClientRow> listClient = clientDAO.getAll(SQLConnection);

			if (listClient != null)
				for (ClientRow client : listClient) {
					this.setServerUser.add(client.getPseudo());
				}

		} catch (SQLException e) {
			ErrorManager.writeError(e);
		} finally {
			SQLConnectionManager.evictConnection(SQLConnection);
		}

	}

	/**
	 * Always be done after {@link FTPFileAssignment#initListServerUser()}.
	 * 
	 * @throws FatalErrorException
	 */
	private void initHashGroup() throws FatalErrorException {
		this.hashGroup = new ConcurrentHashMap<>();

		try (BufferedReader reader = Files.newBufferedReader(this.ftpFileManager.PATH_GROUP_FILE)) {

			StringBuilder builder = new StringBuilder();

			String s = null;
			while ((s = reader.readLine()) != null) {
				builder.append(s);
			}

			String all = builder.toString();

			StringTokenizer groupSeparator = new StringTokenizer(all, GROUP_SEPARATOR);

			String[] groups = new String[groupSeparator.countTokens()];

			for (int i = 0; i < groups.length; i++) {
				groups[i] = groupSeparator.nextToken();
			}

			for (String group : groups) {
				StringTokenizer nameGroupSeparator = new StringTokenizer(group, GROUP_NAME_CLIENT_SEPARATOR);

				String groupName = nameGroupSeparator.nextToken();
				Set<String> setClient = new HashSet<>();

				if (nameGroupSeparator.hasMoreTokens()) {

					String clients = nameGroupSeparator.nextToken();

					StringTokenizer clientSeparator = new StringTokenizer(clients, GROUP_CLIENT_SEPARATOR);

					while (clientSeparator.hasMoreTokens()) {
						setClient.add(clientSeparator.nextToken());
					}
				}

				this.hashGroup.put(groupName, setClient);
			}

			// Verification if the root is in the file.

			if (this.hashGroup.get(ROOT_GROUP) == null) {
				writeRootGroup(this.ftpFileManager.PATH_GROUP_FILE);

				this.initHashGroup();
			}

		} catch (IOException e) {
			ErrorManager.writeError(e);
		} catch (NoSuchElementException e) {
			throw new FatalErrorException("Group file corrupted.");
		}

	}

	private void writeGroup() {
		try (BufferedWriter writer = Files.newBufferedWriter(this.ftpFileManager.PATH_GROUP_FILE)) {

			Set<Entry<String, Set<String>>> set = this.hashGroup.entrySet();

			for (Entry<String, Set<String>> entry : set) {
				writer.write(entry.getKey() + GROUP_NAME_CLIENT_SEPARATOR);

				Set<String> setName = entry.getValue();

				int i = 0;
				for (String name : setName) {
					if (i < setName.size() - 1)
						writer.write(name + GROUP_CLIENT_SEPARATOR);
					else
						writer.write(name);

					i++;
				}

				writer.write(GROUP_SEPARATOR);
			}

		} catch (IOException e) {
			ErrorManager.writeError(e);
		}
	}

	public void refresh() throws FatalErrorException {
		this.initListServerUser();

		this.initHashGroup();
	}

	/**
	 * 
	 * @param groupName
	 * @return true if the new group has been create else false. If the group
	 *         already exists it is not create a new time.
	 */
	public boolean createGroup(String groupName) {
		if (!this.hashGroup.containsKey(groupName)) {
			this.hashGroup.put(groupName, new HashSet<String>());

			this.writeGroup();

			return true;
		} else {
			return false;
		}
	}

	public void removeGroup(String groupName) throws GroupDoesNotExistsException {

		if (groupName == null)
			throw new NullPointerException("Groupname null");

		if (ROOT_GROUP.equals(groupName))
			throw new RootGroupCantBeRemovedException();

		if (this.hashGroup.get(groupName) == null)
			throw new GroupDoesNotExistsException("Group = " + groupName);

		this.hashGroup.remove(groupName);

		Set<Entry<String, Set<String>>> set = this.hashGroup.entrySet();

		for (Entry<String, Set<String>> entry : set) {
			Set<String> setName = entry.getValue();

			setName.remove(groupName);
		}

		this.writeGroup();
	}

	/**
	 * 
	 * @param member
	 * @param group
	 * 
	 * @return true if the member has been add, else false. The member can be add
	 *         ifhe is already in the group.
	 * 
	 * @throws GroupDoesNotExistsException
	 * @throws MembreUnknownException
	 */
	public boolean addMemberInGroup(String member, String group)
			throws GroupDoesNotExistsException, MembreUnknownException {
		Set<String> setName = this.hashGroup.get(group);

		if (setName == null)
			throw new GroupDoesNotExistsException("Group = " + group);

		// A membre can be a group or a client.
		if (!this.setServerUser.contains(member) && !this.hashGroup.containsKey(member))
			throw new MembreUnknownException("Membre = " + member);

		boolean add = setName.add(member);

		if (add) {
			this.writeGroup();

			return true;
		} else
			return false;
	}

	/**
	 * Use in the {@link FTPFileAssignment#memberIsInGroup(String, String)}. We need
	 * this method to look if sub group have not be seen before to avoid infinite
	 * loop.
	 * 
	 * @param member
	 * @param group
	 * @param groupSeen
	 * @return true if the member is in group, else false.
	 */
	private boolean memberIsInGroup(String member, String group, Set<String> groupSeen) {
		Set<String> setName = this.hashGroup.get(group);

		for (String name : setName) {

			// Can be shorter but if it is shorter we can verify that name or correct and
			// knowned.

			if (this.isClient(name)) {
				if (name.equals(member)) {
					return true;
				}
			} else if (this.isGroup(name)) {
				if (name.equals(member))
					return true;
				else {
					// If the group has never be seen.
					if (groupSeen.add(name)) {

						boolean find = this.memberIsInGroup(member, name, groupSeen);

						if (find)
							return true;
					}
				}
			} else {
				System.err.println("Name unknown.");
			}
		}

		return false;
	}

	/**
	 * 
	 * @param member
	 * @param group
	 * @return true if the member is in group, else false.
	 * @throws GroupDoesNotExistsException
	 * @throws MembreUnknownException
	 */
	public boolean memberIsInGroup(String member, String group)
			throws GroupDoesNotExistsException, MembreUnknownException {

		if (member == null)
			throw new NullPointerException("Member null");

		if (group == null)
			throw new NullPointerException("Group null");

		// A membre can be a group or a client.
		if (!this.setServerUser.contains(member) && !this.hashGroup.containsKey(member))
			throw new MembreUnknownException("Membre = " + member);

		Set<String> setName = this.hashGroup.get(group);
		;

		if (setName == null)
			throw new GroupDoesNotExistsException("Group = " + group);

		Set<String> groupSeen = new HashSet<>();
		groupSeen.add(group);

		for (String name : setName) {

			// Can be shorter but if it is shorter we can verify that name or correct and
			// knowned.

			if (this.isClient(name)) {
				if (name.equals(member)) {
					return true;
				}
			} else if (this.isGroup(name)) {

				groupSeen.add(name);

				if (name.equals(member))
					return true;
				else {
					boolean find = this.memberIsInGroup(member, name, groupSeen);

					if (find)
						return true;
				}
			} else {
				System.err.println("Name unknown.");
			}
		}

		return false;
	}

	public boolean isGroup(String name) {
		return this.hashGroup.get(name) != null;
	}

	public boolean isClient(String name) {
		return this.setServerUser.contains(name);
	}

	// Getters and Setters.

	public FTPFileManager getFTPFileManager() {
		return this.ftpFileManager;
	}

	public Set<String> getUnmodifiableSetUsers() {
		return Collections.unmodifiableSet(this.setServerUser);
	}

}
