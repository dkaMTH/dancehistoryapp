package com.intotheballroom;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dasha on 5/17/2015.
 */
public class DragCellFactory implements Callback<TreeView<String>, TreeCell<String>> {

    @Override
    public TreeCell<String> call(TreeView<String> param) {
        return new DragCell(param);
    }

    private class DragCell extends TreeCell<String> {
        public DragCell(TreeView<String> param) {
            setOnDragDetected(event -> {
                ObservableList<TreeItem<String>> selectedItems = param.getSelectionModel().getSelectedItems();
                Dragboard dragboard = this.startDragAndDrop(TransferMode.MOVE);

                ClipboardContent content = new ClipboardContent();
                List<File> files = new ArrayList<File>();
                for (TreeItem<String> selectedItem : selectedItems) {
                    files.add(((FileTreeView.FileTreeItem) selectedItem).getFile());
                }

                content.putFiles(files);
                dragboard.setContent(content);

                event.consume();
            });
            setOnDragDone(event -> {
                ObservableList<TreeItem<String>> selectedItems = param.getSelectionModel().getSelectedItems();
                for (TreeItem<String> selectedItem : selectedItems) {
                    ((FileTreeView.FileTreeItem) selectedItem).update();
                    ((FileTreeView.FileTreeItem) selectedItem.getParent()).update();
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                setText(item);
            } else {
                setText(null);
            }
        }
    }
}
