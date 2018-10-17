package server.ftp.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import server.ftp.exception.file_access_manager.PathNotPutException;
import server.ftp.exception.file_manager.LocalPathIsNotServerPathException;
import server.util.ErrorManager;

public class FTPFileAccessManager {

	// Constants.

	public static final String PATH_ACCESS_SEPARATOR = ",";
	public static final String PATH_SEPARATOR = ";";

	// Variables.

	private final TreeNode root;

	private final FTPFileManager ftpFileManager;

	private final int nbPathNameInRootPath;

	// Constructors.

	public FTPFileAccessManager(FTPFileManager ftpFileManager) {
		this.ftpFileManager = ftpFileManager;

		this.root = new TreeNode(this.ftpFileManager.STRING_PATH_LOCALISATION_FTP_DATA,
				this.ftpFileManager.PATH_LOCALISATION_FTP_DATA_ACCESS, null);

		this.nbPathNameInRootPath = this.ftpFileManager.PATH_LOCALISATION_FTP_DATA.getNameCount();
	}

	// Methods.

	/**
	 * <p>
	 * Put the file in the {@link FTPFileAccessManager}. If the file is already in
	 * the map, return false.
	 * </p>
	 *
	 * 
	 * <p>
	 * This method accept only path which has been convert to loacl path, if you
	 * enter a path which cannot exist on the machine, throw an exception. The path
	 * must be a path which is in the file zone of the FTP server. (In others words,
	 * if it's starts with {@link FTPFileManager#PATH_LOCALISATION_FTP_DATA}).
	 * </p>
	 * 
	 * <p>
	 * The file must exists to be puted.
	 * </p>
	 * 
	 * @param path
	 * @param ftpFileAccess
	 * 
	 * @return true if the file has been put, else false. We can't put a file
	 *         already in the FTPFileAccessManager.
	 * 
	 * @throws LocalPathIsNotServerPathException
	 * @throws PathNotPutException
	 */
	public boolean putFile(Path path, FTPFileAccess ftpFileAccess)
			throws LocalPathIsNotServerPathException, PathNotPutException {
		if (this.ftpFileManager.isServerFile(path)) {
			TreeNode current = this.root;

			boolean isDirectory = Files.isDirectory(path);

			boolean notIn = false;

			for (int i = this.nbPathNameInRootPath; i < path.getNameCount(); i++) {
				String name = path.getName(i).toString();
				TreeNode next = current.getSubTreeNode(name, true);

				if (next == null && i < path.getNameCount() - 1)
					throw new PathNotPutException("Path = " + path + " PathName not put = " + name);

				if (next == null) {
					next = new TreeNode(name, ftpFileAccess, current);
					current.add(next, isDirectory);
					notIn = true;
				}

				current = next;
			}

			return notIn;

		} else
			throw new LocalPathIsNotServerPathException("LocalPath = " + path);
	}

	/**
	 * 
	 * <p>
	 * The file must exists to be puted.
	 * </p>
	 * 
	 * @param path
	 * @throws LocalPathIsNotServerPathException
	 * @throws PathNotPutException
	 */
	public void removeFile(Path path) throws LocalPathIsNotServerPathException, PathNotPutException {
		if (this.ftpFileManager.isServerFile(path)) {
			TreeNode current = this.root;

			boolean isDirectory = Files.isDirectory(path);

			for (int i = this.nbPathNameInRootPath; i < path.getNameCount(); i++) {
				String name = path.getName(i).toString();

				TreeNode next = null;

				if (i < path.getNameCount() - 1)
					next = current.getSubTreeNode(name, true);
				else
					next = current.getSubTreeNode(name, isDirectory);

				if (next == null && !isDirectory)
					throw new PathNotPutException("Path = " + path + " PathName not put = " + name);
				else {
					if (next != null) {
						current = next;
					}
				}
			}

			TreeNode parent = current.parent;

			if (parent != null) {
				parent.remove(current.pathName, isDirectory);
			}

		} else
			throw new LocalPathIsNotServerPathException("LocalPath = " + path);
	}

	public void renameFile(Path path, String newName) throws PathNotPutException {
		TreeNode current = this.root;

		boolean isDirectory = Files.isDirectory(path);

		for (int i = this.nbPathNameInRootPath; i < path.getNameCount(); i++) {
			String name = path.getName(i).toString();

			TreeNode next = null;

			if (i < path.getNameCount() - 1) {
				next = current.getSubTreeNode(name, true);
			} else {
				next = current.getSubTreeNode(name, isDirectory);
			}

			if (next == null && !isDirectory)
				throw new PathNotPutException("Path = " + path + " PathName not put = " + name);
			else {
				if (next != null) {
					current = next;
				}
			}
		}

		current.parent.remove(current.pathName, isDirectory);
		current.pathName = newName;
		current.parent.add(current, isDirectory);

		FTPFileAccess ftpFileAccess = current.ftpFileAccess;
		current.ftpFileAccess = new SpecialFTPFileAccess(current, ftpFileAccess.getOwner(), ftpFileAccess.getGroup(),
				ftpFileAccess.getOwnerPermissionFile(), ftpFileAccess.getGroupPermissionFile(),
				ftpFileAccess.getOtherPermissionFile());
	}

	/**
	 * 
	 * <p>
	 * Return the FTPFileAccess of the path. If the path is not store in the
	 * FTPFileAccessManager, return null.
	 * </p>
	 * <p>
	 * This method accept only path which has been convert to loacl path, if you
	 * enter a path which cannot exist on the machine, throw an exception. The path
	 * must be a path which is in the file zone of the FTP server. (In others words,
	 * if it's starts with {@link FTPFileManager#PATH_LOCALISATION_FTP_DATA}).
	 * </p>
	 * 
	 * @param path
	 * @return the FTPFileAccess of the path, if we don't know the access of the
	 *         file return null.
	 * @throws LocalPathIsNotServerPathException
	 * @throws PathNotPutException
	 */
	public FTPFileAccess getFileAccess(Path path) throws LocalPathIsNotServerPathException, PathNotPutException {
		if (this.ftpFileManager.isServerFile(path)) {
			TreeNode current = this.root;

			boolean isDirectory = Files.isDirectory(path);

			for (int i = this.nbPathNameInRootPath; i < path.getNameCount(); i++) {
				String name = path.getName(i).toString();

				TreeNode next = null;

				if (i < path.getNameCount() - 1)
					next = current.getSubTreeNode(name, true);
				else
					next = current.getSubTreeNode(name, isDirectory);

				if (next == null && !isDirectory)
					throw new PathNotPutException("Path = " + path + " PathName not put = " + name);
				else {
					if (next != null) {
						current = next;
					}
				}
			}

			return current.ftpFileAccess;
		} else
			throw new LocalPathIsNotServerPathException("LocalPath = " + path);
	}

	public void chargeFromFileAccess(Path pathFileAccess) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(pathFileAccess)) {
			StringBuilder builder = new StringBuilder();

			String stringRead = null;
			while ((stringRead = reader.readLine()) != null)
				builder.append(stringRead);

			StringTokenizer pathSeparator = new StringTokenizer(builder.toString(), PATH_SEPARATOR);

			while (pathSeparator.hasMoreTokens()) {
				String description = pathSeparator.nextToken();

				try {

					FTPFileAccess ftpFileAccess = FTPFileAccess.parseFromSaveFormat(description);

					try {
						this.putFile(ftpFileAccess.getPath(), ftpFileAccess);
					} catch (PathNotPutException e) {
						System.err.println("Erreur chelou");
						ErrorManager.writeError(e);
					}

				} catch (LocalPathIsNotServerPathException e) {
					ErrorManager.writeError(e);
				}
			}
		}
	}

	private void saveFileAccess(TreeNode treeNode, BufferedWriter writer) throws IOException {
		writer.write(treeNode.ftpFileAccess.toSaveFormat() + PATH_SEPARATOR);

		Set<Entry<String, TreeNode>> set = treeNode.hashTreeNode.entrySet();

		for (Entry<String, TreeNode> entry : set) {
			this.saveFileAccess(entry.getValue(), writer);
		}
	}

	public void saveFileAccess(Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {

			writer.write(this.root.ftpFileAccess.toSaveFormat() + PATH_SEPARATOR);

			Set<Entry<String, TreeNode>> set = this.root.hashTreeNode.entrySet();

			for (Entry<String, TreeNode> entry : set) {
				this.saveFileAccess(entry.getValue(), writer);
			}

			writer.flush();
		}

	}

	// Getters and Setters.

	public FTPFileManager getFTPFileManager() {
		return this.ftpFileManager;
	}

	// Private static class.

	private static class TreeNode {

		// Constants.

		private static final String STRING_DIRECTORY = "d";
		private static final String STRING_FILE = "f";

		// Variables.

		private TreeNode parent;

		private ConcurrentHashMap<String, TreeNode> hashTreeNode = new ConcurrentHashMap<>();

		private String pathName;
		private SpecialFTPFileAccess ftpFileAccess;

		// Constants.

		public TreeNode(String pathName, FTPFileAccess ftpFileAccess, TreeNode parent) {
			this.pathName = pathName;
			this.ftpFileAccess = new SpecialFTPFileAccess(ftpFileAccess, this);

			this.parent = parent;
		}

		// Methods.

		public static String convertToCorrectKey(String name, boolean isDirectory) {
			if (isDirectory) {
				return STRING_DIRECTORY + name;
			} else {
				return STRING_FILE + name;
			}
		}

		public Path getTotalPath() {

			if (this.parent != null)
				return Paths.get(this.parent.getTotalPath().toString(), this.pathName);
			else
				return Paths.get(this.pathName);
		}

		public TreeNode add(TreeNode treeNode, boolean isDirectory) {
			String key = convertToCorrectKey(treeNode.pathName, isDirectory);

			return this.hashTreeNode.put(key, treeNode);
		}

		public TreeNode remove(String pathName, boolean isDirectory) {
			String key = convertToCorrectKey(pathName, isDirectory);

			return this.hashTreeNode.remove(key);
		}

		public TreeNode getSubTreeNode(String pathName, boolean isDirectory) {
			String key = convertToCorrectKey(pathName, isDirectory);

			return this.hashTreeNode.get(key);
		}

	}

	private static class SpecialFTPFileAccess extends FTPFileAccess {

		// Variables.

		private TreeNode treeNode;

		// Constructors.

		public SpecialFTPFileAccess(TreeNode treeNode, String owner, String group, FileAccess ownerPermission,
				FileAccess groupPermission, FileAccess otherPermission) {
			super(treeNode.getTotalPath(), owner, group, ownerPermission, groupPermission, otherPermission);

			this.treeNode = treeNode;
		}

		public SpecialFTPFileAccess(FTPFileAccess ftpFileAccess, TreeNode treeNode) {
			this(treeNode, ftpFileAccess.getOwner(), ftpFileAccess.getGroup(), ftpFileAccess.getOwnerPermissionFile(),
					ftpFileAccess.getGroupPermissionFile(), ftpFileAccess.getOtherPermissionFile());

		}

		// Methods.

		private void updatePath() {
			this.setPath(treeNode.getTotalPath());
		}

		@Override
		public String toSaveFormat() {

			this.updatePath();

			return super.toSaveFormat();
		}

		@Override
		public String getPathDescriptionLongFormat() {

			this.updatePath();

			return super.getPathDescriptionLongFormat();
		}

		@Override
		public Path getPath() {

			this.updatePath();

			return super.getPath();
		}

	}

}
