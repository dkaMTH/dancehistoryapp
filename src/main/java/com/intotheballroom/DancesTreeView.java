package com.intotheballroom;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.util.Callback;

import java.util.HashMap;
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
        setCellFactory(CheckBoxTreeCell.forTreeView(new Callback<TreeItem<String>, ObservableValue<Boolean>>() {
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
        }));

        ObservableMap<String, DanceFamily> danceFamilies = controller.getDanceFamilies();

        for (DanceFamily danceFamily : danceFamilies.values()) {
            TreeItem<String> danceTreeItem = new TreeItem<>(danceFamily.getName());
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

        setRoot(rootItem);
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
