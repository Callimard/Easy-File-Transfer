package server.ftp.window;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.ftp.debug.Report;
import server.ftp.exception.FatalErrorException;
import server.ftp.exception.data_transfert_manager.EndPortLessThanBeginPortException;
import server.ftp.server.FTPClientConnection;
import server.ftp.server.FTPClientCreator;
import server.ftp.server.FTPServerController;
import server.ftp.sql.table_row.ClientRow;
import server.util.ErrorManager;
import server.util.easy_server.exception.server_controller.ServerControllerAlreadyStartedException;
import server.util.easy_server.exception.server_controller.ServerControllerAlreadyStoppedException;
import server.util.easy_server.exception.server_controller.ServerControllerFailedToStartException;
import server.util.easy_server.exception.server_controller.ServerControllerFailedToStopException;

public class MainWindow extends Application {

	// Constants.

	private static final double DEFAULT_SPACING = 3.d;

	private static final int WIDTH = 1500;

	private static final int HEIGHT = 900;

	// Variables.

	private FTPServerController ftpServerController;

	private Stage stage;

	private Scene scene;

	private BorderPane root;

	private SplitPane centerPane;

	private VBox ftpConnectionPane;

	private VBox commandFtpConnectionPane;

	private HBox boxButton;
	private Button buttonStart;
	private Button buttonStop;

	private Vector<FTPClientConnectionCard> listFTPClientCard = new Vector<>();

	// Constructors.

	public MainWindow() {
	}

	// Methods.

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.stage = primaryStage;

		this.root = new BorderPane();

		this.scene = new Scene(this.root, WIDTH, HEIGHT);

		// Center Pane.

		this.centerPane = new SplitPane();

		this.ftpConnectionPane = new VBox();
		this.ftpConnectionPane.setSpacing(DEFAULT_SPACING);

		this.commandFtpConnectionPane = new VBox();
		this.commandFtpConnectionPane.setSpacing(DEFAULT_SPACING);

		ScrollPane scrollLeft = new ScrollPane(this.ftpConnectionPane);
		scrollLeft.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);

		ScrollPane scollRight = new ScrollPane(this.commandFtpConnectionPane);
		scollRight.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);

		this.centerPane.getItems().addAll(scrollLeft, scollRight);

		this.root.setCenter(this.centerPane);

		// Button.

		this.boxButton = new HBox();

		this.buttonStart = new Button("Start");
		this.buttonStart.setOnMouseClicked((mouseEvent) -> {
			try {
				try {
					this.ftpServerController = new FTPServerController(21, 60_000, 62_000, MainWindow.this);
					
					FTPClientCreator ftpClientCreator = this.ftpServerController.getFTPClientCreator();
					
					ClientRow callimard = new ClientRow(null, "callimard", "Callimard94", "Guillaume", "Rakotomalala", "guil.rako@hotmail.fr", null);
					
					ClientRow alexandre = new ClientRow(null, "alexandre", "Alexandre94", "Alexandre", "Bronner", "alex.bro94@hotmail.fr", null);
					
					ClientRow sylvain = new ClientRow(null, "sylvain", "Sylvain94", "Sylvain", "Rakotomalala", "sylvain@hotmail.fr", null);
					
					ftpClientCreator.createClient(callimard, null);
					
					ftpClientCreator.createClient(alexandre, null);
					
					ftpClientCreator.createClient(sylvain, null);
					
				} catch (FatalErrorException e) {
					ErrorManager.writeError(e);

					System.exit(0);
				}

				this.ftpServerController.start();
			} catch (IOException e) {
				ErrorManager.writeError(e);
				System.exit(0);
			} catch (ServerControllerAlreadyStartedException e) {
				ErrorManager.writeError(e);
				System.exit(0);
			} catch (ServerControllerFailedToStartException e) {
				ErrorManager.writeError(e);
				System.exit(0);
			} catch (EndPortLessThanBeginPortException e) {
				ErrorManager.writeError(e);
				System.exit(0);
			}
		});

		this.buttonStop = new Button("Stop");
		this.buttonStop.setOnMouseClicked((mouseEvent) -> {
			try {
				this.ftpServerController.stopAllNow();
			} catch (ServerControllerAlreadyStoppedException e) {
				ErrorManager.writeError(e);
				System.exit(0);
			} catch (ServerControllerFailedToStopException e) {
				ErrorManager.writeError(e);
			}
		});

		this.boxButton.getChildren().addAll(this.buttonStart, this.buttonStop);

		this.boxButton.setAlignment(Pos.CENTER);

		this.root.setBottom(this.boxButton);

		// end.

		this.stage.setScene(this.scene);

		this.stage.setTitle("Server Window");

		this.stage.setOnCloseRequest((event) -> {
			try {
				this.ftpServerController.stopAllNow();
			} catch (NullPointerException e) {
				// On fait rien les fenetre n'ont pas �t� ouvertes.
			} catch (ServerControllerAlreadyStoppedException e) {
				ErrorManager.writeError(e);
			} catch (ServerControllerFailedToStopException e) {
				ErrorManager.writeError(e);
			}
		});

		this.stage.show();

	}

	public void addFTPClientConnection(FTPClientConnection ftpClientConnection) {

		final FTPClientConnectionCard ftpClientConnectionCard = new FTPClientConnectionCard(
				ftpClientConnection.getPseudo() == null ? "null" : ftpClientConnection.getPseudo(),
				ftpClientConnection);

		ftpClientConnectionCard.setOnMouseClicked((mouseEvent) -> {

			this.commandFtpConnectionPane.getChildren().clear();

			List<Report> listReport = this.ftpServerController.getFTPActionReminder()
					.getListReportFor(ftpClientConnection);

			for (Report report : listReport) {

				ReportCard reportCard = new ReportCard(report);

				this.commandFtpConnectionPane.getChildren().add(reportCard);

			}
		});

		this.listFTPClientCard.add(ftpClientConnectionCard);

		Platform.runLater(() -> this.ftpConnectionPane.getChildren().add(ftpClientConnectionCard));
	}

}
