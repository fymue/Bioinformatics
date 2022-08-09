package fastagen;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import java.util.ArrayList;
import java.util.HashMap;

public class GenPaneController implements Initializable
{
    boolean writeProperties = false;

    @FXML
    Button gPdone, gPback, gPsaveAs, gPstartCalc, gPpropYes, gPpropNo;

    @FXML
    ChoiceBox<String> gPalphabetBox, gPthreadsBox, gPseqsBox;

    @FXML
    TextField gPseqLengthFrom, gPseqLengthTo, gPseqs, gPfastaHeaderPrefix;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
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
    public void startCalc()
    {
        // generate the Fasta entries and start the calculations
        gPstartCalc.setVisible(false);
        gPdone.setVisible(true);
        gPsaveAs.setDisable(false);
        String[] args = convertGUIInputToCLIInput();
        InputEvaluator input = new InputEvaluator();
        boolean isValid = input.evaluateInput(new CommandLineParser(args), args);

        FastaProcessor processor;
        if (isValid) processor = new FastaProcessor(input);
    }

    @FXML
    public void writePropsOff() {writeProperties = false; startCalc();}

    @FXML
    public void writePropsOn() {writeProperties = true; startCalc();}
    

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
