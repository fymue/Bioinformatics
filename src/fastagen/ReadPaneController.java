package fastagen;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.stage.*;
import java.util.*;
import java.io.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import java.net.URL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 * this class serves as the Controller class for the "read mode" scene of the GUI
 */
public class ReadPaneController implements Initializable
{
    private ByteArrayOutputStream stderr;
    private ArrayList<String> inputFiles;
    private int currFile, totalInputFiles;
    private ArrayList<ObservableList<Entry>> calcResults = new ArrayList<>();
    private ArrayList<SequenceCollection<String, Sequence>> inputFastas =  new ArrayList<>();

    @FXML private Button rPdone, rPback, rPstartCalc, rPopenDialog, rPplot,
                         rPprevFile, rPnextFile, rPmaximizeTable, rPsaveToFile;

    @FXML private ChoiceBox<String> rPmodeBox;

    @FXML private ListView rPlistView;

    @FXML private TableView<Entry> rPtableView;

    @FXML private TableColumn<Entry, String> headerCol;

    @FXML private TableColumn<Entry, Integer> gcCol; 
    
    @FXML private TableColumn<Entry, Double> molwCol, meltingtCol;

    @FXML private Label rPcurrFileLabel;


    /**
     * closes the window
     */
    @FXML
    public void close()
    {
        Stage stage = (Stage) rPdone.getScene().getWindow();
        stage.close();
    }

    /**
     * goes back to the initial scene
     */
    @FXML
    public void back() throws Exception
    {
        switchPane(rPback, "startPane.fxml");
    }

    /**
     * opens a file selection window and adds the selected files to a ListView
     */
    @FXML
    public void openFastaFiles()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Fasta-Dateien einlesen");
        List<File> files = chooser.showOpenMultipleDialog(rPopenDialog.getScene().getWindow());

        int totalFilesSelected = files.size();
        String[] filePaths = new String[totalFilesSelected];
        for (int i=0; i<totalFilesSelected; i++) filePaths[i] = files.get(i).toString();
        rPlistView.getItems().addAll(filePaths);
        rPstartCalc.setDisable(false);
    }

    /**
     * read all file paths currently stores in the ListView,
     * parse the Fasta file contents and start the calculations
     */
    @FXML
    public void startCalc()
    {
        String[] args = {"read", "Genome", "--write-properties"};

        ObservableList<String> selected = rPlistView.getItems(); // all items currently in the ListView
        calcResults.clear(); // clear the previous calculation results (if there are any)
        inputFastas.clear();

        totalInputFiles = selected.size();


        if (totalInputFiles != 0)
        {
            InputEvaluator input = new InputEvaluator(args);
            FastaProcessor processor = new FastaProcessor();

            inputFiles = new ArrayList<>(totalInputFiles);

            rPmaximizeTable.setDisable(false);
            rPplot.setDisable(false);
            rPsaveToFile.setDisable(false);

            if (totalInputFiles > 1)
            {
                rPprevFile.setDisable(false);
                rPnextFile.setDisable(false);
            }

            for (int i=0; i<totalInputFiles; i++)
            {
                String filePath = selected.get(i);
                inputFiles.add(filePath);
                
                try
                {
                    // try to parse the provided Fasta file
                    SequenceCollection<String, Sequence> fastaSeqs = processor.readFasta(filePath);
                    inputFastas.add(fastaSeqs);

                    ObservableList<Entry> fastaEntries = FXCollections.observableArrayList();
                    for (String header: fastaSeqs.keySet()) fastaEntries.add(new Entry(header, fastaSeqs.get(header)));
                
                    calcResults.add(fastaEntries);
                }
                catch (Exception e)
                {
                    // if any Exception was thrown, show it in a popup window
                    System.err.println(e); // have to print to stderr first since stderr is read before popup creation
                    createErrorPopup();
                }
            }
            
            currFile = 0;
            updateTableView(rPtableView, currFile); // fill table with results from current input file
        }
    }

    /**
     * reloads the table with the content of the next file in the ListView
     */
    @FXML
    public void nextFile()
    {
        currFile = (currFile == totalInputFiles - 1) ? 0 : currFile + 1;
        updateTableView(rPtableView, currFile);
    }

    /**
     * reloads the table with the content of the previous file in the ListView
     */
    @FXML
    public void prevFile()
    {
        currFile = (currFile == 0) ? totalInputFiles - 1 : currFile - 1;
        updateTableView(rPtableView, currFile);
    }

    /**
     * create a Popup window containing the current
     * TableView (maximized and resizable)
     */
    @FXML
    public void maximizeTable()
    {
        final Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.sizeToScene();

        VBox dialogPane = new VBox();

        TableView tableView = new TableView<Entry>();
        TableColumn<Entry, String> headerCol = new TableColumn<>("Header");
        TableColumn<Entry, Integer> gcCol = new TableColumn<>("GC[%]"); 
        TableColumn<Entry, Double> molwCol = new TableColumn<>("M[g·mol−1]");
        TableColumn<Entry, Double> meltingtCol = new TableColumn<>("Tm[°C]");

        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        initializeTableView(tableView, headerCol, gcCol, molwCol, meltingtCol);
        updateTableView(tableView, currFile);

        dialogPane.getChildren().add(tableView);
        Scene dialogScene = new Scene(dialogPane, 600, 428);
        stage.setScene(dialogScene);
        stage.setTitle(isolateFileName(inputFiles.get(currFile)));
        stage.show();
    }

    /**
     * open a new Window containing a plot of the melting Temperature vs GC%
     * of the currently displayed file in the TableView
     */
    @FXML
    public void plotData()
    {
        final Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.sizeToScene();

        VBox dialogPane = new VBox();

        XYChart.Series series = new XYChart.Series(); 
        series.setName("GC-Gehalt vs Schmelztemperatur");

        // keep track of min. and max. melting temp for x axis scaling
        double maxMeltingTemp = 0.0;
        double minMeltingTemp = Double.POSITIVE_INFINITY;

        for (Entry entry: calcResults.get(currFile))
        {
            int currGCContent = entry.getGcContent();
            double currMeltingTemp = entry.getMeltingTemp();
            maxMeltingTemp = Math.max(maxMeltingTemp, currMeltingTemp);
            minMeltingTemp = Math.min(minMeltingTemp, currMeltingTemp);
            series.getData().add(new XYChart.Data(currMeltingTemp, currGCContent)); 
        }
                
        NumberAxis xAxis = new NumberAxis((int) (minMeltingTemp - 2), (int) (maxMeltingTemp + 2), 10); 
        xAxis.setLabel("Tm[°C]"); 
        NumberAxis yAxis = new NumberAxis(0, 100, 10); 
        yAxis.setLabel("GC[%]");

        LineChart lineChart = new LineChart(xAxis, yAxis);
        lineChart.getData().add(series);

        dialogPane.getChildren().add(lineChart);
        Scene dialogScene = new Scene(dialogPane, 600, 428);
        stage.setScene(dialogScene);
        stage.setTitle(isolateFileName(inputFiles.get(currFile)));
        stage.show();
    }

    @FXML
    public void saveToFile()
    {
        AbstractFasta.writeEntries(inputFastas.get(currFile), genOutputFileName(currFile), true);
    }

    /**
     * implements the inherided method of the <code>Initializable</code> interface
     * and initializes the scene and fills objects with the appropriate initial values
     * @param url required parameter
     * @param rb required parameter
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));


        rPmodeBox.setItems(FXCollections.observableArrayList("Genome"));
        rPmodeBox.setValue("Genome");

        rPstartCalc.setDisable(true);
        rPmaximizeTable.setDisable(true);
        rPnextFile.setDisable(true);
        rPprevFile.setDisable(true);
        rPplot.setDisable(true);
        rPsaveToFile.setDisable(true);

        rPback.setTooltip(new Tooltip("Zurück zur Startseite"));
        rPprevFile.setTooltip(new Tooltip("Ergebnisse der vorherigen Datei"));
        rPnextFile.setTooltip(new Tooltip("Ergebnisse der nächsten Datei"));
        rPplot.setTooltip(new Tooltip("Plotte GC[%] vs. M[g·mol-1]"));
        rPstartCalc.setTooltip(new Tooltip("Berechne GC-Gehalt, mol. Gewicht, Schmelztemp."));
        rPmaximizeTable.setTooltip(new Tooltip("Tabelle auf Fenstergröße maximieren"));


        // intitialize ListView properties (editable, multiple selection)
        rPlistView.setEditable(true);
        rPlistView.setCellFactory(TextFieldListCell.forListView());
        rPlistView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // switch the content of the TableView to the file that was clicked on in the ListView
        rPlistView.setOnMouseClicked(e ->
        {
            String selectedFile = (String) rPlistView.getSelectionModel().getSelectedItem();
            if (selectedFile != null)
            {   
                currFile = inputFiles.indexOf(selectedFile);
                updateTableView(rPtableView, currFile);
            }
        });

        // add Key listener to ListView to delete selected entries on delete key
        rPlistView.setOnKeyPressed(e ->
        {
            ObservableList<String> selected = rPlistView.getSelectionModel().getSelectedItems();
            if (selected != null && e.getCode() == KeyCode.DELETE)
            {
                if (selected.size() == rPlistView.getItems().size())
                {
                    rPstartCalc.setDisable(true);
                    rPmaximizeTable.setDisable(true);
                    rPplot.setDisable(true); 
                    rPsaveToFile.setDisable(true);
                }
                if (rPlistView.getItems().size() - selected.size() <= 1)
                {
                    rPprevFile.setDisable(true);
                    rPnextFile.setDisable(true);
                }

                // also delete the calc results of the files that should be removed
                for (String filePath: selected)
                {
                    int fileIndex = inputFiles.indexOf(filePath);
                    inputFiles.remove(fileIndex);
                    calcResults.remove(fileIndex);
                }

                currFile = (inputFiles.size() == 0) ? -1 : 0; // if there are any files remaining, set currFile to 1st in list
                updateTableView(rPtableView, currFile);

                // remove the selected items from the ListView
                rPlistView.getItems().removeAll(selected);
            }
        });

        // initialize TableView for display of sequence properties
        initializeTableView(rPtableView, headerCol, gcCol, molwCol, meltingtCol);;
    }

    private void switchPane(Button b, String fxmlFile) throws Exception
    {
        Parent pane = FXMLLoader.load(getClass().getResource(fxmlFile));
        b.getScene().setRoot(pane);
    }

    private void createErrorPopup()
    {
        // create a popup window displaying an error message
        String errorMsg = stderr.toString(); // read error from stderr buffer
        stderr.reset(); // clear the buffer in case a new error message appears

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null); // prevent display of header text

        TextArea area = new TextArea(errorMsg);

        area.setWrapText(true);
        area.setEditable(false);
        alert.getDialogPane().setContent(area);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private void initializeTableView(TableView tableView, TableColumn<Entry, String> headerCol, TableColumn<Entry, Integer> gcCol,
                                     TableColumn<Entry, Double> molwCol, TableColumn<Entry, Double> meltingtCol)
    {
        headerCol.setCellValueFactory(new PropertyValueFactory<Entry, String>("header"));
        gcCol.setCellValueFactory(new PropertyValueFactory<Entry, Integer>("gcContent"));
        molwCol.setCellValueFactory(new PropertyValueFactory<Entry, Double>("molWeight"));
        meltingtCol.setCellValueFactory(new PropertyValueFactory<Entry, Double>("meltingTemp"));
        tableView.getColumns().addAll(headerCol, gcCol, molwCol, meltingtCol);
    }

    private void updateTableView(TableView tableView, int currFile)
    {
        if (currFile != -1) // check if there still are calc results of a file left
        {
            String currFileName = isolateFileName(inputFiles.get(currFile));
            rPplot.setTooltip(new Tooltip("Plotte GC[%] vs. M[g·mol-1] für Datei " + currFileName));
            rPcurrFileLabel.setText(currFileName);
            rPcurrFileLabel.setTooltip(new Tooltip(inputFiles.get(currFile)));
            rPsaveToFile.setTooltip(new Tooltip("Speichern der berechneten Werte unter " + isolateFileName(genOutputFileName(currFile))));
            tableView.getItems().clear();
            tableView.getItems().addAll(calcResults.get(currFile));
        }
        else
        {
            rPplot.setTooltip(new Tooltip("Plotte GC[%] vs. M[g·mol-1]"));
            rPcurrFileLabel.setText("");
            rPcurrFileLabel.setTooltip(new Tooltip(""));
            tableView.getItems().clear();
        }
    }

    private String isolateFileName(String filePath) {return filePath.substring(filePath.lastIndexOf("/")+1);}

    private String genOutputFileName(int currFile)
    {
        String currFileName = inputFiles.get(currFile);
        String newFileName = currFileName.substring(0, currFileName.indexOf(".")) + "_parsed.fna";
        return newFileName;
    }
}
