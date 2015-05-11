package com.intotheballroom;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class DanceApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        DocumentsController controller = new DocumentsController(
                new File("Sorted"),
                new File("Unsorted"));

        primaryStage.setTitle("Dance app");

        SplitPane root = new SplitPane();

        SplitPane left = new SplitPane();
        left.setOrientation(Orientation.VERTICAL);
        SortedTreeView sortedView = new SortedTreeView(controller);
        sortedView.setCellFactory(new SortedContextMenuCellFactory(controller));
        FileTreeView unsortedView = new FileTreeView(controller.getUnsortedRoot(), false);
        left.getItems().addAll(
                sortedView,
                unsortedView);

        StackPane middle = new StackPane();
        middle.getChildren().add(new Button("Button Two"));

        StackPane right = new StackPane();
        right.getChildren().add(new Button("Button Three"));

        root.getItems().addAll(left, middle, right);
        root.setDividerPositions(0.2f, 0.8f);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}
