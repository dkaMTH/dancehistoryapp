package com.intotheballroom;

import javafx.scene.control.TreeItem;

import java.util.Comparator;

/**
 * Created by Dasha on 5/12/2015.
 */
public class SortedItem {
    public final static Comparator<TreeItem<SortedItem>> TREEITEM_COMPARATOR =
            (o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

    private String name;
    private SortedItemType type;

    public SortedItem(String name, SortedItemType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public SortedItemType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }
}
