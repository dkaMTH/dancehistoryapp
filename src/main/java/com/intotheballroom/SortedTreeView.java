package com.intotheballroom;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Dasha on 5/10/2015.
 */
public class SortedTreeView extends TreeView<String> {
    private final DocumentsController controller;

    public SortedTreeView(DocumentsController controller) {
        this.controller = controller;
        setRoot(new DocumentRootItem());

    }

    private class DocumentRootItem extends TreeItem<String> {
        private final ObservableList<TreeItem<String>> children;

        private DocumentRootItem() {
            super("Sorted");
            setExpanded(true);
            children = new ObservableListWrapper<>(new ArrayList<>());
            ObservableList<String> years = controller.getYears();
            for (String year : years) {
                children.add(new TreeItem<>(year));
            }

            years.addListener((ListChangeListener<String>) c -> {
                ArrayList<TreeItem<String>> added = new ArrayList<>();
                while (c.next()) {
                    for (String newYear : c.getAddedSubList()) {
                        TreeItem<String> item = new TreeItem<>(newYear);
                        children.add(item);
                        added.add(item);
                    }
                }
                Event.fireEvent(this, new TreeModificationEvent<>(expandedItemCountChangeEvent(), this));
            });
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public ObservableList<TreeItem<String>> getChildren() {
            return children.sorted((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        }
    }
}
