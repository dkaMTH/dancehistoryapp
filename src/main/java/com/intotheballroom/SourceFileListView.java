package com.intotheballroom;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Dasha on 5/17/2015.
 */
public class SourceFileListView extends ListView<String> {
    private String source;
    private String year;

    private final Border defaultBorder;
    private final DocumentsController controller;
    private final ListChangeListener<String> listener;

    public SourceFileListView(DocumentsController controller) {
        setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String param) {
                return new SimpleBooleanProperty();
            }
        }));
        this.controller = controller;
        defaultBorder = getBorder();

        listener = c -> {
            while (c.next()) {
                for (String fileName : c.getAddedSubList()) {
                    getItems().add(fileName);
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
        if (this.year != null && this.source != null) {
            ObservableList<String> oldSourceFiles = controller.getSourceFiles(this.year, this.source);
            oldSourceFiles.removeListener(listener);
        }
        this.year = year;
        this.source = source;
        if (year != null && source != null) {
            ObservableList<String> sourceFiles = controller.getSourceFiles(year, source);
            sourceFiles.addListener(listener);
            getItems().addAll(sourceFiles);
        }

    }
}

