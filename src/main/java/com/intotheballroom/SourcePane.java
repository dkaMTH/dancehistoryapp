package com.intotheballroom;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by Dasha on 5/17/2015.
 */
public class SourcePane extends VBox {

    private final Label sourceName;
    private final SourceFileListView fileList;


    public SourcePane(DocumentsController controller) {
        ObservableList<Node> children = getChildren();

        GridPane sourceProperties = createGridPane();
        sourceProperties.add(new Label("Source:"), 0, 0);
        sourceName = new Label();
        sourceProperties.add(sourceName, 1, 0);
        sourceProperties.add(new Label("Author(s):"), 0, 1);
        TextField authorsField = new TextField();
        authorsField.setPromptText("Use a comma to separate multiple authors");
        authorsField.setPrefWidth(250);
        sourceProperties.add(authorsField, 1, 1);
        children.add(sourceProperties);

        fileList = new SourceFileListView(controller);
        VBox.setVgrow(fileList, Priority.ALWAYS);
        children.add(fileList);

        GridPane fileProperties = createGridPane();
        children.add(fileProperties);
        fileProperties.add(new Label("Page name:"), 0, 0);
        fileProperties.add(new TextField(), 1, 0);
        fileProperties.add(new CheckBox("Rename file"), 2, 0);
        fileProperties.add(new Label("Page author(s):"), 0, 1);
        fileProperties.add(new TextField(), 1, 1);
        fileProperties.add(new Label("Date"), 0, 2);
        fileProperties.add(new HBox(10, createSmallField("Year"), createSmallField("Month"), createSmallField("Day")), 1, 2);
        fileProperties.add(new Label("Comment:"), 0, 3);
        TextArea commentArea = new TextArea();
        commentArea.setPrefWidth(250);
        fileProperties.add(commentArea, 1, 3);
        fileProperties.add(new Label("Dances:"), 0, 4);
        fileProperties.add(new HBox(10, new ComboBox<>(FXCollections.observableArrayList("Waltz", "Collegiate")),
                new ComboBox<>(FXCollections.observableArrayList("Slow", "Hesitation")), new Button("Add")), 1, 4);
        fileProperties.add(new Button("Apply changes"), 1, 5);

        setSource(null, null);
    }

    private TextField createSmallField(String prompt) {
        TextField result = new TextField();
        result.setPrefWidth(50);
        result.setPromptText(prompt);
        return result;
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        return gridPane;
    }

    public void setSource(String year, String source) {
        if (source == null) {
            setDisable(true);
            sourceName.setText(null);
        } else {
            setDisable(false);
            sourceName.setText(source + " (" + year + ")");
        }
        fileList.setSource(year, source);

    }
}
