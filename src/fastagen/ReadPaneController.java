package fastagen;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.PopupWindow;
import javafx.stage.Modality;

import java.net.URL;

import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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

    @FXML private Button rPdone, rPback, rPstartCalc, rPopenDialog, rPplot, rPprevFile, rPnextFile, rPmaximizeTable;

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

        totalInputFiles = selected.size();


        if (totalInputFiles != 0)
        {
            InputEvaluator input = new InputEvaluator(args);
            FastaProcessor processor = new FastaProcessor();

            inputFiles = new ArrayList<>(totalInputFiles);
            currFile = 0;

            rPmaximizeTable.setDisable(false);

            if (totalInputFiles > 1)
            {
                rPprevFile.setDisable(false);
                rPnextFile.setDisable(false);
            }

            for (int i=0; i<totalInputFiles; i++)
            {
                String filePath = selected.get(i);
                String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                inputFiles.add(fileName);
                
                try
                {
                    // try to parse the provided Fasta file
                    SequenceCollection<String, Sequence> fastaSeqs = processor.readFasta(filePath);

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
            
            rPcurrFileLabel.setText(inputFiles.get(currFile));
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
        rPcurrFileLabel.setText(inputFiles.get(currFile));
        updateTableView(rPtableView, currFile);
    }

    /**
     * reloads the table with the content of the previous file in the ListView
     */
    @FXML
    public void prevFile()
    {
        currFile = (currFile == 0) ? totalInputFiles - 1 : currFile - 1;
        rPcurrFileLabel.setText(inputFiles.get(currFile));
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
        stage.initOwner(rPmaximizeTable.getScene().getWindow());
        stage.sizeToScene();
        VBox dialogPane = new VBox();
        TableView tableView = new TableView<Entry>();
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        initializeTableView(tableView);
        updateTableView(tableView, currFile);
        dialogPane.getChildren().add(tableView);
        Scene dialogScene = new Scene(dialogPane, 600, 428);
        stage.setScene(dialogScene);
        stage.show();
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

        // intitialize ListView properties (editable, multiple selection)
        rPlistView.setEditable(true);
        rPlistView.setCellFactory(TextFieldListCell.forListView());
        rPlistView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
                }

                if (rPlistView.getItems().size() - selected.size() <= 1)
                {
                    rPprevFile.setDisable(true);
                    rPnextFile.setDisable(true);
                }

                rPlistView.getItems().removeAll(selected);
                
            }
        });

        // initialize TableView for display of sequence properties
        initializeTableView(rPtableView);
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

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null); // prevent display of header text

        TextArea area = new TextArea(errorMsg);

        area.setWrapText(true);
        area.setEditable(false);
        alert.getDialogPane().setContent(area);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private void initializeTableView(TableView tableView)
    {
        headerCol.setCellValueFactory(new PropertyValueFactory<Entry, String>("header"));
        gcCol.setCellValueFactory(new PropertyValueFactory<Entry, Integer>("gcContent"));
        molwCol.setCellValueFactory(new PropertyValueFactory<Entry, Double>("molWeight"));
        meltingtCol.setCellValueFactory(new PropertyValueFactory<Entry, Double>("meltingTemp"));
        tableView.getColumns().addAll(headerCol, gcCol, molwCol, meltingtCol);
    }

    private void updateTableView(TableView tableView, int currFile)
    {
        tableView.getItems().clear();
        tableView.getItems().addAll(calcResults.get(currFile));
    }
}
