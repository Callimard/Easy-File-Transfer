package server.ftp.window;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import server.ftp.listener.CWDListener;
import server.ftp.server.FTPClientConnection;

public class FTPClientConnectionInformationPane extends VBox implements CWDListener {

	// Constants.

	private static final String CWD = "CWD";

	private static final double IP_PORT_PANE_SPACING = 10.d;

	// Variables.

	private FTPClientConnection ftpClientConnection;

	private Text ipText;
	private Text portText;
	private HBox ipPortPane = new HBox();

	private Text cwdText;

	// Constructors.

	public FTPClientConnectionInformationPane(FTPClientConnection ftpClientConnection) {

		this.ftpClientConnection = ftpClientConnection;

		// ipPortPane.

		this.ipText = new Text(this.ftpClientConnection.getHostAddress());

		this.portText = new Text(Integer.toString(this.ftpClientConnection.getPort()));

		this.ipPortPane.getChildren().addAll(this.ipText, this.portText);
		this.ipPortPane.setSpacing(IP_PORT_PANE_SPACING);

		// cwdText.

		this.cwdText = new Text(CWD + " : " + (this.ftpClientConnection.getCurrentWorkingDirectory() == null ? "null"
				: this.ftpClientConnection.getCurrentWorkingDirectory().toString()));

		// End.

		this.getChildren().addAll(this.ipPortPane, this.cwdText);

		this.ftpClientConnection.addCWDListener(this);
	}

	// Methods.

	@Override
	public void cwdChange(String newCWD) {
		this.cwdText.setText(CWD + " : " + (this.ftpClientConnection.getCurrentWorkingDirectory() == null ? "null"
				: this.ftpClientConnection.getCurrentWorkingDirectory().toString()));
	}

	// Getters and Setters.

	public FTPClientConnection getFTPClientConnection() {
		return this.ftpClientConnection;
	}

	public String getCWD() {
		return this.cwdText.getText();
	}

	public void setCWD(String cwd) {
		this.cwdText.setText(cwd);
	}

}
