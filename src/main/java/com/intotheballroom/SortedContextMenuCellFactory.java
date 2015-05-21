package com.intotheballroom;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class SortedContextMenuCellFactory implements Callback<TreeView<SortedItem>, TreeCell<SortedItem>> {
    private final DocumentsController controller;

    public SortedContextMenuCellFactory(DocumentsController controller) {
        this.controller = controller;
    }

    @Override
    public TreeCell<SortedItem> call(TreeView<SortedItem> param) {
        return new ContextMenuCell(controller);
    }
}
