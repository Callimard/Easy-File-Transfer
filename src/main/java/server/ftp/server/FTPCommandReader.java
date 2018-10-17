package server.ftp.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import server.ftp.task.command.CommandABOR;
import server.ftp.task.command.CommandACCT;
import server.ftp.task.command.CommandALLO;
import server.ftp.task.command.CommandAPPE;
import server.ftp.task.command.CommandCDUP;
import server.ftp.task.command.CommandCWD;
import server.ftp.task.command.CommandDELE;
import server.ftp.task.command.CommandHELP;
import server.ftp.task.command.CommandLIST;
import server.ftp.task.command.CommandMDTM;
import server.ftp.task.command.CommandMKD;
import server.ftp.task.command.CommandMODE;
import server.ftp.task.command.CommandNLST;
import server.ftp.task.command.CommandNOOP;
import server.ftp.task.command.CommandPASS;
import server.ftp.task.command.CommandPASV;
import server.ftp.task.command.CommandPORT;
import server.ftp.task.command.CommandPWD;
import server.ftp.task.command.CommandQUIT;
import server.ftp.task.command.CommandREFUSED;
import server.ftp.task.command.CommandREIN;
import server.ftp.task.command.CommandREST;
import server.ftp.task.command.CommandRETR;
import server.ftp.task.command.CommandRMD;
import server.ftp.task.command.CommandRNFR;
import server.ftp.task.command.CommandRNTO;
import server.ftp.task.command.CommandSITE;
import server.ftp.task.command.CommandSIZE;
import server.ftp.task.command.CommandSMNT;
import server.ftp.task.command.CommandSTAT;
import server.ftp.task.command.CommandSTOR;
import server.ftp.task.command.CommandSTOU;
import server.ftp.task.command.CommandSTRU;
import server.ftp.task.command.CommandSYST;
import server.ftp.task.command.CommandTYPE;
import server.ftp.task.command.CommandUSER;
import server.util.easy_server.CommandReader;
import server.util.easy_server.exception.command_reader.FailToExtractCommandException;

public class FTPCommandReader implements CommandReader<FTPClientConnection> {

	// Variables.

	private FTPServerController ftpServerController;

	private HashMap<String, Class<? extends FTPCommand>> hashStringCommand = new HashMap<>();

	// Constructors.

	public FTPCommandReader(FTPServerController ftpServerController) {

		this.ftpServerController = ftpServerController;

		this.hashStringCommand.put("ABOR", CommandABOR.class);
		this.hashStringCommand.put("ACCT", CommandACCT.class);
		this.hashStringCommand.put("ALLO", CommandALLO.class);
		this.hashStringCommand.put("APPE", CommandAPPE.class);
		this.hashStringCommand.put("CDUP", CommandCDUP.class);
		this.hashStringCommand.put("CWD", CommandCWD.class);
		this.hashStringCommand.put("DELE", CommandDELE.class);
		this.hashStringCommand.put("HELP", CommandHELP.class);
		this.hashStringCommand.put("LIST", CommandLIST.class);
		this.hashStringCommand.put("MDTM", CommandMDTM.class);
		this.hashStringCommand.put("MKD", CommandMKD.class);
		this.hashStringCommand.put("MODE", CommandMODE.class);
		this.hashStringCommand.put("NLST", CommandNLST.class);
		this.hashStringCommand.put("NOOP", CommandNOOP.class);
		this.hashStringCommand.put("PASS", CommandPASS.class);
		this.hashStringCommand.put("PASV", CommandPASV.class);
		this.hashStringCommand.put("PORT", CommandPORT.class);
		this.hashStringCommand.put("PWD", CommandPWD.class);
		this.hashStringCommand.put("QUIT", CommandQUIT.class);
		this.hashStringCommand.put("REFUSED", CommandREFUSED.class);
		this.hashStringCommand.put("REIN", CommandREIN.class);
		this.hashStringCommand.put("REST", CommandREST.class);
		this.hashStringCommand.put("RETR", CommandRETR.class);
		this.hashStringCommand.put("RMD", CommandRMD.class);
		this.hashStringCommand.put("RNFR", CommandRNFR.class);
		this.hashStringCommand.put("RNTO", CommandRNTO.class);
		this.hashStringCommand.put("SITE", CommandSITE.class);
		this.hashStringCommand.put("SIZE", CommandSIZE.class);
		this.hashStringCommand.put("SMNT", CommandSMNT.class);
		this.hashStringCommand.put("STAT", CommandSTAT.class);
		this.hashStringCommand.put("STOR", CommandSTOR.class);
		this.hashStringCommand.put("STOU", CommandSTOU.class);
		this.hashStringCommand.put("STRU", CommandSTRU.class);
		this.hashStringCommand.put("SYST", CommandSYST.class);
		this.hashStringCommand.put("TYPE", CommandTYPE.class);
		this.hashStringCommand.put("USER", CommandUSER.class);
	}

	// Methods.

	@Override
	public Runnable[] extractCommandRunnable(FTPClientConnection ftpClientConnection)
			throws FailToExtractCommandException {

		try {

			String[] arrayCommand = this.read(ftpClientConnection);

			if (arrayCommand != null) {

				Runnable[] arrayRunnable = new Runnable[arrayCommand.length];

				int j = 0;

				for (String command : arrayCommand) {

					StringTokenizer separatorArguments = new StringTokenizer(command, " ");

					String commandName = separatorArguments.nextToken();

					String[] args = separatorArguments.countTokens() != 0 ? new String[separatorArguments.countTokens()]
							: null;

					if (args != null) {
						int i = 0;
						while (separatorArguments.hasMoreTokens()) {
							args[i] = separatorArguments.nextToken();
							i++;
						}
					}

					final Class<? extends FTPCommand> cmdClass = this.hashStringCommand.get(commandName);

					if (cmdClass != null) {

						try {
							Constructor<? extends FTPCommand> constructor = cmdClass
									.getConstructor(FTPClientConnection.class, FTPServerController.class, String[].class);

							arrayRunnable[j] = constructor.newInstance(this.ftpServerController);

						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException e) {
							throw new FailToExtractCommandException(e);
						}

					} else {

						try {
							Constructor<? extends FTPCommand> constructor = this.hashStringCommand.get("REFUSED")
									.getConstructor(FTPServerController.class);

							arrayRunnable[j] = constructor.newInstance(this.ftpServerController);

						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException e) {
							throw new FailToExtractCommandException(e);
						}
					}

					j++;
				}

				return arrayRunnable;
			} else {
				return null;
			}

		} catch (IOException e) {
			throw new FailToExtractCommandException(e);
		}

	}

	@Override
	public Callable<?>[] extractCommandCallable(FTPClientConnection ftpClientConnection)
			throws FailToExtractCommandException {
		return null;
	}

	/**
	 * Don't block the read.
	 * 
	 * @param ftpClientConnection
	 * 
	 * @return the string command array in socket.
	 * 
	 * @throws IOException
	 * 
	 */
	private String[] read(FTPClientConnection ftpClientConnection) throws IOException {

		String s = null;

		ArrayList<String> listCommand = new ArrayList<>();

		while (ftpClientConnection.ready() && (s = ftpClientConnection.readLine()) != null) {
			listCommand.add(s);
			// System.out.println("Read = " + s);
		}

		if (!listCommand.isEmpty()) {

			String[] arrayCommand = new String[listCommand.size()];

			return listCommand.toArray(arrayCommand);
		} else {
			return null;
		}
	}

}
