package com.intotheballroom;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DocumentsController {
    private final File sortedRoot;
    private final File unsortedRoot;
    private ObservableList<String> years;
    private Map<String, ObservableList<String>> yearSourceMap;
    private Map<String, Map<String, ObservableList<String>>> sourceFileMap;

    public DocumentsController(File sortedRoot, File unsortedRoot) {
        this.sortedRoot = sortedRoot;

        years = new ObservableListWrapper<>(new ArrayList<>(Arrays.asList(sortedRoot.list())));
        yearSourceMap = new HashMap<>();
        sourceFileMap = new HashMap<>();

        this.unsortedRoot = unsortedRoot;
    }

    public boolean createYear(String name) {
        boolean succeeded = new File(sortedRoot, name).mkdir();
        if (succeeded) {
            years.add(name);
        }
        return succeeded;
    }

    public boolean createSource(String year, String name) {
        boolean succeeded = new File(sortedRoot, year + "/" + name).mkdir();
        if (succeeded) {
            getSources(year).add(name);
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

    public ObservableList<String> getSources(String year) {
        if (yearSourceMap.containsKey(year)) {
            return yearSourceMap.get(year);
        }
        ObservableListWrapper<String> sources = new ObservableListWrapper<>(
                new ArrayList<>(Arrays.asList(new File(sortedRoot, year).list())));
        yearSourceMap.put(year, sources);
        return sources;
    }

    public ObservableList<String> getSourceFiles(String year, String source) {
        final Map<String, ObservableList<String>> sources;

        if (sourceFileMap.containsKey(year)) {
            sources = sourceFileMap.get(year);
        } else {
            sources = new HashMap<>();
            sourceFileMap.put(year, sources);
        }

        ObservableList<String> result;
        if (sources.containsKey(source)) {
            result = sources.get(source);
        } else {
            String[] list = new File(sortedRoot, year + "/" + source).list();
            if (list == null) {
                System.err.printf("Failed to retrieve files from source [%s] and year [%s]%n", source, year);
            }
            result = new ObservableListWrapper<>(
                    new ArrayList<>(Arrays.asList(list)));
            sources.put(source, result);
        }

        return result;
    }


    public void addFilesToSource(String source, String year, ArrayList<File> files) {
        ObservableList<String> sourceFiles = getSourceFiles(year, source);
        for (File file : files) {
            if (file.renameTo(new File(sortedRoot, year + "/" + source + "/" + file.getName()))) {
                sourceFiles.add(file.getName());
            }
        }
    }
}
