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


/**
 * this class serves as the Controller class for the "read mode" scene of the GUI
 */
public class ReadPaneController implements Initializable
{

    @FXML
    Button rPdone, rPback, rPreadFiles, rPopenDialog, rPplot;

    @FXML
    ChoiceBox<String> rPmodeBox;

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
     * implements the inherided method of the <code>Initializable</code> interface
     * and initializes the scene and fills objects with the appropriate initial values
     * @param url required parameter
     * @param rb required parameter
     */
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
