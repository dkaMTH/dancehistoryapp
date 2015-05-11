package com.intotheballroom;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.util.ArrayList;

public class FileTreeView extends TreeView<String> {
    private final File root;

    public FileTreeView(File root, boolean expandRoot) {
        super(new FileTreeItem(root, expandRoot));
        this.root = root;
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


    }



    private static class FileTreeItem extends TreeItem<String> {
        // We cache whether the File is a leaf or not. A File is a leaf if
        // it is not a directory and does not have any files contained within
        // it. We cache this as isLeaf() is called often, and doing the
        // actual check on File is expensive.
        private boolean isLeaf;

        // We do the children and leaf testing only once, and then set these
        // booleans to false so that we do not check again during this
        // run. A more complete implementation may need to handle more
        // dynamic file system situations (such as where a folder has files
        // added after the TreeView is shown). Again, this is left as an
        // exercise for the reader.
        private boolean isFirstTimeChildren = true;
        private boolean isFirstTimeLeaf = true;


        private final File file;

        public FileTreeItem(File file, boolean expandRoot) {
            super(file.getName());
            this.file = file;
            setExpanded(expandRoot);
        }

        @Override
        public ObservableList<TreeItem<String>> getChildren() {
            if (isFirstTimeChildren) {
                isFirstTimeChildren = false;

                // First getChildren() call, so we actually go off and
                // determine the children of the File contained in this TreeItem.
                super.getChildren().setAll(buildChildren());
            }
            return super.getChildren();
        }

        private ObservableListWrapper<TreeItem<String>> buildChildren() {

            File[] children = file.listFiles();

            ArrayList<TreeItem<String>> items = new ArrayList<TreeItem<String>>();

            for (File child : children) {
                items.add(new FileTreeItem(child, false));
            }

            return new ObservableListWrapper<TreeItem<String>>(items);
        }


        @Override public boolean isLeaf() {
            if (isFirstTimeLeaf) {
                isFirstTimeLeaf = false;
                isLeaf = file.isFile();
            }

            return isLeaf;
        }
    }
}
