package fastagen;

import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * this class serves as the Controller class for the initial scene of the GUI
 */
public class StartPaneController implements Initializable
{
    @FXML private Button sPread, sPgen;

     /**
     * implements the inherided method of the <code>Initializable</code> interface
     * and initializes the scene and fills objects with the appropriate initial values
     * @param url required parameter
     * @param rb required parameter
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        sPread.setTooltip(new Tooltip("Fasta(s) einlesen"));
        sPgen.setTooltip(new Tooltip("Fasta(s) generieren"));
    }

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
