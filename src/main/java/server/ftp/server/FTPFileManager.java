package server.ftp.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import server.ftp.exception.FatalErrorException;
import server.ftp.exception.file_access_manager.PathNotPutException;
import server.ftp.exception.file_assignment.GroupDoesNotExistsException;
import server.ftp.exception.file_assignment.MembreUnknownException;
import server.ftp.exception.file_manager.ClearWorkspaceDirectoryFailedException;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.ftp.exception.file_manager.NameForFileNotCorrectException;
import server.ftp.exception.file_manager.NoAccessToThisFileException;
import server.ftp.exception.file_manager.PathIsNotDirectoryException;
import server.ftp.exception.file_manager.PathIsNotFileException;
import server.ftp.exception.file_manager.RemotePahtWrongFormatException;
import server.ftp.exception.file_manager.RootUserDirectoryAlreadyExistsException;
import server.ftp.exception.file_manager.UserHasNotWorkspaceDirectoryException;
import server.ftp.server.FTPFileAccess.FileAccess;
import server.util.ErrorManager;

/**
 * <p>
 * This object manage all file of the FTP server.
 * </p>
 * <p>
 * Each user register in the server has a directory which has name the ID of the
 * user in the data base.
 * </p>
 * <p>
 * When a client want to work in this work space, the FileManager will enter in
 * the file which is corresponding with is ID.
 * </p>
 * <p>
 * For file sharing, there is the same treatment. Each sharing has an ID in the
 * data base, the sharing has a directory which has name the ID of the sharing.
 * </p>
 * 
 * @author 3407488
 *
 */
public class FTPFileManager {

	// Constants.

	private static final String FTP_WORKSPACE = "FTP";

	private static final String USERS_DIRECTORY_NAME = "Users";

	private static final String CONFIG_DIRECTORY_NAME = "Config";

	private static final String GROUP_FILE_NAME = "group";

	private static final String FILE_ACCESS_FILE_NAME = "file_access";

	private static final int LOCK_MANAGER_CAPACITY = 150;

	public static final String FILE_SEPARATOR = "\\";

	public static final FileAccess[] DEFAULT_FILE_ACCESS_USER_DIRECTORY = { FileAccess.READ_WRITE_EXECUTE,
			FileAccess.READ_WRITE_EXECUTE, FileAccess.NOTHING };

	/**
	 * Z:
	 */
	public static final String REMOTE_ROOT_STRING = "Z:";

	/**
	 * Z:\
	 */
	public static final Path REMOTE_ROOT_PATH = Paths.get(REMOTE_ROOT_STRING + FILE_SEPARATOR);

	public static final String REMOTE_USERS_DIRECTORY_PATH = REMOTE_ROOT_STRING + FILE_SEPARATOR + FTP_WORKSPACE
			+ FILE_SEPARATOR + USERS_DIRECTORY_NAME;

	public final FTPFileAccess PATH_LOCALISATION_FTP_DATA_ACCESS;
	public final FTPFileAccess PATH_WORKSPACE_ACCESS;
	public final FTPFileAccess PATH_USERS_DIRECTORY_ACCESS;

	/**
	 * This variable is the path where the user want that the server store all data
	 * of the FTP server.
	 */
	public final String STRING_PATH_LOCALISATION_FTP_DATA;
	public final String STRING_PATH_WORKSPACE;
	public final String STRING_PATH_USERS_DIRECTORY;
	public final String STRING_PATH_CONFIG_DIRECTORY;
	public final String STRING_PATH_GROUP_FILE;
	public final String STRING_PATH_FILE_ACCESS_FILE;

	public final Path PATH_LOCALISATION_FTP_DATA;
	public final Path PATH_WORKSPACE;
	public final Path PATH_USERS_DIRECTORY;
	public final Path PATH_CONFIG_DIRECTORY;
	public final Path PATH_GROUP_FILE;
	public final Path PATH_FILE_ACCESS_FILE;

	// Variables.

	private final FTPFileGroupAssignment ftpFileGroupAssignment;
	private final FTPFileAccessManager ftpFileAccessManager;

//	private final FTPLockManager ftpLockManager;

	// Constructors.

	public FTPFileManager() throws FatalErrorException {
		this(null);
	}

	public FTPFileManager(String PATH_LOCALISATION_DATA) throws FatalErrorException {
		String TMP_PATH_LOCALISATION_DATA = PATH_LOCALISATION_DATA;

		if (TMP_PATH_LOCALISATION_DATA == null || TMP_PATH_LOCALISATION_DATA.isEmpty()) {
			this.STRING_PATH_LOCALISATION_FTP_DATA = System.getProperty("user.dir");
		} else {
			this.STRING_PATH_LOCALISATION_FTP_DATA = PATH_LOCALISATION_DATA;
		}

		this.PATH_LOCALISATION_FTP_DATA = Paths.get(this.STRING_PATH_LOCALISATION_FTP_DATA);

		this.PATH_LOCALISATION_FTP_DATA_ACCESS = new FTPFileAccess(this.PATH_LOCALISATION_FTP_DATA,
				FTPFileGroupAssignment.ROOT_CLIENT, FTPFileGroupAssignment.ROOT_GROUP, FileAccess.READ_WRITE_EXECUTE,
				FileAccess.READ_WRITE_EXECUTE, FileAccess.NOTHING);

		this.STRING_PATH_WORKSPACE = this.STRING_PATH_LOCALISATION_FTP_DATA + FILE_SEPARATOR + FTP_WORKSPACE;

		this.PATH_WORKSPACE = Paths.get(this.STRING_PATH_WORKSPACE);

		this.PATH_WORKSPACE_ACCESS = new FTPFileAccess(this.PATH_WORKSPACE, FTPFileGroupAssignment.ROOT_CLIENT,
				FTPFileGroupAssignment.ROOT_GROUP, FileAccess.READ_WRITE_EXECUTE, FileAccess.READ_WRITE_EXECUTE,
				FileAccess.NOTHING);

		this.STRING_PATH_USERS_DIRECTORY = this.STRING_PATH_WORKSPACE + FILE_SEPARATOR + USERS_DIRECTORY_NAME;

		this.PATH_USERS_DIRECTORY = Paths.get(this.STRING_PATH_USERS_DIRECTORY);

		this.PATH_USERS_DIRECTORY_ACCESS = new FTPFileAccess(this.PATH_USERS_DIRECTORY,
				FTPFileGroupAssignment.ROOT_CLIENT, FTPFileGroupAssignment.ROOT_GROUP, FileAccess.READ_WRITE_EXECUTE,
				FileAccess.READ_WRITE_EXECUTE, FileAccess.READ_EXECUTE);

		this.STRING_PATH_CONFIG_DIRECTORY = this.STRING_PATH_LOCALISATION_FTP_DATA + FILE_SEPARATOR
				+ CONFIG_DIRECTORY_NAME;

		this.PATH_CONFIG_DIRECTORY = Paths.get(this.STRING_PATH_CONFIG_DIRECTORY);

		this.STRING_PATH_GROUP_FILE = this.STRING_PATH_CONFIG_DIRECTORY + FILE_SEPARATOR + GROUP_FILE_NAME;

		this.PATH_GROUP_FILE = Paths.get(this.STRING_PATH_GROUP_FILE);

		this.STRING_PATH_FILE_ACCESS_FILE = this.STRING_PATH_CONFIG_DIRECTORY + FILE_SEPARATOR + FILE_ACCESS_FILE_NAME;

		this.PATH_FILE_ACCESS_FILE = Paths.get(this.STRING_PATH_FILE_ACCESS_FILE);

//		this.ftpLockManager = new FTPLockManager(LOCK_MANAGER_CAPACITY);

		this.createWorkSpace();

		this.createConfig();

		// Order important.

		this.ftpFileGroupAssignment = new FTPFileGroupAssignment(this);
		this.ftpFileAccessManager = new FTPFileAccessManager(this);

		this.putImportantDirectory();

		this.putAllUserPath();
	}

	// Methods.

	public static String generateUniqueFileName(Path directory) throws PathIsNotDirectoryException {
		if (Files.isDirectory(directory)) {

			File direcotryFile = directory.toFile();

			File[] subFile = direcotryFile.listFiles();

			ArrayList<String> listFileName = new ArrayList<>(subFile.length);

			for (int i = 0; i < subFile.length; i++) {
				File file = subFile[i];

				if (file.isFile()) {
					listFileName.add(file.getName());
				}
			}

			String vrac = "";

			for (String string : listFileName) {
				vrac += string;
			}

			return Integer.toString(vrac.hashCode());

		} else {
			throw new PathIsNotDirectoryException(directory);
		}
	}

	private void putImportantDirectory() {
		try {

			this.ftpFileAccessManager.putFile(this.PATH_LOCALISATION_FTP_DATA, this.PATH_LOCALISATION_FTP_DATA_ACCESS);

			this.ftpFileAccessManager.putFile(this.PATH_WORKSPACE, this.PATH_WORKSPACE_ACCESS);

			this.ftpFileAccessManager.putFile(this.PATH_USERS_DIRECTORY, this.PATH_USERS_DIRECTORY_ACCESS);

		} catch (LocalPathIsNotServerPathException e) {
			// Never append
			ErrorManager.writeError(e);
		} catch (PathNotPutException e) {
			ErrorManager.writeError(e);
		}
	}

	private void putAllUserPath() {
		Set<String> setUser = this.ftpFileGroupAssignment.getUnmodifiableSetUsers();

		for (String user : setUser) {
			try {

				Path userPath = Paths.get(this.STRING_PATH_USERS_DIRECTORY, user);

				this.ftpFileAccessManager.putFile(Paths.get(this.STRING_PATH_USERS_DIRECTORY, user),
						new FTPFileAccess(userPath, user, user, DEFAULT_FILE_ACCESS_USER_DIRECTORY[0],
								DEFAULT_FILE_ACCESS_USER_DIRECTORY[1], DEFAULT_FILE_ACCESS_USER_DIRECTORY[2]));
			} catch (LocalPathIsNotServerPathException e) {
				// Never append
				ErrorManager.writeError(e);
			} catch (PathNotPutException e) {
				ErrorManager.writeError(e);
			}
		}
	}

	/**
	 * Create the workspace for the FTP server.
	 */
	private void createWorkSpace() {
		Path pathWorkspace = null;
		Path pathUserDirectory = null;
		try {
			pathWorkspace = Paths.get(this.STRING_PATH_WORKSPACE);

			boolean exists = Files.exists(pathWorkspace);

			if (!exists) {
				Files.createDirectory(pathWorkspace);

				pathUserDirectory = Paths.get(this.STRING_PATH_USERS_DIRECTORY);

				Files.createDirectory(pathUserDirectory);

			} else {
				System.err.println("Dossier workspace existe deja");
			}
		} catch (IOException e) {
			ErrorManager.writeError(e);
		}
	}

	private void createConfig() {

		Path pathConfig = null;
		Path pathGroupFile = null;

		try {

			pathConfig = Paths.get(this.STRING_PATH_CONFIG_DIRECTORY);

			pathGroupFile = Paths.get(this.STRING_PATH_GROUP_FILE);

			boolean exists = Files.exists(pathConfig);

			if (!exists) {

				Files.createDirectory(pathConfig);

				Files.createFile(pathGroupFile);

				FTPFileGroupAssignment.writeRootGroup(pathGroupFile);

				return;
			} else {
				exists = Files.exists(pathGroupFile);

				if (!exists) {
					Files.createFile(pathGroupFile);

					FTPFileGroupAssignment.writeRootGroup(pathGroupFile);
				}
			}

		} catch (IOException e) {
			ErrorManager.writeError(e);
		}
	}

	/**
	 * Remove all root users directory.
	 */
	@SuppressWarnings("unused")
	private void clearAllUserWorkspaceDirectory() {
		try {
			Files.newDirectoryStream(Paths.get(this.STRING_PATH_USERS_DIRECTORY)).forEach(path -> {
				try {
					this.deleteDirectoryRecursive(path);
				} catch (IOException e) {
					ErrorManager.writeError(e);
				}
			});
		} catch (IOException e) {
			ErrorManager.writeError(e);
		}
	}

	/**
	 * Delete a directory or a file. If the directory is not empty, delete all
	 * elements inside.
	 * 
	 * @param pathDirectory
	 * @throws IOException
	 */
	private void deleteDirectoryRecursive(Path pathDirectory) throws IOException {
		if (Files.isDirectory(pathDirectory)) {
			try {

				// TODO le dossier ne veut jamais se supprimer directement.

				File[] tabFile = pathDirectory.toFile().listFiles();

				if (tabFile != null) {

					for (int i = 0; i < tabFile.length; i++) {
						File file = tabFile[i];

						this.deleteDirectoryRecursive(Paths.get(file.getAbsolutePath()));
					}
				}

				this.ftpFileAccessManager.removeFile(pathDirectory);
				
				Files.delete(pathDirectory);
				
				// boolean b = pathDirectory.toFile().delete();
				//
				// System.out.println("Suppression du dossier " + pathDirectory + " reussi ? " +
				// b);

			} catch (LocalPathIsNotServerPathException e) {
				ErrorManager.writeError(e);
			} catch (PathNotPutException e) {
				ErrorManager.writeError(e);
			}
		} else {

			// boolean b = pathDirectory.toFile().delete();
			//
			// System.out.println("Suppression du fichier " + pathDirectory + " reussi ? " +
			// b);

			try {
				this.ftpFileAccessManager.removeFile(pathDirectory);

				Files.delete(pathDirectory);
			} catch (LocalPathIsNotServerPathException e) {
				ErrorManager.writeError(e);
			} catch (PathNotPutException e) {
				ErrorManager.writeError(e);
			}
		}
	}

	public boolean isClient(String pseudo) {
		return this.ftpFileGroupAssignment.isClient(pseudo);
	}

	public boolean isGroup(String group) {
		return this.ftpFileGroupAssignment.isGroup(group);
	}

	/**
	 * 
	 * @param remotePath
	 * @return the local path corresponding to the remote path. The remote path must
	 *         begin with {@link FTPFileManager#REMOTE_ROOT_STRING} character.
	 * @throws RemotePahtWrongFormatException
	 */
	public Path convertToLocalPath(Path remotePath) throws RemotePahtWrongFormatException {

		try {
			if (remotePath.startsWith(REMOTE_ROOT_PATH)) {

				String[] nameRemotePath = new String[remotePath.getNameCount()];

				for (int i = 0; i < remotePath.getNameCount(); i++) {
					nameRemotePath[i] = remotePath.getName(i).toString();
				}

				return Paths.get(this.STRING_PATH_LOCALISATION_FTP_DATA, nameRemotePath);
			} else {
				throw new RemotePahtWrongFormatException(
						"The path does not begin by \"" + REMOTE_ROOT_STRING + "\". RemotePath = " + remotePath);
			}

		} catch (IndexOutOfBoundsException e) {
			throw new RemotePahtWrongFormatException(
					"The path does not begin by \"" + REMOTE_ROOT_STRING + "\". RemotePath = " + remotePath);
		}
	}

	/**
	 * 
	 * @param localPath
	 * @return true if the path is a path which is situated in the ftp server file
	 *         zone. (In others words, if it's starts with
	 *         {@link FTPFileManager#STRING_PATH_LOCALISATION_FTP_DATA}).
	 */
	public boolean isServerFile(Path localPath) {
		return localPath.startsWith(this.STRING_PATH_LOCALISATION_FTP_DATA);
	}

	public Path convertToRemotePath(Path localPath) throws LocalPathIsNotServerPathException {
		if (this.isServerFile(localPath)) {

			int nbName = Paths.get(this.STRING_PATH_LOCALISATION_FTP_DATA).getNameCount();

			String stringRemotePath[] = new String[localPath.getNameCount() - nbName];
			for (int i = 0; i < stringRemotePath.length; i++) {
				stringRemotePath[i] = localPath.getName(i + nbName).toString();
			}

			return Paths.get(REMOTE_ROOT_PATH.toString(), stringRemotePath);

		} else
			throw new LocalPathIsNotServerPathException("LocalPath = " + localPath);
	}

	/**
	 * 
	 * 
	 * @param path
	 * @return the string of permission of the file.
	 * @throws LocalPathIsNotServerPathException
	 */
	public String getPathDescriptionLongFormat(Path path) throws LocalPathIsNotServerPathException {
		try {
			return this.ftpFileAccessManager.getFileAccess(path).getPathDescriptionLongFormat();
		} catch (PathNotPutException e) {
			ErrorManager.writeError(e);
			return null;
		}
	}

	public boolean isRoot(FTPClientConnection ftpClientConnection) {
		try {

			if (FTPFileGroupAssignment.ROOT_CLIENT.equals(ftpClientConnection.getPseudo()))
				return true;

			return this.ftpFileGroupAssignment.memberIsInGroup(ftpClientConnection.getPseudo(),
					FTPFileGroupAssignment.ROOT_GROUP);
		} catch (GroupDoesNotExistsException | MembreUnknownException e) {
			ErrorManager.writeError(e);
			return false;
		}
	}

	public boolean readAccess(Path path, FTPClientConnection ftpClientConnection)
			throws LocalPathIsNotServerPathException {

		// System.out.println("READ PATH = " + path);

		if (this.isRoot(ftpClientConnection))
			return true;

		if (PATH_LOCALISATION_FTP_DATA.equals(path))
			return false;

		if (PATH_WORKSPACE.equals(path))
			return false;

		if (PATH_USERS_DIRECTORY.equals(path))
			return true;

		FTPFileAccess ftpFileAccess = null;
		try {
			ftpFileAccess = this.ftpFileAccessManager.getFileAccess(path);
		} catch (PathNotPutException e) {
			ErrorManager.writeError(e);
			return false;
		}

		String pseudo = ftpClientConnection.getPseudo();

		FileAccess permissionFile = null;

		if (ftpFileAccess.getOwner().equals(pseudo)) {
			permissionFile = ftpFileAccess.getOwnerPermissionFile();
		} else {
			try {
				if (this.ftpFileGroupAssignment.memberIsInGroup(pseudo, ftpFileAccess.getGroup())) {
					permissionFile = ftpFileAccess.getGroupPermissionFile();
				} else {
					permissionFile = ftpFileAccess.getOtherPermissionFile();
				}
			} catch (GroupDoesNotExistsException e) {
				ErrorManager.writeError(e);
				return false;
			} catch (MembreUnknownException e) {
				ErrorManager.writeError(e);
				return false;
			}
		}

		if (permissionFile == FileAccess.READ || permissionFile == FileAccess.READ_WRITE
				|| permissionFile == FileAccess.READ_EXECUTE || permissionFile == FileAccess.READ_WRITE_EXECUTE) {
			return true;
		} else {
			return false;
		}
	}

	public boolean writeAccess(Path path, FTPClientConnection ftpClientConnection)
			throws LocalPathIsNotServerPathException {

		// System.out.println("WRITE PATH = " + path);

		if (this.isRoot(ftpClientConnection))
			return true;

		if (PATH_LOCALISATION_FTP_DATA.equals(path))
			return false;

		if (PATH_WORKSPACE.equals(path))
			return false;

		if (PATH_USERS_DIRECTORY.equals(path))
			return false;

		FTPFileAccess ftpFileAccess;
		try {
			ftpFileAccess = this.ftpFileAccessManager.getFileAccess(path);
		} catch (PathNotPutException e) {
			ErrorManager.writeError(e);
			return false;
		}

		String pseudo = ftpClientConnection.getPseudo();

		FileAccess permissionFile = null;

		if (ftpFileAccess.getOwner().equals(pseudo)) {
			permissionFile = ftpFileAccess.getOwnerPermissionFile();
		} else {
			try {
				if (this.ftpFileGroupAssignment.memberIsInGroup(pseudo, ftpFileAccess.getGroup())) {
					permissionFile = ftpFileAccess.getGroupPermissionFile();
				} else {
					permissionFile = ftpFileAccess.getOtherPermissionFile();
				}
			} catch (GroupDoesNotExistsException e) {
				ErrorManager.writeError(e);
				return false;
			} catch (MembreUnknownException e) {
				ErrorManager.writeError(e);
				return false;
			}
		}

		if (permissionFile == FileAccess.WRITE || permissionFile == FileAccess.READ_WRITE
				|| permissionFile == FileAccess.WRITE_EXECUTE || permissionFile == FileAccess.READ_WRITE_EXECUTE) {
			return true;
		} else {
			return false;
		}
	}

	public boolean executeAccess(Path path, FTPClientConnection ftpClientConnection)
			throws LocalPathIsNotServerPathException {

		// System.out.println("EXECUTE PATH = " + path);

		if (this.isRoot(ftpClientConnection))
			return true;

		if (PATH_LOCALISATION_FTP_DATA.equals(path))
			return false;

		if (PATH_WORKSPACE.equals(path))
			return true;

		if (PATH_USERS_DIRECTORY.equals(path))
			return true;

		FTPFileAccess ftpFileAccess;
		try {
			ftpFileAccess = this.ftpFileAccessManager.getFileAccess(path);
		} catch (PathNotPutException e) {
			ErrorManager.writeError(e);
			return false;
		}

		String pseudo = ftpClientConnection.getPseudo();

		FileAccess permissionFile = null;

		if (ftpFileAccess.getOwner().equals(pseudo)) {
			permissionFile = ftpFileAccess.getOwnerPermissionFile();
		} else {
			try {
				if (this.ftpFileGroupAssignment.memberIsInGroup(pseudo, ftpFileAccess.getGroup())) {
					permissionFile = ftpFileAccess.getGroupPermissionFile();
				} else {
					permissionFile = ftpFileAccess.getOtherPermissionFile();
				}
			} catch (GroupDoesNotExistsException e) {
				ErrorManager.writeError(e);
				return false;
			} catch (MembreUnknownException e) {
				ErrorManager.writeError(e);
				return false;
			}
		}

		if (permissionFile == FileAccess.EXECUTE || permissionFile == FileAccess.READ_EXECUTE
				|| permissionFile == FileAccess.WRITE_EXECUTE || permissionFile == FileAccess.READ_WRITE_EXECUTE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param share
	 * @return the path of the user root directory if it exists, else return null.
	 */
	public Path getLocalUserWorkspaceDirectoryPath(String userName) {

		if (userName == null)
			return null;

		Path pathUserWorkspaceDirectory = Paths.get(this.STRING_PATH_USERS_DIRECTORY, userName);

		if (Files.exists(pathUserWorkspaceDirectory)) {
			return pathUserWorkspaceDirectory;
		} else {
			return null;
		}
	}

	public Path getRemoteUserWorkspaceDirectoryPath(String userName) {
		try {
			return this.convertToRemotePath(this.getLocalUserWorkspaceDirectoryPath(userName));
		} catch (LocalPathIsNotServerPathException e) {
			ErrorManager.writeError(e);
			return null;
		}
	}

	/**
	 * Create the root directory for the user.
	 * 
	 * @param client
	 * @throws IOException
	 * @throws RootUserDirectoryAlreadyExistsException
	 */
	public void createWorkspaceDirectory(String directoryUser)
			throws IOException, RootUserDirectoryAlreadyExistsException {
		String absolutePathUserDirectory = this.STRING_PATH_USERS_DIRECTORY + FILE_SEPARATOR + directoryUser;

		Path pathUserDirectory = Paths.get(absolutePathUserDirectory);

//		this.ftpLockManager.lockOnWithParent(pathUserDirectory);

		boolean exists = Files.exists(pathUserDirectory);

		if (!exists) {
			Files.createDirectory(pathUserDirectory);

			try {
				this.ftpFileAccessManager.putFile(pathUserDirectory, new FTPFileAccess(pathUserDirectory, directoryUser,
						directoryUser, FileAccess.READ_WRITE_EXECUTE, FileAccess.READ_EXECUTE, FileAccess.NOTHING));
			} catch (LocalPathIsNotServerPathException e) {
				ErrorManager.writeError(e);
				// Nerver append.
			} catch (PathNotPutException e) {
				ErrorManager.writeError(e);
			}

//			this.ftpLockManager.unlockOnWithParent(pathUserDirectory);
		} else {
//			this.ftpLockManager.unlockOnWithParent(pathUserDirectory);
			throw new RootUserDirectoryAlreadyExistsException(directoryUser);
		}
	}

	/**
	 * <p>
	 * Delete all elements in the root share directory.
	 * </p>
	 * 
	 * <p>
	 * <b>Can throw a fatal error exception.</b>
	 * </p>
	 * 
	 * @param client
	 * @throws UserHasNotWorkspaceDirectoryException
	 * @throws IOException
	 * @throws ClearWorkspaceDirectoryFailedException
	 * @throws FatalErrorException
	 */
	public void clearWorkspaceDirectory(String client) throws UserHasNotWorkspaceDirectoryException, IOException,
			ClearWorkspaceDirectoryFailedException, FatalErrorException {
		Path pathUserWorkspaceDirectory = this.getLocalUserWorkspaceDirectoryPath(client);

//		try {
//			this.ftpLockManager.lockOnDirectoryRecursive(pathUserWorkspaceDirectory);
//		} catch (LockOnDirectoryRecursiveFailedException | PathIsNotDirectoryException e) {
//			ErrorManager.writeError(e);
//			throw new ClearWorkspaceDirectoryFailedException(pathUserWorkspaceDirectory);
//		}

		try {

			if (pathUserWorkspaceDirectory != null) {
				File fileRootUserDirectory = pathUserWorkspaceDirectory.toFile();
				String[] tabSubFile = fileRootUserDirectory.list();

				for (int i = 0; i < tabSubFile.length; i++) {
					Path path = Paths.get(pathUserWorkspaceDirectory.toString() + FILE_SEPARATOR + tabSubFile[i]);
					this.deleteDirectoryRecursive(path);
				}

			} else {
				throw new UserHasNotWorkspaceDirectoryException(client);
			}
		} finally {
//			try {
//				this.ftpLockManager.unlockOnDirectoryRecursive(pathUserWorkspaceDirectory);
//			} catch (PathIsNotDirectoryException | UnLockOnDirectoryRecursiveFailedException e) {
//				ErrorManager.writeError(e);
//				throw new FatalErrorException();
//			}
		}
	}

	public void createDirectory(Path directoryPath, FTPClientConnection ftpClientConnection)
			throws IOException, NoAccessToThisFileException, LocalPathIsNotServerPathException {
		try {
			if (this.writeAccess(directoryPath.getParent(), ftpClientConnection)) {

//				this.ftpLockManager.lockOnWithParent(directoryPath);

				Files.createDirectory(directoryPath);

				try {
					this.ftpFileAccessManager.putFile(directoryPath,
							new FTPFileAccess(directoryPath, ftpClientConnection.getPseudo(),
									ftpClientConnection.getPseudo(), DEFAULT_FILE_ACCESS_USER_DIRECTORY[0],
									DEFAULT_FILE_ACCESS_USER_DIRECTORY[1], DEFAULT_FILE_ACCESS_USER_DIRECTORY[2]));
				} catch (PathNotPutException e) {
					ErrorManager.writeError(e);
				}
			} else {
				throw new NoAccessToThisFileException("Path = " + directoryPath.getParent());
			}

		} finally {
//			this.ftpLockManager.unlockOnWithParent(directoryPath);
		}
	}

	public void deleteDirectory(Path directoryPath, FTPClientConnection ftpClientConnection) throws IOException,
			PathIsNotDirectoryException, NoAccessToThisFileException, LocalPathIsNotServerPathException {

		try {
			if (this.writeAccess(directoryPath.getParent(), ftpClientConnection)) {

//				this.ftpLockManager.lockOnWithParent(directoryPath);

				if (Files.isDirectory(directoryPath)) {
					
					this.deleteDirectoryRecursive(directoryPath);
					
				} else {
					throw new PathIsNotDirectoryException(directoryPath);
				}
			} else {
				throw new NoAccessToThisFileException("Path = " + directoryPath.getParent());
			}

			System.out.println("Fin delete RMD");
		} finally {
//			this.ftpLockManager.unlockOnWithParent(directoryPath);
		}

	}

	public void createFile(Path filePath, FTPClientConnection ftpClientConnection)
			throws IOException, LocalPathIsNotServerPathException, NoAccessToThisFileException {
		try {

			if (this.writeAccess(filePath.getParent(), ftpClientConnection)) {
//				this.ftpLockManager.lockOnWithParent(filePath);

				Files.createFile(filePath);

				try {
					this.ftpFileAccessManager.putFile(filePath,
							new FTPFileAccess(filePath, ftpClientConnection.getPseudo(),
									ftpClientConnection.getPseudo(), DEFAULT_FILE_ACCESS_USER_DIRECTORY[0],
									DEFAULT_FILE_ACCESS_USER_DIRECTORY[1], DEFAULT_FILE_ACCESS_USER_DIRECTORY[2]));
				} catch (PathNotPutException e) {
					ErrorManager.writeError(e);
				}
			} else {
				throw new NoAccessToThisFileException("Path = " + filePath.getParent());
			}

		} finally {
//			this.ftpLockManager.unlockOnWithParent(filePath);
		}
	}

	public void deleteFile(Path pathToDelete, FTPClientConnection ftpClientConnection)
			throws IOException, PathIsNotFileException, LocalPathIsNotServerPathException, NoAccessToThisFileException {

		try {

			if (this.writeAccess(pathToDelete.getParent(), ftpClientConnection)) {

//				this.ftpLockManager.lockOnWithParent(pathToDelete);

				if (!Files.isDirectory(pathToDelete)) {
					// Files.deleteIfExists(pathToDelete);

					try {
						this.ftpFileAccessManager.removeFile(pathToDelete);

						Files.deleteIfExists(pathToDelete);
					} catch (PathNotPutException e) {
						ErrorManager.writeError(e);
					}
				} else
					throw new PathIsNotFileException(pathToDelete);
			} else {
				throw new NoAccessToThisFileException("Path = " + pathToDelete.getParent());
			}

		} finally {
//			this.ftpLockManager.unlockOnWithParent(pathToDelete);
		}
	}

	/**
	 * Rename the file for the user.
	 * 
	 * @param pathToRenamed
	 * @param newName
	 * @throws NameForFileNotCorrectException
	 * @throws NoAccessToThisFileException
	 * @throws LocalPathIsNotServerPathException
	 */
	public void renameFile(Path pathToRenamed, String newName, FTPClientConnection ftpClientConnection)
			throws NameForFileNotCorrectException, LocalPathIsNotServerPathException, NoAccessToThisFileException {

		if (this.writeAccess(pathToRenamed.getParent(), ftpClientConnection)) {

			if (newName == null || newName.isEmpty()) {
				throw new NameForFileNotCorrectException(newName);
			}

//			this.ftpLockManager.lockOnWithParent(pathToRenamed);

			File file = pathToRenamed.toFile();

			String newPathName = pathToRenamed.getParent().toString() + FILE_SEPARATOR + newName;

			try {
				this.ftpFileAccessManager.renameFile(pathToRenamed, newName);

				file.renameTo(new File(newPathName));
			} catch (PathNotPutException e) {
				ErrorManager.writeError(e);
			}

//			this.ftpLockManager.unlockOnWithParent(pathToRenamed);
		} else {
			throw new NoAccessToThisFileException("Path = " + pathToRenamed.getParent());
		}
	}

	/**
	 * 
	 * @param rootPath
	 * @return the list of sub file if rootPath is a directory, else if rootPath is
	 *         a File or does not exist, return null.
	 * @throws LocalPathIsNotServerPathException
	 * @throws NoAccessToThisFileException
	 */
	public List<Path> getListSubFile(Path rootPath, FTPClientConnection ftpClientConnection)
			throws LocalPathIsNotServerPathException, NoAccessToThisFileException {
		Vector<Path> listPath = new Vector<>();

		if (Files.exists(rootPath)) {
			if (Files.isDirectory(rootPath)) {

				if (this.readAccess(rootPath, ftpClientConnection)) {
					try (DirectoryStream<Path> listing = Files.newDirectoryStream(rootPath)) {

						for (Path path : listing) {
							listPath.add(path);
						}

						return listPath;

					} catch (IOException e) {
						ErrorManager.writeError(e);
						return null;
					}
				} else {
					if (this.executeAccess(rootPath, ftpClientConnection)) {
						return null;
					} else {
						throw new NoAccessToThisFileException("Path = " + rootPath);
					}
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param rootPath
	 * @return a list of all file sub file. If the rootPath is a file, return just
	 *         the file. If rootPath does not exist return null.
	 */
	public List<Path> getSubPathRecursiveFrom(Path rootPath) {
		Vector<Path> listPath = new Vector<>();

		if (Files.exists(rootPath)) {
			if (Files.isDirectory(rootPath)) {

				try (DirectoryStream<Path> listing = Files.newDirectoryStream(rootPath)) {

					for (Path path : listing) {
						listPath.add(path);
						listPath.addAll(this.getSubPathRecursiveFrom(path));
					}

					return listPath;

				} catch (IOException e) {
					ErrorManager.writeError(e);
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean createGroup(String group) {
		return this.ftpFileGroupAssignment.createGroup(group);
	}

	public void refresh() throws FatalErrorException {
		this.ftpFileGroupAssignment.refresh();
	}

	public void chargeFromFileAccess() throws IOException {
		if (Files.exists(this.PATH_FILE_ACCESS_FILE))
			ftpFileAccessManager.chargeFromFileAccess(this.PATH_FILE_ACCESS_FILE);
	}

	public void saveFileAccess() throws IOException {
		ftpFileAccessManager.saveFileAccess(this.PATH_FILE_ACCESS_FILE);
	}

	public boolean putFile(Path path, FTPFileAccess ftpFileAccess) throws LocalPathIsNotServerPathException {
		try {
			return ftpFileAccessManager.putFile(path, ftpFileAccess);
		} catch (PathNotPutException e) {
			ErrorManager.writeError(e);
			return false;
		}
	}

	// Getters and Setters.

	// Private classes.

	/**
	 * <p>
	 * The lock manager which allow to lock and unlock on a Path.
	 * </p>
	 * <p>
	 * If you want that your FTPManager works, you should lock on a absolute path
	 * and not just a not complete path.
	 * </p>
	 * <p>
	 * For example the Path : /home/myUserName/Documents/text.txt can considered
	 * same than the Path ~/Documents/text.txt, but in this system is two different
	 * paths and so two different locks.
	 * </p>
	 * 
	 * @author Callimard
	 *
	 */
//	private class FTPLockManager extends LockManager<Path> {
//
//		// Constants.
//
//		// Variables.
//
//		// Constructors.
//
//		@SuppressWarnings("unused")
//		public FTPLockManager() {
//			super();
//		}
//
//		public FTPLockManager(int capacity) {
//			super(capacity);
//		}
//
//		// Methods.
//
//		/**
//		 * Re lock all file which are in the save list. This method is use when an error
//		 * was occurred in a method of unlock.
//		 * 
//		 * @param saveList
//		 */
//		private void lockSaveList(List<Path> saveList) {
//			for (Path path : saveList) {
//				this.lockOn(path);
//			}
//		}
//
//		/**
//		 * Re unlock all file which are in the save list. This method is use when an
//		 * error was occurred in a method of lock.
//		 * 
//		 * @param saveList
//		 */
//		private void unlockSaveList(List<Path> saveList) {
//			for (Path path : saveList) {
//				this.unlockOn(path);
//			}
//		}
//
//		/**
//		 * <p>
//		 * Create a lock for the path and also the parent directory of the path.
//		 * </p>
//		 * <p>
//		 * The order to take lock is very important, and so we begin to lock the parent
//		 * directory before take the path directory.
//		 * </p>
//		 * 
//		 * <p>
//		 * <b>/!\It's recommended to use absolute path/!\</b>
//		 * </p>
//		 * 
//		 * @param path
//		 */
//		public void lockOnWithParent(Path path) {
//			Path pathParentDirectory = path.getParent();
//
//			this.lockOn(pathParentDirectory);
//			this.lockOn(path);
//		}
//
//		/**
//		 * <p>
//		 * UnLock the path and the directory parent path.
//		 * </p>
//		 * <p>
//		 * The order use to unlock is the path before the parent path.
//		 * </p>
//		 * 
//		 * <p>
//		 * <b>/!\It's recommended to use absolute path/!\</b>
//		 * </p>
//		 * 
//		 * @param path
//		 */
//		public void unlockOnWithParent(Path path) {
//			Path pathParentDirectory = path.getParent();
//
//			this.unlockOn(path);
//			this.unlockOn(pathParentDirectory);
//		}
//
//		/**
//		 * <p>
//		 * Lock on the directory and all files (directory and file) which are in the
//		 * directory
//		 * </p>
//		 * 
//		 * <p>
//		 * <b>/!\It's recommended to use absolute path/!\</b>
//		 * </p>
//		 * 
//		 * @param pathDirectory
//		 * @throws LockOnDirectoryRecursiveFailedException
//		 * @throws PathIsNotDirectoryException
//		 */
//		@SuppressWarnings("resource")
//		public void lockOnDirectoryRecursive(Path pathDirectory)
//				throws LockOnDirectoryRecursiveFailedException, PathIsNotDirectoryException {
//			if (Files.isDirectory(pathDirectory)) {
//				ArrayList<Path> saveList = new ArrayList<>();
//
//				this.lockOn(pathDirectory);
//
//				Stream<Path> stream = null;
//				try {
//					stream = Files.list(pathDirectory);
//				} catch (IOException e) {
//					ErrorManager.writeError(e);
//					this.unlockOn(pathDirectory);
//					throw new LockOnDirectoryRecursiveFailedException(pathDirectory);
//				}
//
//				for (Iterator<Path> iterator = stream.iterator(); iterator.hasNext();) {
//					Path path = iterator.next();
//					saveList.add(path);
//
//					if (Files.isDirectory(path)) {
//						try {
//							this.lockOnDirectoryRecursive(path);
//						} catch (LockOnDirectoryRecursiveFailedException e) {
//							ErrorManager.writeError(e);
//							this.unlockSaveList(saveList);
//							this.unlockOn(pathDirectory);
//
//							throw new LockOnDirectoryRecursiveFailedException(path);
//						}
//					} else {
//						this.lockOn(path);
//					}
//				}
//			} else {
//				throw new PathIsNotDirectoryException(pathDirectory);
//			}
//
//		}
//
//		/**
//		 * <p>
//		 * Unlock the directory and all files and directory which are in the directory.
//		 * </p>
//		 * <p>
//		 * We unlock in first the file and directory which are in the directory before
//		 * unlock the directory.
//		 * </p>
//		 * 
//		 * <p>
//		 * <b>/!\It's recommended to use absolute path/!\</b>
//		 * </p>
//		 * 
//		 * @param pathDirectory
//		 * @throws PathIsNotDirectoryException
//		 * @throws UnLockOnDirectoryRecursiveFailedException
//		 */
//		@SuppressWarnings("resource")
//		public void unlockOnDirectoryRecursive(Path pathDirectory)
//				throws PathIsNotDirectoryException, UnLockOnDirectoryRecursiveFailedException {
//			if (Files.isDirectory(pathDirectory)) {
//				ArrayList<Path> saveList = new ArrayList<>();
//
//				Stream<Path> stream = null;
//				try {
//					stream = Files.list(pathDirectory);
//				} catch (IOException e) {
//					ErrorManager.writeError(e);
//					throw new UnLockOnDirectoryRecursiveFailedException(pathDirectory);
//				}
//
//				for (Iterator<Path> iterator = stream.iterator(); iterator.hasNext();) {
//					Path path = iterator.next();
//
//					saveList.add(path);
//
//					if (Files.isDirectory(path)) {
//						try {
//							this.unlockOnDirectoryRecursive(pathDirectory);
//						} catch (UnLockOnDirectoryRecursiveFailedException e) {
//							ErrorManager.writeError(e);
//							this.lockSaveList(saveList);
//
//							throw new UnLockOnDirectoryRecursiveFailedException(path);
//						}
//					} else {
//						this.unlockOn(path);
//					}
//				}
//
//				this.unlockOn(pathDirectory);
//			} else {
//				throw new PathIsNotDirectoryException(pathDirectory);
//			}
//		}
//
//	}

}
