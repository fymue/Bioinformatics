package fastagen;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.stage.FileChooser;
import java.io.PrintStream;
import java.io.IOException;
import java.io.OutputStream;
import javafx.application.Platform;

public class GenPaneController implements Initializable
{
    private FastaProcessor processor;
    private PrintStream ps;
    private boolean writeProperties = false;
    private InputEvaluator input = new InputEvaluator();

    @FXML
    Button gPdone, gPback, gPsaveAs, gPstartCalc, gPpropYes, gPpropNo;

    @FXML
    ChoiceBox<String> gPalphabetBox, gPthreadsBox, gPseqsBox;

    @FXML
    TextField gPseqLengthFrom, gPseqLengthTo, gPseqs, gPfastaHeaderPrefix;

    @FXML TextArea gPtextArea;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        ps = new PrintStream(new Console(gPtextArea));

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

        System.setOut(ps);
        System.setErr(ps);
    }

    @FXML
    public void close()
    {
        Stage stage = (Stage) gPdone.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void back() throws Exception
    {
        switchPane(gPback, "startPane.fxml");
    }

    @FXML
    public void saveOutput()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Speichern unter");
        String outputFile = chooser.showSaveDialog(gPsaveAs.getScene().getWindow()).toString();
        AbstractFasta.writeEntries(processor.fastaEntries, outputFile, writeProperties);
    }

    @FXML
    public void startCalc()
    {
        // generate the Fasta entries and start the calculations
        gPtextArea.clear();
        gPstartCalc.setVisible(false);
        gPdone.setVisible(true);
        gPsaveAs.setDisable(false);

        String[] args = convertGUIInputToCLIInput();
        boolean isValid = input.evaluateInput(new CommandLineParser(args), args);

        if (isValid) processor = new FastaProcessor(input);
    }

    @FXML
    public void writePropsOff() {writeProperties = false; gPdone.setVisible(false); gPstartCalc.setVisible(true);}

    @FXML
    public void writePropsOn() {writeProperties = true; gPdone.setVisible(false); gPstartCalc.setVisible(true);}
    

    private void switchPane(Button b, String fxmlFile) throws Exception
    {
        Parent pane = FXMLLoader.load(getClass().getResource(fxmlFile));
        b.getScene().setRoot(pane);
    }

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
    
}

class Console extends OutputStream
{
    private TextArea console;

    public Console(TextArea console)
    {
        this.console = console;
    }

    public void appendText(String valueOf)
    {
        Platform.runLater(() -> console.appendText(valueOf));
    }

    public void write(int b) throws IOException
    {
        appendText(String.valueOf((char)b));
    }
}

