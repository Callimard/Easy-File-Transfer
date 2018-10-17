package server.ftp.window;

import server.ftp.listener.PseudoListener;
import server.ftp.server.FTPClientConnection;
import server.util.graphic_component.Card;

public class FTPClientConnectionCard extends Card implements PseudoListener {

	// Variables.

	private FTPClientConnectionInformationPane infoPane;

	// Constructors.

	public FTPClientConnectionCard(String pseudo, FTPClientConnection ftpClientConnection) {
		super(pseudo, new FTPClientConnectionInformationPane(ftpClientConnection));

		this.infoPane = (FTPClientConnectionInformationPane) this.getSubNode();

		ftpClientConnection.addPseudoListener(this);
	}

	// Methods.

	@Override
	public void pseudoChange(String newPseudo) {
		this.setTitle(newPseudo);
	}

	// Getters and Setters.

	public FTPClientConnectionInformationPane getInfoPane() {
		return this.infoPane;
	}

}
