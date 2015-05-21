package com.intotheballroom;

/**
 * Created by Dasha on 5/12/2015.
 */
public class SortedItem {
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
