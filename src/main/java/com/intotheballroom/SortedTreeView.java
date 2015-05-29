package com.intotheballroom;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Collection;
import java.util.Iterator;

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
            children.sort((o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName()));

            controller.foundFilesProperty().addListener((observable, oldValue, newValue) -> {
                Collection<String> newYears;
                if (newValue == null) {
                    newYears = controller.getYears();
                } else {
                    newYears = newValue.keySet();
                }

                for (Iterator<TreeItem<SortedItem>> iterator = children.iterator(); iterator.hasNext(); ) {
                    TreeItem<SortedItem> child = iterator.next();
                    if (!newYears.contains(child.getValue().getName())) {
                        iterator.remove();
                    } else {
                        ((YearTreeItem) child).filterChildren(newValue);
                    }
                }

                outer:
                for (String yearName : newYears) {
                    for (TreeItem<SortedItem> child : children) {
                        if (yearName.equals(child.getValue().getName())) {
                            continue outer;
                        }
                    }
                    YearTreeItem newItem = new YearTreeItem(new SortedItem(yearName, SortedItemType.YEAR), controller);
                    newItem.filterChildren(newValue);
                    children.add(newItem);
                }

                children.sort((o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName()));
            });

            years.addListener((ListChangeListener<String>) c -> {
                while (c.next()) {
                    for (String newYear : c.getAddedSubList()) {
                        YearTreeItem item = new YearTreeItem(new SortedItem(newYear, SortedItemType.YEAR), controller);
                        children.add(item);
                    }
                }

                children.sort((o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName()));
            });
        }

        @Override
        public boolean isLeaf() {
            return getChildren().isEmpty();
        }
    }
}
