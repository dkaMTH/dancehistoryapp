package com.intotheballroom;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ContextMenuCell extends TreeCell<SortedItem> {

    private final DocumentsController controller;

    public ContextMenuCell(DocumentsController controller) {
        this.controller = controller;
    }

    @Override
    protected void updateItem(SortedItem item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            setText(item.getName());
            if (item.getType() == SortedItemType.ROOT) {
                setContextMenu(createRootContextMenu());
            } else if (item.getType() == SortedItemType.YEAR) {
                setContextMenu(createYearContextMenu());
            } else {
                setContextMenu(null);
            }
        } else {
            setText(null);
            setContextMenu(null);
        }
    }

    private ContextMenu createRootContextMenu() {
        MenuItem menuAddYearItem = new MenuItem("Add year");
        menuAddYearItem.setOnAction((ActionEvent t) -> {
            Stage yearPrompt = new Stage();
            yearPrompt.setTitle("Enter year");

            GridPane root = new GridPane();
            root.setAlignment(Pos.CENTER);
            root.setHgap(10);
            root.setVgap(10);
            root.setPadding(new Insets(10, 10, 10, 10));

            root.add(new Label("Year:"), 0, 0);
            TextField yearTextField = new TextField();
            yearTextField.setPromptText("Enter Year");
            yearTextField.setPrefWidth(50);
            root.add(yearTextField, 1, 0);

            Button cancel = new Button("Cancel");
            cancel.setCancelButton(true);
            Button ok = new Button("OK");
            ok.setDefaultButton(true);
            HBox hb = new HBox(10);
            hb.setAlignment(Pos.BOTTOM_RIGHT);
            hb.getChildren().add(ok);
            hb.getChildren().add(cancel);
            root.add(hb, 1, 1);

            ok.setOnAction(event -> {
                if (controller.createYear(yearTextField.getText())) {
                    yearPrompt.close();
                }
            });
            cancel.setOnAction(event -> yearPrompt.close());

            yearPrompt.setScene(new Scene(root, 200, 100));
            yearPrompt.initModality(Modality.APPLICATION_MODAL);
            yearPrompt.show();
        });
        MenuItem menuRebuildIndex = new MenuItem("Rebuild search index");
        menuRebuildIndex.setOnAction(event -> {
            controller.reindex();
        });
        return new ContextMenu(menuAddYearItem, menuRebuildIndex);
    }

    private ContextMenu createYearContextMenu() {
        String currentYear = getTreeItem().getValue().getName();
//        MenuItem menuDeleteYearItem = new MenuItem("Delete year");
//        MenuItem menuEditYearItem = new MenuItem("Edit year");
        MenuItem menuAddSourceItem = new MenuItem("Add Source");

        menuAddSourceItem.setOnAction((ActionEvent t) -> {
            Stage sourcePrompt = new Stage();
            sourcePrompt.setTitle("New Source");

            GridPane root = new GridPane();
            root.setAlignment(Pos.CENTER);
            root.setHgap(10);
            root.setVgap(10);
            root.setPadding(new Insets(10, 10, 10, 10));

            root.add(new Label("Source (" + currentYear + "):"), 0, 0);
            TextField sourceTextField = new TextField();
            sourceTextField.setPromptText("Enter Source Title");
            sourceTextField.setPrefWidth(180);
            root.add(sourceTextField, 1, 0);

            Button cancel = new Button("Cancel");
            cancel.setCancelButton(true);
            Button ok = new Button("OK");
            ok.setDefaultButton(true);
            HBox hb = new HBox(10);
            hb.setAlignment(Pos.BOTTOM_RIGHT);
            hb.getChildren().add(ok);
            hb.getChildren().add(cancel);
            root.add(hb, 1, 1);

            ok.setOnAction(event -> {
                if (controller.createSource(currentYear, sourceTextField.getText())) {
                    sourcePrompt.close();
                }
            });
            cancel.setOnAction(event -> sourcePrompt.close());

            sourcePrompt.setScene(new Scene(root, 300, 100));
            sourcePrompt.initModality(Modality.APPLICATION_MODAL);
            sourcePrompt.show();
        });

//        return new ContextMenu(menuEditYearItem, menuDeleteYearItem, new SeparatorMenuItem(), menuAddSourceItem);
        return new ContextMenu(menuAddSourceItem);

    }
}
