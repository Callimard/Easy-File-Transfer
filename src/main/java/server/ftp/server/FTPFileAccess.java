package server.ftp.server;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import server.ftp.exception.WrongPathDescriptionFormatException;

public class FTPFileAccess {

	// Constants.

	public static final FileAccess[] DEFAULT_ACCESS = { FileAccess.READ_WRITE_EXECUTE, FileAccess.NOTHING,
			FileAccess.NOTHING };

	public static final int NUMBER_PERMISSIONS = 3;

	public static final int INDEX_OWNER_PERMISSION = 0;

	public static final int INDEX_GROUP_PERMISSION = 1;

	public static final int INDEX_OTHER_PERMISSION = 2;

	public static final int MINIMUM_TOKENS_IN_DESCRIPTION = 6;

	// Variables.

	private Path path;

	private String owner;

	private String group;

	private FileAccess[] globalAccess = DEFAULT_ACCESS;

	// Constructors.

	public FTPFileAccess(Path path, String owner, String group, FileAccess ownerPermission, FileAccess groupPermission,
			FileAccess otherPermission) {
		if (path != null)
			this.path = path;
		else
			throw new NullPointerException("Path null");

		if (owner != null)
			this.owner = owner;
		else
			this.owner = FTPFileGroupAssignment.ROOT_CLIENT;

		if (group != null)
			this.group = group;
		else
			this.group = FTPFileGroupAssignment.ROOT_GROUP;

		this.globalAccess = new FileAccess[NUMBER_PERMISSIONS];

		if (otherPermission != null)
			this.globalAccess[INDEX_OWNER_PERMISSION] = ownerPermission;
		else
			this.globalAccess[INDEX_OWNER_PERMISSION] = FileAccess.NOTHING;

		if (groupPermission != null)
			this.globalAccess[INDEX_GROUP_PERMISSION] = groupPermission;
		else
			this.globalAccess[INDEX_GROUP_PERMISSION] = FileAccess.NOTHING;

		if (otherPermission != null)
			this.globalAccess[INDEX_OTHER_PERMISSION] = otherPermission;
		else
			this.globalAccess[INDEX_OTHER_PERMISSION] = FileAccess.NOTHING;
	}

	// Methods.

	public static FTPFileAccess parseFromLongDescribe(Path parentPath, String description)
			throws WrongPathDescriptionFormatException {

		if (parentPath == null)
			throw new NullPointerException("Current path null");

		StringTokenizer separator = new StringTokenizer(description, " ");

		if (separator.countTokens() >= MINIMUM_TOKENS_IN_DESCRIPTION) {

			String permissions = separator.nextToken();

			// NB DIRECTORY.
			separator.nextToken();

			String owner = separator.nextToken();
			String group = separator.nextToken();

			// SIZE.
			separator.nextToken();

			StringBuilder nameFileBuilder = new StringBuilder(separator.nextToken() + " ");
			String nameFile = "NO_NAME";

			while (separator.hasMoreTokens()) {
				if (separator.countTokens() > 1)
					nameFileBuilder.append(separator.nextToken() + " ");
				else
					nameFileBuilder.append(separator.nextToken());
			}

			nameFile = nameFileBuilder.toString();

			Path path = Paths.get(parentPath.toString(), nameFile);

			String filePermissions = permissions.substring(1, permissions.length());

			String stringPermissionOwner = filePermissions.substring(0, 3);
			String stringPermissionGroup = filePermissions.substring(3, 6);
			String stringPermissionOther = filePermissions.substring(6, 9);

			FileAccess permissionFileOwner = FileAccess.getPermissionFile(stringPermissionOwner);
			FileAccess permissionFileGroup = FileAccess.getPermissionFile(stringPermissionGroup);
			FileAccess permissionFileOther = FileAccess.getPermissionFile(stringPermissionOther);

			return new FTPFileAccess(path, owner, group, permissionFileOwner, permissionFileGroup, permissionFileOther);

		} else {
			throw new WrongPathDescriptionFormatException("Description = " + description);
		}
	}

	public static FTPFileAccess parseFromSaveFormat(String saveFormat) {
		StringTokenizer pathSeparator = new StringTokenizer(saveFormat, FTPFileAccessManager.PATH_ACCESS_SEPARATOR);

		Path path = Paths.get(pathSeparator.nextToken());

		String information = pathSeparator.nextToken();

		StringTokenizer informationSeparator = new StringTokenizer(information, " ");

		String access = informationSeparator.nextToken();
		String owner = informationSeparator.nextToken();
		String group = informationSeparator.nextToken();

		String stringPermissionOwner = access.substring(0, 3);
		String stringPermissionGroup = access.substring(3, 6);
		String stringPermissionOther = access.substring(6, 9);

		FileAccess permissionFileOwner = FileAccess.getPermissionFile(stringPermissionOwner);
		FileAccess permissionFileGroup = FileAccess.getPermissionFile(stringPermissionGroup);
		FileAccess permissionFileOther = FileAccess.getPermissionFile(stringPermissionOther);

		return new FTPFileAccess(path, owner, group, permissionFileOwner, permissionFileGroup, permissionFileOther);
	}

	public String toSaveFormat() {
		return this.path.toString() + FTPFileAccessManager.PATH_ACCESS_SEPARATOR
				+ this.globalAccess[INDEX_OWNER_PERMISSION].describe
				+ this.globalAccess[INDEX_GROUP_PERMISSION].describe
				+ this.globalAccess[INDEX_OTHER_PERMISSION].describe + " " + this.getOwner() + " " + this.getGroup();
	}

	public int getPermissionNumber() {
		return this.globalAccess[INDEX_OWNER_PERMISSION].value * 100
				+ this.globalAccess[INDEX_GROUP_PERMISSION].value * 10
				+ this.globalAccess[INDEX_OTHER_PERMISSION].value;
	}

	/**
	 * 
	 * @return the long format description of the path.
	 */
	public String getPathDescriptionLongFormat() {
		StringBuilder builder = new StringBuilder();

		boolean isDirectory = false;

		if (Files.isDirectory(this.path)) {
			builder.append("d");
			isDirectory = true;
		} else if (Files.isSymbolicLink(this.path)) {
			builder.append("l");
		} else {
			builder.append("-");
		}

		builder.append(this.globalAccess[INDEX_OWNER_PERMISSION].describe);
		builder.append(this.globalAccess[INDEX_GROUP_PERMISSION].describe);
		builder.append(this.globalAccess[INDEX_OTHER_PERMISSION].describe + " ");

		if (isDirectory) {
			try {
				DirectoryStream<Path> stream = Files.newDirectoryStream(this.path);

				final Incrementer incrementer = new Incrementer();

				stream.forEach((p) -> {
					if (Files.isDirectory(p) || Files.isSymbolicLink(p)) {
						incrementer.increments();
					}
				});

				if (incrementer.i > 0)
					builder.append(incrementer + " ");
				else
					builder.append("1 ");

			} catch (IOException e) {
				builder.append("1 ");
			}
		} else {
			builder.append("1 ");
		}

		builder.append(this.owner + " ");
		builder.append(this.group + " ");
		try {
			builder.append(Files.size(path) + " ");
		} catch (IOException e) {
			builder.append(" 0 ");
		}

		builder.append(this.path.getFileName());

		return builder.toString();
	}

	// Getters and Setters.

	public Path getPath() {
		return this.path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	/**
	 * 
	 * @return a clone of FileAccess array.
	 */
	public FileAccess[] getGlobalAccess() {
		return this.globalAccess.clone();
	}

	public FileAccess getOwnerPermissionFile() {
		return this.globalAccess[INDEX_OWNER_PERMISSION];
	}

	public void setOwnerPermissionFile(FileAccess fileAccess) {
		this.globalAccess[INDEX_OWNER_PERMISSION] = fileAccess;
	}

	public FileAccess getGroupPermissionFile() {
		return this.globalAccess[INDEX_GROUP_PERMISSION];
	}

	public void setGroupPermissionFile(FileAccess fileAccess) {
		this.globalAccess[INDEX_GROUP_PERMISSION] = fileAccess;
	}

	public FileAccess getOtherPermissionFile() {
		return this.globalAccess[INDEX_OTHER_PERMISSION];
	}

	public void setOtherPermissionFile(FileAccess fileAccess) {
		this.globalAccess[INDEX_OTHER_PERMISSION] = fileAccess;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	// Private class.

	private static class Incrementer {

		// Variables.

		private int i;

		// Constructors.

		public Incrementer(int i) {
			this.i = i;
		}

		public Incrementer() {
			this(0);
		}

		// Methods.

		@Override
		public String toString() {
			return Integer.toString(this.i);
		}

		public void increments() {
			this.i++;
		}
	}

	// Public Enum

	public static enum FileAccess {
		NOTHING(0, "---"), READ(4, "r--"), WRITE(2, "-w-"), EXECUTE(1, "--x"), READ_WRITE(6, "rw-"), READ_EXECUTE(5,
				"r-x"), WRITE_EXECUTE(3, "-wx"), READ_WRITE_EXECUTE(7, "rwx");

		// Constants.

		private static final FileAccess ALL_PERMISSION_FILE[] = { NOTHING, EXECUTE, WRITE, WRITE_EXECUTE, READ,
				READ_EXECUTE, READ_WRITE, READ_WRITE_EXECUTE };

		// Variables.

		private int value;

		private String describe;

		// Constructors.

		FileAccess(int value, String describe) {
			this.value = value;
			this.describe = describe;
		}

		// Methods.

		public static final FileAccess getPermissionFile(String permissionFile) {

			if (permissionFile.length() < 3 || permissionFile.length() > 3)
				return NOTHING;

			int read = permissionFile.charAt(0) == 'r' ? READ.value : 0;
			int write = permissionFile.charAt(1) == 'w' ? WRITE.value : 0;
			int execute = permissionFile.charAt(2) == 'x' ? EXECUTE.value : 0;

			return ALL_PERMISSION_FILE[read + write + execute];
		}

		public static final FileAccess getPermissionFile(int permissionNumber) {
			if (permissionNumber > 7 || permissionNumber < 0)
				return NOTHING;

			return ALL_PERMISSION_FILE[permissionNumber];
		}

		// Getters.

		public int value() {
			return this.value;
		}

		public String describe() {
			return this.describe;
		}
	}

}
