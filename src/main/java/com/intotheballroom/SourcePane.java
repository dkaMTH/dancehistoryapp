package com.intotheballroom;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by Dasha on 5/17/2015.
 */
public class SourcePane extends VBox {

    private final Label sourceName;
    private final SourceFileListView fileList;
    private final FilePropertiesPane filePropertiesPane;


    public SourcePane(DocumentsController controller) {
        ObservableList<Node> children = getChildren();

        GridPane sourceProperties = initGridPane(new GridPane());
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

        filePropertiesPane = new FilePropertiesPane(controller, fileList.getSelectedFiles());
        GridPane fileProperties = initGridPane(filePropertiesPane);
        children.add(fileProperties);

        setSource(null, null);
    }


    private GridPane initGridPane(GridPane gridPane) {
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
        filePropertiesPane.setSource(year, source);

    }
}
