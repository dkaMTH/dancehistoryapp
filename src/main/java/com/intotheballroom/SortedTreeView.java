package com.intotheballroom;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Created by Dasha on 5/10/2015.
 */
public class SortedTreeView extends TreeView<SortedItem> {
    private final DocumentsController controller;

    public SortedTreeView(DocumentsController controller) {
        this.controller = controller;
        setRoot(new DocumentRootItem());
    }

    private class DocumentRootItem extends TreeItem<SortedItem> {
        private boolean childrenInitialized;

        private DocumentRootItem() {
            super(new SortedItem("Sorted", SortedItemType.ROOT));
            setExpanded(true);
            initChildren();
        }

        private void initChildren() {
            if (childrenInitialized)
                return;
            childrenInitialized = true;
            ObservableList<String> years = controller.getYears();
            ObservableList<TreeItem<SortedItem>> children = getChildren();
            for (String year : years) {
                children.add(new YearTreeItem(new SortedItem(year, SortedItemType.YEAR), controller));
            }

            years.addListener((ListChangeListener<String>) c -> {
                while (c.next()) {
                    for (String newYear : c.getAddedSubList()) {
                        TreeItem<SortedItem> item = new YearTreeItem(new SortedItem(newYear, SortedItemType.YEAR), controller);
                        children.add(item);
                    }
                }
            });
        }

        @Override
        public boolean isLeaf() {
            return getChildren().isEmpty();
        }
    }
}
