package com.intotheballroom;

import java.io.File;
import java.util.*;

public class ListStuff {
    public static Map<String, Integer> extensions = new HashMap<String, Integer>();

    public static void main(String[] args) {
        File f = new File(args[0]);

        listExtensions(f);

        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(extensions.entrySet());

        list.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return Integer.compare(o1.getValue(), o2.getValue());
            }
        });

        for (Map.Entry<String, Integer> extension : list) {
            System.out.println(extension.getKey() + " - " + extension.getValue());
        }
    }

    private static void listExtensions(File f) {
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                listExtensions(file);
            }
        } else {
            if (f.getName().contains(".")) {
                String extension = f.getName().substring(f.getName().lastIndexOf(".")).toLowerCase();
                if (extensions.containsKey(extension)) {
                    extensions.put(extension, extensions.get(extension) + 1);
                } else {
                    extensions.put(extension.toLowerCase(), 1);
                }
            }
        }
    }
}
