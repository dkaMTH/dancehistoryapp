package com.intotheballroom;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.collections.ObservableMapWrapper;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentsController {
    private final File sortedRoot;
    private final File unsortedRoot;
    private ObservableList<String> years;
    private ObservableMap<String, DanceFamily> danceFamilies;
    private Map<String, ObservableList<String>> yearSourceMap;
    private Map<String, Map<String, ObservableList<FileDescription>>> sourceFileMap;

    public DocumentsController(File sortedRoot, File unsortedRoot) {
        this.sortedRoot = sortedRoot;
        if (!sortedRoot.isDirectory() && !sortedRoot.mkdirs()) {
            throw new IllegalStateException(String.format("Unable to create directory %s", sortedRoot.getAbsolutePath()));
        }

        danceFamilies = new ObservableMapWrapper<>(loadTree(new File(sortedRoot, "dances.xml")));
        //noinspection ConstantConditions
        years = new ObservableListWrapper<>(new ArrayList<>(
                Arrays.stream(sortedRoot.listFiles()).filter(File::isDirectory).map(File::getName).collect(Collectors.toList())));
        yearSourceMap = new HashMap<>();
        sourceFileMap = new HashMap<>();

        this.unsortedRoot = unsortedRoot;
    }

    private Map<String, DanceFamily> loadTree(File file) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Map<String, DanceFamily> result = new HashMap<>();
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            NodeList familyNodes = document.getDocumentElement().getChildNodes();
            int familyNodesLength = familyNodes.getLength();
            for (int i = 0; i < familyNodesLength; i++) {
                Node familyNode = familyNodes.item(i);
                if (familyNode.getNodeType() == Node.ELEMENT_NODE && familyNode.getNodeName().equals("family")) {
                    Node familyNameNode = familyNode.getAttributes().getNamedItem("name");
                    if (familyNameNode == null)
                        continue;
                    String familyName = familyNameNode.getNodeValue();
                    if (familyName == null)
                        continue;
                    DanceFamily danceFamily = new DanceFamily(familyName);
                    result.put(familyName, danceFamily);
                    NodeList styleNodes = familyNode.getChildNodes();
                    int styleNodesLength = styleNodes.getLength();
                    for (int j = 0; j < styleNodesLength; j++) {
                        Node styleNode = styleNodes.item(j);
                        if (styleNode.getNodeType() == Node.ELEMENT_NODE && styleNode.getNodeName().equals("style")) {
                            Node styleNameNode = styleNode.getAttributes().getNamedItem("name");
                            if (styleNameNode == null) {
                                continue;
                            }
                            String styleName = styleNameNode.getNodeValue();
                            if (styleName == null)
                                continue;
                            danceFamily.getStyles().add(styleName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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

    public ObservableList<FileDescription> getSourceFiles(String year, String source) {
        final Map<String, ObservableList<FileDescription>> sources;

        if (sourceFileMap.containsKey(year)) {
            sources = sourceFileMap.get(year);
        } else {
            sources = new HashMap<>();
            sourceFileMap.put(year, sources);
        }

        ObservableList<FileDescription> result;
        if (sources.containsKey(source)) {
            result = sources.get(source);
        } else {
            String[] list = new File(sortedRoot, year + File.separator + source).list();
            if (list == null) {
                System.err.printf("Failed to retrieve files from source [%s] and year [%s]%n", source, year);
                return null;
            }
            ArrayList<FileDescription> descriptions = new ArrayList<>();
            for (String fileName : list) {
                descriptions.add(new FileDescription(fileName));
            }
            result = new ObservableListWrapper<>(descriptions);
            sources.put(source, result);
        }

        return result;
    }


    public void addFilesToSource(String source, String year, ArrayList<File> files) {
        ObservableList<FileDescription> sourceFiles = getSourceFiles(year, source);
        for (File file : files) {
            if (file.renameTo(new File(sortedRoot, year + File.separator + source + File.separator + file.getName()))) {
                sourceFiles.add(new FileDescription(file.getName()));
            }
        }
    }

    public File getFile(String year, String source, String fileName) {
        return new File(sortedRoot, year + File.separator + source + File.separator + fileName);
    }


    public void setFileProperty(String year, String source, String fileName, FilePropertyType type, String value) {
        getFileDescription(year, source, fileName).setProperty(type, value);
    }

    public FileDescription getFileDescription(String year, String source, String fileName) {
        ObservableList<FileDescription> sourceFiles = getSourceFiles(year, source);
        for (FileDescription sourceFile : sourceFiles) {
            if (sourceFile.getName().equals(fileName))
                return sourceFile;
        }
        throw new IllegalStateException(String.format("Requested non existing file [%s] from year [%s] and source [%s]", year, source, fileName));
    }

    public ObservableMap<String, DanceFamily> getDanceFamilies() {
        return danceFamilies;
    }

    public boolean saveSource(String year, String source) {
        ObservableList<FileDescription> sourceFiles = getSourceFiles(year, source);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            Document document = dbf.newDocumentBuilder().newDocument();
            Element files = document.createElement("files");
            document.appendChild(files);
            for (FileDescription sourceFile : sourceFiles) {
                if (sourceFile.getAvailableProperties().isEmpty())
                    continue;
                Element file = document.createElement("file");
                file.setAttribute("name", sourceFile.getName());
                for (FilePropertyType propertyType : sourceFile.getAvailableProperties()) {
                    Element property = document.createElement("property");
                    property.setAttribute("type", propertyType.name().toLowerCase());
                    property.setTextContent(sourceFile.getProperty(propertyType));
                    file.appendChild(property);
                }
                files.appendChild(file);
            }


            // Use a Transformer for output
            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer =
                    tFactory.newTransformer();

            DOMSource domSource = new DOMSource(document);
            try (FileOutputStream outputStream = new FileOutputStream(new File(
                    sortedRoot, year + File.separator + source + File.separator + "description.xml"))) {
                StreamResult result = new StreamResult(outputStream);
                transformer.transform(domSource, result);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return false;
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
