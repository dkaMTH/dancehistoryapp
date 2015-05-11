package com.intotheballroom;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class SortedContextMenuCellFactory implements Callback<TreeView<String>, TreeCell<String>> {
    private final DocumentsController controller;

    public SortedContextMenuCellFactory(DocumentsController controller) {
        this.controller = controller;
    }

    @Override
    public TreeCell<String> call(TreeView<String> param) {
        return new ContextMenuCell(controller);
    }
}
