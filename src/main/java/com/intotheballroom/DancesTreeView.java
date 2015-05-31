package com.intotheballroom;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Dasha on 5/24/2015.
 */
public class DancesTreeView extends TreeView<String> {
    private final Map<String, ObservableValue<Boolean>> selectedDances = new HashMap<>();
    private final DocumentsController controller;

    public DancesTreeView(DocumentsController controller) {
        this.controller = controller;
        setShowRoot(false);
        TreeItem<String> rootItem = new TreeItem<>();
        MenuItem addFamilyItem = new MenuItem("Add family");
        addFamilyItem.setOnAction(
                event -> new ModalDialog("Add family", "Family name", "name", familyName -> {
                    if (!controller.getDanceFamilies().containsKey(familyName)) {
                        controller.getDanceFamilies().put(familyName, new DanceFamily(familyName));
                        return true;
                    } else {
                        return false;
                    }
                }, null).show());
        setCellFactory(new CheckedContextMenuTreeCellFactory(new Callback<TreeItem<String>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(TreeItem<String> param) {
                if (param.equals(rootItem)) {
                    return null;
                } else if (param.getParent().equals(rootItem)) {
                    return selectedDances.get(param.getValue());
                } else {
                    return selectedDances.get(param.getParent().getValue() + " > " + param.getValue());
                }
            }
        }, param -> {
            if (param == null || param == rootItem)
                return null;

            if (param.getParent() == rootItem) {

                MenuItem deleteFamilyItem = new MenuItem("Delete family");
                deleteFamilyItem.setOnAction(event -> controller.getDanceFamilies().remove(param.getValue()));
                MenuItem addStyleItem = new MenuItem("Add style");
                addStyleItem.setOnAction(event -> new ModalDialog("Add style", "Style name", "name", styleName -> {
                    if (!controller.getDanceFamilies().get(param.getValue()).getStyles().contains(styleName)) {
                        controller.getDanceFamilies().get(param.getValue()).getStyles().add(styleName);
                        return true;
                    } else {
                        return false;
                    }
                }, null).show());
                return new ContextMenu(addFamilyItem, addStyleItem, deleteFamilyItem);
            } else {
                MenuItem deleteStyleItem = new MenuItem("Delete style");
                deleteStyleItem.setOnAction(event -> controller.getDanceFamilies().get(param.getParent().getValue()).getStyles().remove(param.getValue()));
                return new ContextMenu(deleteStyleItem);
            }
        }));

        ObservableMap<String, DanceFamily> danceFamilies = controller.getDanceFamilies();

        for (DanceFamily danceFamily : danceFamilies.values()) {
            TreeItem<String> danceTreeItem = createFamiliesItem(danceFamily);
            for (String style : danceFamily.getStyles()) {
                danceTreeItem.getChildren().add(new TreeItem<>(style));
                selectedDances.put(danceFamily.getName() + " > " + style, new SimpleBooleanProperty(false));
            }
            rootItem.getChildren().add(danceTreeItem);
            selectedDances.put(danceFamily.getName(), new SimpleBooleanProperty(false));
        }

        for (ObservableValue<Boolean> value : selectedDances.values()) {
            value.addListener(this::onSelectionChange);
        }

        danceFamilies.addListener((MapChangeListener<String, DanceFamily>) change -> {
            if (change.wasAdded()) {
                rootItem.getChildren().add(createFamiliesItem(change.getValueAdded()));
                rootItem.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            } else {
                for (Iterator<TreeItem<String>> iterator = rootItem.getChildren().iterator(); iterator.hasNext(); ) {
                    TreeItem<String> item = iterator.next();
                    if (item.getValue().equals(change.getKey())) {
                        iterator.remove();
                    }
                }
            }
            controller.saveDanceFamilies();
        });

        setRoot(rootItem);
        setContextMenu(new ContextMenu(addFamilyItem));
    }

    private TreeItem<String> createFamiliesItem(DanceFamily danceFamily) {
        TreeItem<String> result = new TreeItem<>(danceFamily.getName());
        danceFamily.getStyles().addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                for (String removedStyle : c.getRemoved()) {
                    for (Iterator<TreeItem<String>> iterator = result.getChildren().iterator(); iterator.hasNext(); ) {
                        if (iterator.next().getValue().equals(removedStyle)) {
                            iterator.remove();
                        }
                    }
                }
                for (String addedStyle : c.getAddedSubList()) {
                    result.getChildren().add(new TreeItem<>(addedStyle));
                }
                result.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            }
            controller.saveDanceFamilies();
        });
        return result;
    }

    private void onSelectionChange(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        List<String> filter = selectedDances.entrySet().stream()
                .filter(entry -> entry.getValue().getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (filter.isEmpty() || filter.size() == selectedDances.size()) {
            filter = null;
        }
        controller.setDanceFilter(filter);
    }
}
