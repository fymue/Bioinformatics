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

public class GenPaneController implements Initializable
{

    @FXML
    Button gPdone, gPback, gPsaveAs, gPstartCalc;

    @FXML
    ChoiceBox<String> gPalphabetBox;

    @FXML
    ChoiceBox<Integer> gPthreadsBox, gPseqsBox;

    @FXML
    TextField gPseqLengthFrom, gPseqLengthTo, gPseqs, gPfastaHeaderPrefix;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        gPalphabetBox.setItems(FXCollections.observableArrayList("Genome", "Protein"));
        gPalphabetBox.setValue("Genome");
        
        int maxThreads = Runtime.getRuntime().availableProcessors();
        int defaultThreads = (int) (maxThreads * 0.75);
        gPthreadsBox.getItems().add(1);
        for (int i=2; i<maxThreads+2; i += 2) gPthreadsBox.getItems().add(i);
        gPthreadsBox.setValue(defaultThreads);

        gPseqsBox.setItems(FXCollections.observableArrayList(1, 5, 10, 50));
        gPseqsBox.setValue(1);

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
    }

    private void switchPane(Button b, String fxmlFile) throws Exception
    {
        Parent pane = FXMLLoader.load(getClass().getResource(fxmlFile));
        b.getScene().setRoot(pane);
    }
    
}
