package server.ftp.window;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map.Entry;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import server.ftp.debug.Report;

public class ReportInformationPane extends VBox {

	// Constants.

	private static final int DEFAULT_SPACING = 3;

	// Variables.

	private Report report;

	// Constructors.

	public ReportInformationPane(Report report) {
		super(DEFAULT_SPACING);

		this.report = report;

		this.init();
	}

	// Methods.

	private void init() {

		List<Entry<LocalDateTime, String>> listAction = this.report.getListAction();

		if (this.report.isFailed()) {
			Text t = new Text("FAIL");
			t.setFill(Color.RED);
			this.getChildren().add(t);
		}

		for (Entry<LocalDateTime, String> entry : listAction) {

			String text = "[" + entry.getKey() + "] - " + entry.getValue();

			Text t = new Text(text);

			if (this.report.isFailed()) {
				t.setFill(Color.RED);
			} else {
				t.setFill(Color.DODGERBLUE);
			}

			this.getChildren().add(t);

		}

	}

}
