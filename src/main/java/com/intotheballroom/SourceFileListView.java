package com.intotheballroom;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dasha on 5/17/2015.
 */
public class SourceFileListView extends ListView<String> {
    private String source;
    private String year;

    private final Border defaultBorder;
    private final DocumentsController controller;
    private final ListChangeListener<FileDescription> listener;
    private final HashMap<String, SimpleBooleanProperty> checkBoxItems = new HashMap<>();
    private final BatchObservableList<String> checkedFiles = new BatchObservableList<>(new ArrayList<>());
    private final BatchObservableList<String> selectedFiles = new BatchObservableList<>(new ArrayList<>());

    public SourceFileListView(DocumentsController controller) {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) c -> updateSelection());
        checkedFiles.addListener((ListChangeListener<String>) c -> updateSelection());
        setEditable(true);
        setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String param) {
                return checkBoxItems.get(param);
            }
        }));
        this.controller = controller;
        defaultBorder = getBorder();

        listener = c -> {
            while (c.next()) {
                for (FileDescription description : c.getAddedSubList()) {
                    getItems().add(description.getName());
                    checkBoxItems.put(description.getName(), createCheckedItemSelectionProperty(description.getName()));
                }
            }
        };


        setOnDragOver(event -> {
            if (event.getDragboard().hasFiles() && source != null) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles() && source != null) {
                setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            }
            event.consume();
        });

        setOnDragExited(event -> {
            if (event.getDragboard().hasFiles() && source != null) {
                setBorder(defaultBorder);
            }
            event.consume();
        });
        setOnDragDropped(event -> {
            if (source != null) {
                ArrayList<File> files = new ArrayList<>();
                for (File file : event.getDragboard().getFiles()) {
                    addFile(file, files);
                }
                controller.addFilesToSource(source, year, files);
            }
        });

        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                event.consume();
                ObservableList<String> selectedItems = getSelectionModel().getSelectedItems();
                if (selectedItems.isEmpty()) {
                    return;
                }
                boolean newValue = !checkBoxItems.get(selectedItems.get(0)).getValue();

                checkedFiles.beginBatchChange();
                for (String selectedItem : selectedItems) {
                    checkBoxItems.get(selectedItem).setValue(newValue);
                }
                checkedFiles.endBatchChange();
            } else if (event.getCode() == KeyCode.F2) {
                event.consume();
                edit(getSelectionModel().getSelectedIndex());
            }
        });

        setOnMouseClicked(event -> {
            if (getSelectionModel().getSelectedItem() != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                File file = controller.getFile(year, source, getSelectionModel().getSelectedItem());
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            event.consume();
        });

        controller.foundFilesProperty().addListener(SourceFileListView.this::foundFilesChanged);
    }

    private void updateSelection() {
        selectedFiles.beginBatchChange();
        selectedFiles.clear();
        if (checkedFiles.isEmpty()) {
            selectedFiles.addAll(getSelectionModel().getSelectedItems());
        } else {
            selectedFiles.addAll(checkedFiles);
        }
        selectedFiles.endBatchChange();
    }

    private void foundFilesChanged(ObservableValue<? extends Map<String, Map<String, Set<String>>>> observable, Map<String, Map<String, Set<String>>> oldValue, Map<String, Map<String, Set<String>>> newValue) {
        setSource(this.year, this.source);
    }

    public ObservableList<String> getSelectedFiles() {
        return selectedFiles;
    }

    private SimpleBooleanProperty createCheckedItemSelectionProperty(String fileName) {
        SimpleBooleanProperty property = new SimpleBooleanProperty();
        property.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                checkedFiles.add(fileName);
            } else {
                checkedFiles.remove(fileName);
            }
        });
        return property;
    }

    private void addFile(File file, ArrayList<File> files) {
        if (file.isDirectory()) {
            for (File subfile : file.listFiles()) {
                addFile(subfile, files);
            }
        } else {
            files.add(file);
        }
    }

    public void setSource(String year, String source) {
        getItems().clear();
        checkBoxItems.clear();
        checkedFiles.clear();
        if (this.year != null && this.source != null) {
            ObservableList<FileDescription> oldSourceFiles = controller.getSourceFiles(this.year, this.source);
            oldSourceFiles.removeListener(listener);
        }
        this.year = year;
        this.source = source;
        if (year != null && source != null) {
            ObservableList<FileDescription> sourceFiles = controller.getSourceFiles(year, source);
            sourceFiles.addListener(listener);
            ObservableList<String> items = getItems();
            Map<String, Map<String, Set<String>>> foundFiles = controller.foundFilesProperty().getValue();
            Set<String> files = null;
            if (foundFiles != null && foundFiles.containsKey(year)) {
                Map<String, Set<String>> yearData = foundFiles.get(year);
                if (yearData != null && yearData.containsKey(source)) {
                    files = yearData.get(source);
                }
            }
            for (FileDescription sourceFile : sourceFiles) {
                if (files == null || files.contains(sourceFile.getName())) {
                    items.add(sourceFile.getName());
                    checkBoxItems.put(sourceFile.getName(), createCheckedItemSelectionProperty(sourceFile.getName()));
                }
            }
        }
    }
}

