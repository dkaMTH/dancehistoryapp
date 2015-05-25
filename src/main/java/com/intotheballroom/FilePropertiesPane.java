package com.intotheballroom;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Dasha on 5/23/2015.
 */
public class FilePropertiesPane extends GridPane {

    private final Map<FilePropertyType, Boolean> modifiedProperties = new EnumMap<>(FilePropertyType.class);
    private final Map<FilePropertyType, TextInputControl> propertyEditors = new EnumMap<>(FilePropertyType.class);
    private final EnumMap<FilePropertyType, String> commonValues = new EnumMap<>(FilePropertyType.class);

    private boolean updatingText;

    private String year;
    private String source;

    public FilePropertiesPane(DocumentsController controller, ObservableList<String> checkedFiles) {
        this.add(new Label("Page name:"), 0, 0);
        TextField pageName = new TextField();
        this.add(pageName, 1, 0);
        propertyEditors.put(FilePropertyType.PAGENAME, pageName);

        this.add(new CheckBox("Rename file"), 2, 0); //TODO: Move, maybe

        this.add(new Label("Page author(s):"), 0, 1);
        TextField pageAuthors = new TextField();
        this.add(pageAuthors, 1, 1);
        propertyEditors.put(FilePropertyType.PAGEAUTHORS, pageAuthors);

        this.add(new Label("Date:"), 0, 2);
        TextField pageYear = createSmallField("Year");
        TextField pageMonth = createSmallField("Month");
        TextField pageDay = createSmallField("Day");
        this.add(new HBox(10, pageYear, pageMonth, pageDay), 1, 2);
        propertyEditors.put(FilePropertyType.YEAR, pageYear);
        propertyEditors.put(FilePropertyType.MONTH, pageMonth);
        propertyEditors.put(FilePropertyType.DAY, pageDay);

        this.add(new Label("Comment:"), 0, 3);
        TextArea commentArea = new TextArea();
        commentArea.setPrefWidth(250);
        this.add(commentArea, 1, 3);
        propertyEditors.put(FilePropertyType.COMMENT, commentArea);

        this.add(new Label("Dances:"), 0, 4);
        TextField danceField = new TextField();
        this.add(danceField, 1, 4);
        propertyEditors.put(FilePropertyType.DANCES, danceField);


        for (Map.Entry<FilePropertyType, TextInputControl> entry : propertyEditors.entrySet()) {
            entry.getValue().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    setModified(entry.getKey(), false);
                } else if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.ENTER) {
                    setModified(entry.getKey(), true);
                }
            });
            entry.getValue().textProperty().addListener((observable, oldValue, newValue) -> {
                if (!updatingText) {
                    setModified(entry.getKey(), true);
                }
            });
        }

        Button applyChanges = new Button("Apply changes");

        applyChanges.setOnAction(event -> {
            for (Map.Entry<FilePropertyType, TextInputControl> entry : propertyEditors.entrySet()) {
                FilePropertyType propertyType = entry.getKey();
                if (isModified(propertyType)) {
                    String value = entry.getValue().getText();
                    for (String checkedFile : checkedFiles) {
                        controller.setFileProperty(year, source, checkedFile, propertyType, value);
                    }
                    commonValues.put(propertyType, value);
                    setModified(propertyType, false);
                }
            }
        });

        Button reset = new Button("Reset");

        reset.setOnAction(event -> {
            for (FilePropertyType propertyType : propertyEditors.keySet()) {
                setModified(propertyType, false);
            }
        });

        this.add(new HBox(10, applyChanges, reset), 1, 5);

        checkedFiles.addListener((ListChangeListener<String>) change -> {
            commonValues.clear();
            for (String checkedFile : checkedFiles) {
                FileDescription description = controller.getFileDescription(this.year, source, checkedFile);
                for (FilePropertyType type : EnumSet.allOf(FilePropertyType.class)) {
                    String fileValue = description.getProperty(type);
                    if (!commonValues.containsKey(type)) {
                        commonValues.put(type, fileValue);
                    } else if (!Objects.equals(commonValues.get(type), fileValue)) {
                        commonValues.put(type, null);
                    }
                }
            }

            for (Map.Entry<FilePropertyType, TextInputControl> entry : propertyEditors.entrySet()) {
                if (!isModified(entry.getKey())) {
                    updatingText = true;
                    entry.getValue().setText(commonValues.get(entry.getKey()));
                    updatingText = false;
                }
            }
        });

    }

    public void setSource(String year, String source) {
        this.year = year;
        this.source = source;
    }

    public void setModified(FilePropertyType propertyType, boolean modified) {
        modifiedProperties.put(propertyType, modified);
        Node control = propertyEditors.get(propertyType);
        if (control instanceof TextArea) {
            control = control.lookup(".content");
        }
        if (modified) {
            control.setStyle("-fx-background-color: #B0C4DE");
        } else {
            control.setStyle(null);
            updatingText = true;
            propertyEditors.get(propertyType).setText(commonValues.get(propertyType));
            updatingText = false;
        }
    }

    public boolean isModified(FilePropertyType propertyType) {
        Boolean value = modifiedProperties.get(propertyType);
        return value != null && value;
    }

    private TextField createSmallField(String prompt) {
        TextField result = new TextField();
        result.setPrefWidth(50);
        result.setPromptText(prompt);
        return result;
    }

}
