package com.intotheballroom;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class DanceApp extends Application {
    public static void main(String[] args) {
        Toolkit.getDefaultToolkit();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, BackingStoreException {
        DirectoryChooser chooser = new DirectoryChooser();
        Preferences prefs = Preferences.userNodeForPackage(DanceApp.class);
        String defaultDirectory = prefs.get("root", null);

        if (defaultDirectory != null) {
            chooser.setInitialDirectory(new File(defaultDirectory));
        }
        File directory = chooser.showDialog(primaryStage);

        if (directory == null) {
            System.exit(1);
            return;
        }

        prefs.put("root", directory.getAbsolutePath());
        prefs.flush();

        DocumentsController controller = new DocumentsController(
                new File(directory, "Sorted"),
                new File(directory, "Unsorted"));

        primaryStage.setTitle("Dance app");

        SplitPane root = new SplitPane();

        SplitPane left = new SplitPane();
        left.setOrientation(Orientation.VERTICAL);
        SourcePane sourcePane = new SourcePane(controller);
        SortedTreeView sortedView = new SortedTreeView(controller);
        sortedView.setCellFactory(new SortedContextMenuCellFactory(controller));
        sortedView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getValue().getType() == SortedItemType.SOURCE) {
                sourcePane.setSource(newValue.getParent().getValue().getName(), newValue.getValue().getName());
            } else {
                sourcePane.setSource(null, null);
            }
        });
        FileTreeView unsortedView = new FileTreeView(controller.getUnsortedRoot(), false);
        left.getItems().addAll(
                sortedView,
                unsortedView);

        StackPane middle = new StackPane();
        middle.getChildren().add(sourcePane);

        StackPane right = new StackPane();
        TextField textField = new TextField();
        controller.filterTextProperty().bind(textField.textProperty());
        right.getChildren().add(new VBox(textField, new DancesTreeView(controller)));

        root.getItems().addAll(left, middle, right);
        root.setDividerPositions(0.2f, 0.8f);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}
