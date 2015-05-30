package com.intotheballroom;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.*;

/**
 * Created by Dasha on 5/23/2015.
 */
public class FilePropertiesPane extends GridPane {

    private final Map<FilePropertyType, Boolean> modifiedProperties = new EnumMap<>(FilePropertyType.class);
    private final Map<FilePropertyType, TextInputControl> propertyEditors = new EnumMap<>(FilePropertyType.class);
    private final EnumMap<FilePropertyType, String> commonValues = new EnumMap<>(FilePropertyType.class);
    private final DocumentsController controller;

    private boolean updatingText;

    private String year;
    private String source;
    private final TextField danceField;
    private boolean danceFieldValid = true;

    public FilePropertiesPane(DocumentsController controller, ObservableList<String> selectedFiles) {
        this.controller = controller;
        this.add(new Label("Page name:"), 0, 0);
        TextField pageName = new TextField();
        this.add(pageName, 1, 0);
        propertyEditors.put(FilePropertyType.PAGENAME, pageName);


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
        commentArea.setPrefWidth(300);
        this.add(commentArea, 1, 3);
        propertyEditors.put(FilePropertyType.COMMENT, commentArea);

        this.add(new Label("Dances:"), 0, 4);
        danceField = new TextField();
        this.add(danceField, 1, 4);
        propertyEditors.put(FilePropertyType.DANCES, danceField);
        danceField.textProperty().addListener(this::validateDanceField);


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
        CheckBox renameCheckbox = new CheckBox("Rename file");

        applyChanges.setOnAction(event -> {
            if (isModified(FilePropertyType.DANCES)) {
                ArrayList<String> matchedDances = new ArrayList<>();
                String[] patterns = danceField.getText().split(",");
                for (String pattern : patterns) {
                    if (!pattern.trim().isEmpty()) {
                        ArrayList<String> matchingDances = getMatchingDances(pattern);
                        if (matchingDances.size() != 1) {
                            return;
                        }
                        matchedDances.addAll(matchingDances);
                    }
                }
                Collections.sort(matchedDances);
                StringJoiner sj = new StringJoiner(", ");
                matchedDances.stream().forEach(sj::add);
                danceField.setText(sj.toString());
            }

            for (Map.Entry<FilePropertyType, TextInputControl> entry : propertyEditors.entrySet()) {
                FilePropertyType propertyType = entry.getKey();
                if (isModified(propertyType)) {
                    String value = entry.getValue().getText();
                    for (String checkedFile : selectedFiles) {
                        controller.setFileProperty(year, source, checkedFile, propertyType, value);
                    }
                    commonValues.put(propertyType, value);
                    setModified(propertyType, false);
                }
            }

            if (renameCheckbox.isSelected() && !renameCheckbox.isDisable()) {
                for (String selectedFile : selectedFiles) {
                    controller.renameFile(year, source, selectedFile, pageName.getText());
                }
            }

            controller.saveSource(year, source);
        });

        Button reset = new Button("Reset");

        reset.setOnAction(event -> {
            for (FilePropertyType propertyType : propertyEditors.keySet()) {
                setModified(propertyType, false);
            }
        });

        HBox bottomRow = new HBox(10, renameCheckbox, applyChanges, reset);
        bottomRow.setAlignment(Pos.BASELINE_RIGHT);
        this.add(bottomRow, 1, 5);

        selectedFiles.addListener((ListChangeListener<String>) change -> {
            commonValues.clear();

            renameCheckbox.setDisable(selectedFiles.size() != 1);

            for (String checkedFile : selectedFiles) {
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

    private void validateDanceField(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        danceFieldValid = true;
        StringBuilder message = new StringBuilder();

        if (newValue != null && !newValue.trim().isEmpty()) {
            String[] patterns = newValue.split(",");
            for (String pattern : patterns) {
                if (pattern.trim().isEmpty()) {
                    continue;
                }
                ArrayList<String> matches = getMatchingDances(pattern);

                if (matches.size() == 0) {
                    danceFieldValid = false;
                    message.append(String.format("Can't resolve entry [%s].", pattern.trim()));
                } else if (matches.size() > 1) {
                    danceFieldValid = false;
                    StringJoiner sj = new StringJoiner(", ");
                    matches.stream().forEach(sj::add);
                    message.append(String.format("Ambiguous entry [%s] matches (%s).", pattern.trim(), sj.toString()));
                }
            }
        }

        if (!danceFieldValid) {
            Tooltip tooltip = new Tooltip(message.toString());
            System.out.println(message.toString());
            danceField.setTooltip(tooltip);
        } else {
            danceField.setTooltip(null);
        }
    }

    private ArrayList<String> getMatchingDances(String pattern) {
        String familyName;
        String styleName = null;
        if (pattern.contains(">")) {
            familyName = pattern.substring(0, pattern.indexOf('>')).trim();
            styleName = pattern.substring(pattern.indexOf('>') + 1).trim();
        } else {
            familyName = pattern.trim();
        }

        ArrayList<String> matches = new ArrayList<>();
        ObservableMap<String, DanceFamily> danceFamilies = controller.getDanceFamilies();
        for (DanceFamily danceFamily : danceFamilies.values()) {
            if (danceFamily.getName().toLowerCase().startsWith(familyName.toLowerCase())) {
                if (styleName == null) {
                    matches.add(danceFamily.getName());
                } else {
                    for (String style : danceFamily.getStyles()) {
                        if (style.toLowerCase().startsWith(styleName.toLowerCase())) {
                            matches.add(danceFamily.getName() + " > " + style);
                        }
                    }
                }
            }
        }
        return matches;
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
