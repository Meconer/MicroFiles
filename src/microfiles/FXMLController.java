/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package microfiles;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import static microfiles.Configuration.FILE_AND_PATH_SEPARATOR;

/**
 * FXML Controller class
 *
 * @author mats.andersson
 */
public class FXMLController implements Initializable {

    @FXML
    private TextField searchText;

    @FXML
    private CheckBox fileNameCheckBox;

    @FXML
    private CheckBox pathNameCheckBox;

    @FXML
    private CheckBox requireAllSearchTerms;

    @FXML
    private Label progressLabel;

    @FXML
    private TableView<FileSet> fileTable;

    @FXML
    private TableColumn<FileSet, String> fileNameColumn;

    @FXML
    private TableColumn<FileSet, String> pathColumn;

    @FXML
    private Button updateTableButton;

    List<FileSet> fileData;

    private final String PATH_TO_FILELIST = System.getenv("LOCALAPPDATA") + "\\FileList.data";
    private final String PATH_TO_SEARCH_PLACES_LIST = System.getenv("LOCALAPPDATA") + "\\FilePlaces.data";
    ArrayList<String> fileStringList = new ArrayList<>();

    private static final String[] PATHS_TO_SEARCH = {
        "J:\\nc\\NCDOK",
        "J:\\kundritn",
        "Z:\\Produktion",
        "P:\\Fräs",
        "P:\\Fräsning gamla",
        "P:\\Svarv nya",
        "P:\\Svarvning",
        "G:\\MICROPRECISION AB\\Kunder"};

    private ArrayList<String> placesToSearch;

    private static final String[] EXTENSIONS_TO_SAVE = {};
//        ".nc",
//        ".nch",
//        ".pdf",
//        ".stp",
//        ".step",
//        ".ics",
//        ".dxf",
//        ".h",
//        ".tif",
//        ".png",
//        ".jpg",
//        ".mcam",
//        ".vnc",
//        ".pm"
//    };

    @FXML
    public void upDateFileList() {
        if (Utils.askForOk("Är du säker? Det tar flera minuter", "Uppdatera fillista?")) {
            buildNewFileList();
        }
    }

    @FXML
    void changeSearchPathList() {
        try {
            Runtime.getRuntime().exec("notepad " + PATH_TO_SEARCH_PLACES_LIST);
        } catch (IOException ex) {
            Utils.showError("Något gick fel : " + ex.getMessage());
        }
    }

    @FXML
    public void openInExplorer() {
        var item = fileTable.getSelectionModel().getSelectedItem();
        if (item != null) {
            var selectedPath = fileTable.getSelectionModel().getSelectedItem().getPathName();

            try {
                if (selectedPath != null) {
                    Desktop.getDesktop().open(new File(selectedPath));
                }
            } catch (IOException ex) {
                Utils.showError("Något gick fel : " + ex.getMessage());
            }
        }
    }

    @FXML
    void openSelectedFile() {
        FileSet fileSet = fileTable.getSelectionModel().getSelectedItem();
        if (fileSet != null) {
            String selectedFile = fileSet.getPathName() + "\\" + fileSet.getFileName();
            try {
                Desktop.getDesktop().open(new File(selectedFile));
            } catch (IOException ex) {
                Utils.showError("Något gick fel : " + ex.getMessage());
            }
        }
    }

    private void getPathsToSearch() {
        if (Files.exists(Paths.get(PATH_TO_SEARCH_PLACES_LIST))) {
            readSearchPlacesFromFile();
        } else {
            placesToSearch = new ArrayList<>();
            placesToSearch.addAll(Arrays.asList(PATHS_TO_SEARCH));
            saveSearchPlacesToFile();
        }
    }

    Task fileListUpdateTask = new Task<Integer>() {
        @Override
        protected Integer call() throws Exception {
            int iterations = 0;
            getPathsToSearch();
            String message = "Påbörjar sökning";
            updateMessage(message);
            System.out.println(message);
            for (String pathString : placesToSearch) {
                message = "Söker i " + pathString;
                updateMessage(message);
                System.out.println(message);
                FileList fileList = new FileList(pathString, EXTENSIONS_TO_SAVE);

                List<String> list = fileList.getStringList();
                fileStringList.addAll(list);
                iterations += list.size();
                if (isCancelled()) {
                    break;
                }

                message = "Antal filer genomsökta : " + iterations;
                updateMessage(message);
                System.out.println(message);

            }
            updateMessage("Sökning klar. Lagrat " + iterations + " filer.");
            fileData = new ArrayList<>();
            fileStringList.forEach((fileString) -> {
                int lastFileSeparatorPosition = fileString.lastIndexOf("\\");
                String fileName = fileString.substring(lastFileSeparatorPosition + 1);
                String filePath = fileString.substring(0, lastFileSeparatorPosition);
                FileSet fileSet = new FileSet();
                fileSet.setFileName(fileName);
                fileSet.setPathName(filePath);
                fileData.add(fileSet);
            });

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(PATH_TO_FILELIST, StandardCharsets.UTF_8));
                for (FileSet fs : fileData) {
                    bw.write(fs.getFileName() + Configuration.FILE_AND_PATH_SEPARATOR + fs.getPathName());
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ex) {
                Utils.showError("Fel vid skrivning av " + PATH_TO_FILELIST + ". " + ex.getMessage());
            }
            Platform.runLater(() -> {
                updateTableButton.setVisible(true);
            });
            return iterations;
        }

    };

    private void buildNewFileList() {

        var updateTask = new Thread(fileListUpdateTask);
        updateTask.start();
    }

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println(PATH_TO_FILELIST);
        if (Files.exists(Paths.get(PATH_TO_FILELIST))) {
            readFileListFromFile();
            initTable();
        } else {
            //if (Utils.askForOk("Det finns ingen fillista. Söker igenom för att skapa en ny", "Varning")) {
                //buildNewFileListAndWait();
            //};

        }

        updateTableButton.setVisible(false);

        fileNameCheckBox.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            filterTable(searchText.getText().toLowerCase());
        });

        pathNameCheckBox.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            filterTable(searchText.getText().toLowerCase());
        });

        requireAllSearchTerms.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            filterTable(searchText.getText().toLowerCase());
        });

        progressLabel.textProperty().bind(fileListUpdateTask.messageProperty());

        searchText.setOnAction(e -> {
            String[] searchWords = searchText.getText().split("\\s");
            System.out.println("-------");
            for (String searchWord : searchWords) {
                System.out.println(searchWord + "#");
            }
        });
    }

    @FXML
    private void initTable() {
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("FileName"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("PathName"));

        ObservableList fileTableData = FXCollections.observableArrayList(fileData);
        filteredData = new FilteredList<>(fileTableData, fileSet -> true);
        SortedList<FileSet> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(fileTable.comparatorProperty());
        fileTable.setItems(sortedData);
        updateTableButton.setVisible(false);
        searchText.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTable(newValue.toLowerCase());
        });
    }

    private FilteredList<FileSet> filteredData;

    private void readFileListFromFile() {
        try {
            fileData = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(PATH_TO_FILELIST, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                int separatorPosition = line.indexOf(FILE_AND_PATH_SEPARATOR);
                String fileName = line.substring(0, separatorPosition);
                String pathName = line.substring(separatorPosition + 2);
                FileSet fileSet = new FileSet();
                fileSet.setFileName(fileName);
                fileSet.setPathName(pathName);
                fileData.add(fileSet);
            }
            br.close();
            System.out.println("Read " + fileData.size() + " files from " + PATH_TO_FILELIST);

        } catch (FileNotFoundException ex) {
            Utils.showError("Kan inte läsa in " + PATH_TO_FILELIST + ". Filen finns inte");
            System.exit(0);
        } catch (IOException ex) {
            Utils.showError("Fel vid inläsning av " + PATH_TO_FILELIST + ". " + ex.getMessage());
            System.exit(0);
        }
    }

    private void filterTable(String filterValue) {
        filteredData.setPredicate(fileSet
                -> {
            if (filterValue == null || filterValue.isEmpty()) {
                return true;
            } else {
                String[] searchWords = filterValue.toLowerCase().split("\\s");
                String textToSearchIn = "";
                if (fileNameCheckBox.isSelected()) {
                    textToSearchIn = fileSet.getFileName().toLowerCase();
                }
                if (pathNameCheckBox.isSelected()) {
                    textToSearchIn += fileSet.getPathName().toLowerCase();
                }
                if (!textToSearchIn.isBlank()) {
                    if (requireAllSearchTerms.isSelected()) {
                        Boolean found = true;
                        for (String searchWord : searchWords) {
                            if (!textToSearchIn.contains(searchWord)) {
                                found = false;
                                break;
                            }
                        }
                        if (found) {
                            return true;
                        }
                    } else {
                        Boolean found = false;
                        for (String searchWord : searchWords) {
                            if (textToSearchIn.contains(searchWord)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            return true;
                        }

                    }
                }
                return false;
            }
        });
    }

    private void buildNewFileListAndWait() {
        buildNewFileList();
        while (!fileListUpdateTask.isDone()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }

        }

    }

    private void readSearchPlacesFromFile() {
        try {
            System.out.println("Reading search places from file " + PATH_TO_SEARCH_PLACES_LIST);
            BufferedReader br = new BufferedReader(new FileReader(PATH_TO_SEARCH_PLACES_LIST, StandardCharsets.UTF_8));
            String line;
            placesToSearch = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("//")) {
                    placesToSearch.add(line); // Use only lines without comments
                }
                System.out.println("Place: " + line);
            }
            br.close();
            System.out.println("Read " + fileData.size() + " files from " + PATH_TO_FILELIST);

        } catch (FileNotFoundException ex) {
            Utils.showError("Kan inte läsa in " + PATH_TO_FILELIST + ". Filen finns inte");
            System.exit(0);
        } catch (IOException ex) {
            Utils.showError("Fel vid inläsning av " + PATH_TO_FILELIST + ". " + ex.getMessage());
            System.exit(0);
        }
    }

    private void saveSearchPlacesToFile() {
        System.out.println("Creating search places to file " + PATH_TO_SEARCH_PLACES_LIST);
        try (FileWriter fw = new FileWriter(PATH_TO_SEARCH_PLACES_LIST)) {
            for (String line : placesToSearch) {
                fw.write(line + System.lineSeparator());
            }
            fw.close();
        } catch (IOException ex) {
            Utils.showError("Fel vid skrivande av " + PATH_TO_SEARCH_PLACES_LIST + ". " + ex.getMessage());
            System.exit(0);
        }

    }

}
