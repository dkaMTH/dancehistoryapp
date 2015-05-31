package com.intotheballroom;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.util.Callback;

/**
 * Created by Dasha on 5/30/2015.
 */
public class CheckedContextMenuTreeCellFactory implements Callback<TreeView<String>, TreeCell<String>> {
    private final Callback<TreeView<String>, TreeCell<String>> impl;
    private final Callback<TreeItem<String>, ContextMenu> menu;

    public CheckedContextMenuTreeCellFactory(Callback<TreeItem<String>, ObservableValue<Boolean>> checkedCallback,
                                             Callback<TreeItem<String>, ContextMenu> menuCallback) {
        this.menu = menuCallback;
        impl = CheckBoxTreeCell.forTreeView(checkedCallback);
    }

    @Override
    public TreeCell<String> call(TreeView<String> param) {
        TreeCell<String> result = impl.call(param);
        result.treeItemProperty().addListener((observable, oldValue, newValue) -> {
            result.setContextMenu(menu.call(newValue));
        });
        return result;
    }
}
