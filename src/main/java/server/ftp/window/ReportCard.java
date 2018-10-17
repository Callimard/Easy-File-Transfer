package server.ftp.window;

import javafx.scene.text.Font;
import server.ftp.debug.Report;
import server.util.graphic_component.Card;

public class ReportCard extends Card {

	// Constants.

	// Variables.

	// Constructors.

	public ReportCard(Report report) {
		super("[" + report.getCreationDate() + "] - " + report.getTitle(), new ReportInformationPane(report));

		this.setTitleFont(Font.font(25));
	}

	// Methods.

}
