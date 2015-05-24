package com.intotheballroom;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Created by Dasha on 5/23/2015.
 */
public class FilePropertiesPane extends GridPane {

    private String year;
    private String source;

    public FilePropertiesPane(DocumentsController controller, ObservableList<String> checkedFiles) {
        this.add(new Label("Page name:"), 0, 0);
        this.add(new TextField(), 1, 0);
        this.add(new CheckBox("Rename file"), 2, 0);
        this.add(new Label("Page author(s):"), 0, 1);
        this.add(new TextField(), 1, 1);
        this.add(new Label("Date"), 0, 2);
        this.add(new HBox(10, createSmallField("Year"), createSmallField("Month"), createSmallField("Day")), 1, 2);
        this.add(new Label("Comment:"), 0, 3);
        TextArea commentArea = new TextArea();
        commentArea.setPrefWidth(250);
        this.add(commentArea, 1, 3);
        this.add(new Label("Dances:"), 0, 4);
        TextField danceField = new TextField();
        this.add(danceField, 1, 4);
        Button applyChanges = new Button("Apply changes");

        applyChanges.setOnAction(event -> {
            for (String checkedFile : checkedFiles) {
                controller.setFileProperty(year, source, checkedFile, FilePropertyType.COMMENT, commentArea.getText());
            }
        });

        Button reset = new Button("Reset");
        this.add(new HBox(10, applyChanges, reset), 1, 5);

        checkedFiles.addListener((ListChangeListener<String>) change -> {
            EnumMap<FilePropertyType, String> propertiesValues = new EnumMap<>(FilePropertyType.class);
            for (String checkedFile : checkedFiles) {
                FileDescription description = controller.getFileDescription(year, source, checkedFile);
                for (FilePropertyType type : EnumSet.allOf(FilePropertyType.class)) {
                    String fileValue = description.getProperty(type);
                    if (!propertiesValues.containsKey(type)) {
                        propertiesValues.put(type, fileValue);
                    } else if (!Objects.equals(propertiesValues.get(type), fileValue)) {
                        propertiesValues.put(type, null);
                    }
                }
            }

            String commentValue = propertiesValues.get(FilePropertyType.COMMENT);
            commentArea.setText(commentValue);
        });

    }

    private TextField createSmallField(String prompt) {
        TextField result = new TextField();
        result.setPrefWidth(50);
        result.setPromptText(prompt);
        return result;
    }

    public void setSource(String year, String source) {
        this.year = year;
        this.source = source;
    }
}
