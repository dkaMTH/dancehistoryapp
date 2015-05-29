package com.intotheballroom;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class YearTreeItem extends TreeItem<SortedItem> {
    private final DocumentsController controller;
    private final SortedItem item;
    private boolean childrenInitialized;
    private Map<String, Set<String>> visibleChildren;

    public YearTreeItem(SortedItem sortedItem, DocumentsController controller) {
        super(sortedItem);
        this.item = sortedItem;
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
            if (visibleChildren == null || visibleChildren.containsKey(source)) {
                children.add(new TreeItem<>(new SortedItem(source, SortedItemType.SOURCE)));
            }
        }
        getChildren().sort(SortedItem.TREEITEM_COMPARATOR);

        sources.addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                for (String newSource : c.getAddedSubList()) {
                    if (visibleChildren == null || visibleChildren.containsKey(newSource)) {
                        TreeItem<SortedItem> item = new TreeItem<>(new SortedItem(newSource, SortedItemType.SOURCE));
                        children.add(item);
                    }
                }
            }
            getChildren().sort(SortedItem.TREEITEM_COMPARATOR);
        });
    }

    @Override
    public boolean isLeaf() {
        initChildren();
        return getChildren().isEmpty();
    }

    public void filterChildren(Map<String, Map<String, Set<String>>> visibleChildren) {
        this.visibleChildren = visibleChildren == null ? null : visibleChildren.get(item.getName());
        if (!childrenInitialized)
            return;

        Collection<String> sources;
        if (visibleChildren == null) {
            sources = controller.getSources(getValue().getName());
        } else {
            sources = visibleChildren.keySet();
        }

        for (Iterator<TreeItem<SortedItem>> iterator = getChildren().iterator(); iterator.hasNext(); ) {
            TreeItem<SortedItem> child = iterator.next();
            if (!sources.contains(child.getValue().getName()))
                iterator.remove();
        }

        outer:
        for (String source : sources) {
            for (TreeItem<SortedItem> treeItem : getChildren()) {
                if (treeItem.getValue().getName().equals(source))
                    continue outer;
            }
            getChildren().add(new TreeItem<>(new SortedItem(source, SortedItemType.SOURCE)));
        }

        getChildren().sort(SortedItem.TREEITEM_COMPARATOR);
    }
}
