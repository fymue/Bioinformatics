package fastagen;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

/**
 * this class serves as the Controller class for the initial scene of the GUI
 */
public class StartPaneController
{
    @FXML
    private Button sPread, sPgen;

    /**
     * switches to the scene associated with the "read" mode of the program
     * @throws Exception
     */
    @FXML
    public void switchToReadPane() throws Exception
    {
        switchPane(sPread, "readPane.fxml");
    }

    /**
     * switches to the scene associated with the "generate" mode of the program
     * @throws Exception
     */
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
