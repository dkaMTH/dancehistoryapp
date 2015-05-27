package com.intotheballroom;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by Dasha on 5/25/2015.
 */
public class DanceFamily {
    private final String name;
    private final ObservableList<String> styles = FXCollections.observableArrayList();

    public DanceFamily(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ObservableList<String> getStyles() {
        return styles;
    }
}
