package com.intotheballroom;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Function;

/**
 * Created by Dasha on 5/30/2015.
 */
public class ModalDialog extends Stage {
    public ModalDialog(String title, String label, String promptText, Function<String, Boolean> onOk, Runnable onCancel) {
        this.setTitle(title);

        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(10, 10, 10, 10));

        root.add(new Label(label), 0, 0);
        TextField sourceTextField = new TextField();
        sourceTextField.setPromptText(promptText);
        sourceTextField.setPrefWidth(200);
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
            if (onOk.apply(sourceTextField.getText())) {
                close();
            }
        });
        cancel.setOnAction(onCancel != null ? event1 -> onCancel.run() : event -> close());

        this.setScene(new Scene(root, 300, 100));
        this.initModality(Modality.APPLICATION_MODAL);
    }
}
