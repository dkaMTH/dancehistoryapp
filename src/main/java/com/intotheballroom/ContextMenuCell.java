package com.intotheballroom;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ContextMenuCell extends TreeCell<String> {
    public ContextMenuCell(DocumentsController controller) {
        MenuItem menuItem = new MenuItem("Add year");
        menuItem.setOnAction((ActionEvent t) -> {
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
            //TODO:add later
        });
        setContextMenu(new ContextMenu(menuItem));
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(item);
    }
}
