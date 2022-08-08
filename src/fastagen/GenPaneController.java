package fastagen;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
    Button gPdone, gPback;

    @FXML
    ChoiceBox<String> gPalphabetBox;

    @FXML
    ChoiceBox<Integer> gPthreadsBox, gPseqsBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        gPalphabetBox.setItems(FXCollections.observableArrayList("Genome", "Protein"));
        gPthreadsBox.setItems(FXCollections.observableArrayList(1, 2, 4, 8));
        gPseqsBox.setItems(FXCollections.observableArrayList(1, 5, 10, 50));
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

    private void switchPane(Button b, String fxmlFile) throws Exception
    {
        Parent pane = FXMLLoader.load(getClass().getResource(fxmlFile));
        b.getScene().setRoot(pane);
    }
    
}
