package com.intotheballroom;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class YearTreeItem extends TreeItem<SortedItem> {
    private final DocumentsController controller;
    private boolean childrenInitialized;

    public YearTreeItem(SortedItem sortedItem, DocumentsController controller) {
        super(sortedItem);
        this.controller = controller;
    }

    @Override
    public ObservableList<TreeItem<SortedItem>> getChildren() {
        initChildren();
        return super.getChildren();
    }

    private void initChildren() {
        if (childrenInitialized)
            return;
        childrenInitialized = true;
        ObservableList<String> sources = controller.getSources(getValue().getName());
        ObservableList<TreeItem<SortedItem>> children = getChildren();

        for (String source : sources) {
            children.add(new TreeItem<>(new SortedItem(source, SortedItemType.SOURCE)));
        }

        sources.addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                for (String newSource : c.getAddedSubList()) {
                    TreeItem<SortedItem> item = new TreeItem<>(new SortedItem(newSource, SortedItemType.SOURCE));
                    children.add(item);
                }
            }
        });

    }

    @Override
    public boolean isLeaf() {
        initChildren();
        return getChildren().isEmpty();
    }
}
