package com.intotheballroom;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dasha on 5/23/2015.
 */
public class FileDescription {
    private String name;
    private final Map<FilePropertyType, String> properties = new EnumMap<>(FilePropertyType.class);

    public FileDescription(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<FilePropertyType> getAvailableProperties() {
        return properties.keySet();
    }

    public String getProperty(FilePropertyType type) {
        return properties.get(type);
    }

    public void setProperty(FilePropertyType type, String value) {
        properties.put(type, value);
    }

    public void setName(String name) {
        this.name = name;
    }
}
