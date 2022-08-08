package fastagen;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

public class StartPaneController
{
    @FXML
    private Button sPread, sPgen;

    @FXML
    public void switchToReadPane() throws Exception
    {
        switchPane(sPread, "readPane.fxml");
    }

    @FXML
    public void switchToGenPane() throws Exception
    {
        switchPane(sPgen, "genPane.fxml");
    }

    private void switchPane(Button b, String fxmlFile) throws Exception
    {
        Parent pane = FXMLLoader.load(getClass().getResource(fxmlFile));
        b.getScene().setRoot(pane);
    }
    
}
