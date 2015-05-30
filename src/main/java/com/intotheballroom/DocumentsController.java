package com.intotheballroom;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.collections.ObservableMapWrapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
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
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DocumentsController {
    private final File sortedRoot;
    private final File unsortedRoot;
    private final IndexWriter indexWriter;
    private ObservableList<String> years;
    private ObservableMap<String, DanceFamily> danceFamilies;
    private Map<String, ObservableList<String>> yearSourceMap;
    private Map<String, Map<String, ObservableList<FileDescription>>> sourceFileMap;
    private final StringProperty filterTextProperty = new SimpleStringProperty();
    private final SearcherManager searcherManager;

    private ObjectProperty<Map<String, Map<String, Set<String>>>> foundFiles = new SimpleObjectProperty<>(null);
    private List<String> danceFilter;

    public DocumentsController(File sortedRoot, File unsortedRoot) throws IOException {
        filterTextProperty.addListener(this::filterChanged);

        this.sortedRoot = sortedRoot;
        if (!sortedRoot.isDirectory() && !sortedRoot.mkdirs()) {
            throw new IllegalStateException(String.format("Unable to create directory %s", sortedRoot.getAbsolutePath()));
        }

        danceFamilies = new ObservableMapWrapper<>(loadTree(new File(sortedRoot, "dances.xml")));
        //noinspection ConstantConditions
        years = new ObservableListWrapper<>(new ArrayList<>(
                Arrays.stream(sortedRoot.listFiles()).filter(File::isDirectory).filter(file -> !file.getName().startsWith(".")).map(File::getName).collect(Collectors.toList()))).filtered(null);
        yearSourceMap = new HashMap<>();
        sourceFileMap = new HashMap<>();

        this.unsortedRoot = unsortedRoot;

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        indexWriter = new IndexWriter(FSDirectory.open(new File(sortedRoot, ".index")), iwc);

        searcherManager = new SearcherManager(indexWriter, true, null);
    }

    public ObjectProperty<Map<String, Map<String, Set<String>>>> foundFilesProperty() {
        return foundFiles;
    }

    private void filterChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        updateVisibleItems();
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
            File descriptionFile = new File(sortedRoot, year + File.separator + source + File.separator + "description.xml");
            Map<String, FileDescription> availableDescriptions = new HashMap<>();
            if (descriptionFile.isFile()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
                    Document document = documentBuilder.parse(descriptionFile);
                    Element documentElement = document.getDocumentElement();
                    NodeList availableFiles = documentElement.getChildNodes();
                    int availableFilesLength = availableFiles.getLength();
                    for (int i = 0; i < availableFilesLength; i++) {
                        Node availableFile = availableFiles.item(i);
                        if (availableFile.getNodeType() != Node.ELEMENT_NODE ||
                                !availableFile.getNodeName().equals("file")) {
                            continue;
                        }
                        String fileName = availableFile.getAttributes().getNamedItem("name").getNodeValue();
                        FileDescription availableDescription = new FileDescription(fileName);

                        NodeList filePropertyNodes = availableFile.getChildNodes();
                        int filePropertyNodesLength = filePropertyNodes.getLength();
                        for (int j = 0; j < filePropertyNodesLength; j++) {
                            Node filePropertyNode = filePropertyNodes.item(j);
                            if (filePropertyNode.getNodeType() != Node.ELEMENT_NODE ||
                                    !filePropertyNode.getNodeName().equals("property")) {
                                continue;
                            }
                            String type = filePropertyNode.getAttributes().getNamedItem("type").getNodeValue();
                            availableDescription.setProperty(FilePropertyType.valueOf(type.toUpperCase()), filePropertyNode.getTextContent());
                        }

                        availableDescriptions.put(availableDescription.getName(), availableDescription);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            ArrayList<FileDescription> descriptions = new ArrayList<>();
            for (String fileName : list) {
                if (fileName.equals("description.xml"))
                    continue;
                if (availableDescriptions.containsKey(fileName)) {
                    descriptions.add(availableDescriptions.get(fileName));
                } else {
                    descriptions.add(new FileDescription(fileName));
                }
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
                org.apache.lucene.document.Document fileDocument = createDocument(year, source, sourceFile);
                indexWriter.updateDocument(new Term("path", fileDocument.get("path")), fileDocument);
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

    public void reindex() {
        try {
            indexWriter.deleteAll();

            for (Map.Entry<String, ObservableList<String>> yearEntry : yearSourceMap.entrySet()) {
                for (String source : yearEntry.getValue()) {
                    ObservableList<FileDescription> files = getSourceFiles(yearEntry.getKey(), source);
                    for (FileDescription file : files) {
                        org.apache.lucene.document.Document document = createDocument(yearEntry.getKey(), source, file);
                        indexWriter.addDocument(document);
                    }
                }
            }

            indexWriter.commit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private org.apache.lucene.document.Document createDocument(String year, String source, FileDescription file) {
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        document.add(new StringField("path", year + File.separator + source + File.separator + file.getName(), Field.Store.YES));
        Set<FilePropertyType> availableProperties = file.getAvailableProperties();
        if (availableProperties.contains(FilePropertyType.COMMENT)) {
            document.add(new TextField("comment", file.getProperty(FilePropertyType.COMMENT), Field.Store.NO));
        }
        if (availableProperties.contains(FilePropertyType.PAGENAME)) {
            document.add(new TextField("name", file.getProperty(FilePropertyType.PAGENAME), Field.Store.NO));
        }

        if (availableProperties.contains(FilePropertyType.PAGEAUTHORS)) {
            document.add(new TextField("authors", file.getProperty(FilePropertyType.PAGEAUTHORS), Field.Store.NO));
        }
        if (availableProperties.contains(FilePropertyType.DANCES)) {
            String[] dances = file.getProperty(FilePropertyType.DANCES).split(",");
            for (String dance : dances) {
                document.add(new StringField("dance", dance.trim(), Field.Store.NO));
            }
        }
        if (availableProperties.contains(FilePropertyType.YEAR)) {
            String yearProperty = file.getProperty(FilePropertyType.YEAR);
            String monthProperty = file.getProperty(FilePropertyType.MONTH);
            String dayProperty = file.getProperty(FilePropertyType.DAY);
            int yearValue = Integer.parseInt(yearProperty == null ? "0" : yearProperty.trim());
            int monthValue = Integer.parseInt(monthProperty == null ? "0" : monthProperty.trim());
            int dayValue = Integer.parseInt(dayProperty == null ? "0" : dayProperty.trim());
            String date = String.format("%04d%02d%02d", yearValue, monthValue, dayValue);
            document.add(new StringField("date", date, Field.Store.NO));
            System.out.println(date);
        }
        return document;
    }

    public StringProperty filterTextProperty() {
        return filterTextProperty;
    }

    public void setDanceFilter(List<String> dances) {
        this.danceFilter = dances;

        updateVisibleItems();
    }

    private void updateVisibleItems() {
        String newValue = filterTextProperty.getValue().trim();
        IndexSearcher indexSearcher = null;
        Map<String, Map<String, Set<String>>> foundFiles = null;
        if (!newValue.isEmpty() || danceFilter != null) {
            try {
                searcherManager.maybeRefreshBlocking();
                indexSearcher = searcherManager.acquire();
                BooleanQuery booleanQuery = new BooleanQuery();
                if (!newValue.isEmpty()) {
                    MultiFieldQueryParser qp = new MultiFieldQueryParser(Version.LUCENE_40, new String[]{"name", "authors", "comment"}, new StandardAnalyzer(Version.LUCENE_40));
                    qp.setDefaultOperator(QueryParser.Operator.OR);
                    Query query = qp.parse(newValue);
                    booleanQuery.add(query, BooleanClause.Occur.MUST);
                }
                if (danceFilter != null) {
                    BooleanQuery dancesQuery = new BooleanQuery();
                    booleanQuery.add(dancesQuery, BooleanClause.Occur.MUST);
                    for (String dance : danceFilter) {
                        dancesQuery.add(new TermQuery(new Term("dance", dance)), BooleanClause.Occur.SHOULD);
                    }
                }
                TopDocs docs = indexSearcher.search(booleanQuery, 1000000);
                foundFiles = new HashMap<>();
                for (ScoreDoc scoreDoc : docs.scoreDocs) {
                    org.apache.lucene.document.Document doc = indexSearcher.doc(scoreDoc.doc);
                    String path = doc.getField("path").stringValue();
                    String[] split = path.split(Pattern.quote(File.separator));
                    if (split.length == 3) {
                        Map<String, Set<String>> visibleYear;
                        if (foundFiles.containsKey(split[0])) {
                            visibleYear = foundFiles.get(split[0]);
                        } else {
                            visibleYear = new HashMap<>();
                            foundFiles.put(split[0], visibleYear);
                        }

                        Set<String> visibleSource;
                        if (visibleYear.containsKey(split[1])) {
                            visibleSource = visibleYear.get(split[1]);
                        } else {
                            visibleSource = new HashSet<>();
                            visibleYear.put(split[1], visibleSource);
                        }

                        visibleSource.add(split[2]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
            } finally {
                if (indexSearcher != null) {
                    try {
                        searcherManager.release(indexSearcher);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        this.foundFiles.setValue(foundFiles);
    }

    public void renameFile(String year, String source, String oldName, String newName) {
        ObservableList<FileDescription> sourceFiles = getSourceFiles(year, source);

        String extension = "";
        if (oldName.indexOf('.') != -1) {
            extension = oldName.substring(oldName.indexOf('.'));
        }
        if (!newName.endsWith(extension)) {
            newName += extension;
        }

        File oldFile = new File(sortedRoot, year + File.separator + source + File.separator + oldName);
        File newFile = new File(sortedRoot, year + File.separator + source + File.separator + newName);
        if (newFile.isFile() || !oldFile.renameTo(newFile)) {
            return;
        }

        FileDescription sourceFile = null;
        for (Iterator<FileDescription> iterator = sourceFiles.iterator(); iterator.hasNext(); ) {
            sourceFile = iterator.next();
            if (sourceFile.getName().equals(oldName)) {
                iterator.remove();
                break;
            }
        }
        if (sourceFile != null) {
            sourceFile.setName(newName);
            sourceFiles.add(sourceFile);
        }
    }
}
