package fastagen;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.fxml.Initializable;

public class ReadPaneController
{

    @FXML
    Button rPdone, rPback;

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

    private void switchPane(Button b, String fxmlFile) throws Exception
    {
        Parent pane = FXMLLoader.load(getClass().getResource(fxmlFile));
        b.getScene().setRoot(pane);
    }
    
}
