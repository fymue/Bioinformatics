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

import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.net.URL;

import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.List;

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
    ByteArrayOutputStream stderr;
    @FXML
    Button rPdone, rPback, rPstartCalc, rPopenDialog, rPplot;

    @FXML
    ChoiceBox<String> rPmodeBox;

    @FXML
    ListView rPlistView;


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
        HashMap<String, SequenceCollection<String, Sequence>> calcResults = new HashMap<>();

        String[] args = {"read", "Genome", "--write-properties"};

        ObservableList<String> selected = rPlistView.getItems(); // all items currently in the ListView

        if (selected.size() != 0)
        {
            InputEvaluator input = new InputEvaluator(args);
            FastaProcessor processor = new FastaProcessor();

            for (String filePath: selected)
            {
                String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                
                try
                {
                    // try to parse the provided Fasta file
                    SequenceCollection<String, Sequence> fastaSeqs = processor.readFasta(filePath);
                    calcResults.put(fileName, fastaSeqs);
                }
                catch (Exception e)
                {
                    // if any Exception was thrown, show it in a popup window
                    System.err.println(e); // have to print to stderr first since stderr is read before popup creation
                    createErrorPopup();
                }
            }
        }
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
                rPlistView.getItems().removeAll(selected);
            }
        });

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
    
}
