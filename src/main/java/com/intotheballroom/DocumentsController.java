package com.intotheballroom;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class DocumentsController {
    private final File sortedRoot;
    private final File unsortedRoot;
    private ObservableList<String> years;

    public DocumentsController(File sortedRoot, File unsortedRoot) {
        this.sortedRoot = sortedRoot;

        years = new ObservableListWrapper<>(new ArrayList<>(Arrays.asList(sortedRoot.list())));

        this.unsortedRoot = unsortedRoot;
    }

    public boolean createYear(String name) {
        boolean succeeded = new File(sortedRoot, name).mkdir();
        if (succeeded) {
            years.add(name);
        }
        return succeeded;
    }

    public File getSortedRoot() {
        return sortedRoot;
    }

    public File getUnsortedRoot() {
        return unsortedRoot;
    }

    public ObservableList<String> getYears() {
        return years;
    }
}
