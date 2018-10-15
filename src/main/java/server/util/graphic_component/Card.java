package server.util.graphic_component;

import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Card extends VBox {

	// Constants.

	public static final double DEFAULT_SPACING = 15.0d;

	public static final Border DEFAULT_BORDER = new Border(
			new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));

	// Variables.

	private Text title;

	private Node subNode;

	// Constructors.

	private Card() {
		super();

		this.setBorder(DEFAULT_BORDER);
	}

	public Card(String title, Node subNode) {
		this(title, subNode, DEFAULT_SPACING);
	}

	public Card(String title, Node subNode, double spacing) {
		this();

		this.setSpacing(spacing);

		this.title = new Text(title);

		this.subNode = subNode;

		this.getChildren().add(this.title);
		this.getChildren().add(this.subNode);
	}

	public Card(Text title, Node subNode) {
		this(title, subNode, DEFAULT_SPACING);
	}

	public Card(Text title, Node subNode, double spacing) {
		this();

		this.setSpacing(spacing);

		this.title = title;

		this.subNode = subNode;

		this.getChildren().add(this.title);
		this.getChildren().add(this.subNode);
	}

	public void setTitleFont(Font titleFont) {
		this.title.setFont(titleFont);
	}

	// Getters and Setters.

	public String getTitle() {
		return this.title.getText();
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}

	public Node getSubNode() {
		return this.subNode;
	}

	public void setSubNode(Node subNode) {
		this.subNode = subNode;
	}

}
