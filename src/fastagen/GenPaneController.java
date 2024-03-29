package fastagen;

import javafx.fxml.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.net.URL;
import java.util.*;
import java.io.*;

/**
 * this class serves as the Controller class for the "read mode" scene of the GUI
 */
public class GenPaneController implements Initializable
{
    private FastaProcessor processor;
    private ByteArrayOutputStream stdout, stderr;
    private boolean writeProperties = false;
    private InputEvaluator input = new InputEvaluator();

    @FXML private Button gPdone, gPback, gPsaveAs, gPstartCalc, gPpropYes, gPpropNo;

    @FXML private ChoiceBox<String> gPalphabetBox, gPthreadsBox, gPseqsBox;

    @FXML private TextField gPseqLengthFrom, gPseqLengthTo, gPseqs, gPfastaHeaderPrefix;

    @FXML private TextArea gPtextArea;

    @FXML private Label gPpropLabel;

    /**
     * implements the inherided method of the <code>Initializable</code> interface
     * and initializes the scene and fills objects with the appropriate initial values
     * @param url required parameter
     * @param rb required parameter
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        stdout = new ByteArrayOutputStream();
        stderr = new ByteArrayOutputStream();

        // fill ChoiceBoxes and TextFields with initial values
        gPalphabetBox.setItems(FXCollections.observableArrayList("Genome", "Protein"));
        gPalphabetBox.setValue("Genome");
        
        int maxThreads = Runtime.getRuntime().availableProcessors();
        int defaultThreads = (int) (maxThreads * 0.75);
        gPthreadsBox.getItems().add("1");
        for (int i=2; i<maxThreads+2; i += 2) gPthreadsBox.getItems().add(Integer.toString(i));
        gPthreadsBox.setValue(Integer.toString(defaultThreads));

        gPseqsBox.setItems(FXCollections.observableArrayList("1", "5", "10", "50"));

        gPsaveAs.setDisable(true);
        gPdone.setVisible(false);

        gPstartCalc.setTooltip(new Tooltip("Starte Sequenz-Generierung"));
        gPback.setTooltip(new Tooltip("Zurück zur Startseite"));
        gPpropLabel.setTooltip(new Tooltip("Berechne GC-Gehalt, mol. Gewicht, Schmelztemp."));
        gPsaveAs.setTooltip(new Tooltip("Speichern im Fasta-Format unter"));

        // set stdout and stderr to custom PrintStream
        System.setErr(new PrintStream(stderr));
        System.setOut(new PrintStream(stdout));
    }

    /**
     * closes the window
     */
    @FXML
    public void close()
    {
        Stage stage = (Stage) gPdone.getScene().getWindow();
        stage.close();
    }

    /**
     * goes back to the initial scene
     */
    @FXML
    public void back() throws Exception
    {
        switchPane(gPback, "startPane.fxml");
    }

    /**
     * saves the generated Fasta entries to a file using a FileSaveDialog
     */
    @FXML
    public void saveOutput()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Speichern unter");
        String outputFile = chooser.showSaveDialog(gPsaveAs.getScene().getWindow()).toString();
        AbstractFasta.writeEntries(processor.fastaEntries, outputFile, writeProperties);

        // if an error occured during the Fasta writing process, create a popup
        if (stderr.size() != 0) createErrorPopup();
    }

    /**
     * calculate the sequence properties and store them
     */
    @FXML
    public void startCalc() throws IOException
    {
        // generate the Fasta entries and start the calculations
        gPtextArea.clear();
        stdout.reset(); // clear the stdout buffer to not print previous results

        String[] args = convertGUIInputToCLIInput();
        boolean isValid = input.evaluateInput(new CommandLineParser(args), args);

        if (isValid)
        {
            processor = new FastaProcessor(input);
            gPstartCalc.setVisible(false);
            gPdone.setVisible(true);
            gPsaveAs.setDisable(false);
            gPtextArea.setText(stdout.toString());
        }
        else
        {
            /*
             * if an exception was thrown/error message was printed to stderr,
             * read the buffer and create a popup displaying the error message
             */
            createErrorPopup();
        }
    }

    /**
     * turn off the option to calculate/write sequence properties
     */
    @FXML
    public void writePropsOff() {writeProperties = false; gPdone.setVisible(false); gPstartCalc.setVisible(true);}

    /**
     * turn on the option to calculate/write sequence properties
     */
    @FXML
    public void writePropsOn() {writeProperties = true; gPdone.setVisible(false); gPstartCalc.setVisible(true);}
    

    private void switchPane(Button b, String fxmlFile) throws Exception
    {
        Parent pane = FXMLLoader.load(getClass().getResource(fxmlFile));
        b.getScene().setRoot(pane);
    }

    /**
     * convert the input from the various GUI fields to CLI input used by
     * the CLI mode of this program in order to reuse the previously written methods
     * @return the GUI input converted to classic CLI input
     */
    private String[] convertGUIInputToCLIInput()
    {
        // convert the input from the GUI to CLI input so it can be evaluated
        // using the existing InputEvaluator class originally used from CLI input evaluation

        ArrayList<String> args = new ArrayList<>();
        args.add("generate");
        String seqFrom = "";
        String seqTo = "";

        HashMap<TextField, String> textFieldValues = new HashMap<>();
        textFieldValues.put(gPseqLengthFrom, "-lf");
        textFieldValues.put(gPseqLengthTo, "-lt");
        textFieldValues.put(gPseqs, "-e");
        textFieldValues.put(gPfastaHeaderPrefix, "--header");

        args.add(gPalphabetBox.getValue());


        for (TextField textField: textFieldValues.keySet())
        {
            String val = textField.getText().isEmpty() ? textField.getPromptText() : textField.getText();
            String flag = textFieldValues.get(textField);

            if (flag.equals("-lf")) seqFrom = val;
            else if (flag.equals("-lt")) seqTo = val;
            else if (flag.equals("-e") && gPseqsBox.getValue() != null)
            {
                args.add("-e");
                args.add(gPseqsBox.getValue());
            }
            else
            {
                args.add(flag);
                args.add(val);
            }
        }

        args.add("-l");
        args.add(seqFrom + ".." + seqTo);

        args.add("-t");
        args.add(gPthreadsBox.getValue());

        if (writeProperties) args.add("--write-properties");

        args.add("-q"); // always run quietly/non-verbose in GUI mode

        return args.toArray(new String[args.size()]);
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
    
}