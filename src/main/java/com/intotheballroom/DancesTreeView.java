package com.intotheballroom;

import javafx.collections.ObservableMap;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;

/**
 * Created by Dasha on 5/24/2015.
 */
public class DancesTreeView extends TreeView<String> {

    public DancesTreeView(DocumentsController controller) {
        setShowRoot(false);
        setCellFactory(CheckBoxTreeCell.forTreeView());
        TreeItem<String> rootItem = new TreeItem<>();

        ObservableMap<String, DanceFamily> danceFamilies = controller.getDanceFamilies();

        for (DanceFamily danceFamily : danceFamilies.values()) {
            TreeItem<String> danceTreeItem = new TreeItem<>(danceFamily.getName());
            for (String style : danceFamily.getStyles()) {
                danceTreeItem.getChildren().add(new TreeItem<>(style));
            }
            rootItem.getChildren().add(danceTreeItem);
        }

        setRoot(rootItem);
    }
}
