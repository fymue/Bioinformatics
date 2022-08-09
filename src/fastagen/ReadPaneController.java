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


public class ReadPaneController implements Initializable
{

    @FXML
    Button rPdone, rPback, rPreadFiles, rPopenDialog, rPplot;

    @FXML
    ChoiceBox<String> rPmodeBox;

    @FXML
    public void close()
    {
        Stage stage = (Stage) rPdone.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void back() throws Exception
    {
        switchPane(rPback, "startPane.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        rPmodeBox.setItems(FXCollections.observableArrayList("Genome", "Protein"));
        rPmodeBox.setValue("Genome");

    }

    private void switchPane(Button b, String fxmlFile) throws Exception
    {
        Parent pane = FXMLLoader.load(getClass().getResource(fxmlFile));
        b.getScene().setRoot(pane);
    }
    
}
