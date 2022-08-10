package fastagen;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

/**
 * This class reads the required program parameters provided 
 * via the command line/GUI and executes the requested tasks
 */
public class Main extends Application
{
    private Stage primaryStage;

    /**
     * main method
     * @param args standard main input parameter
     */
    public static void main(String[] args)
    {
        Fasta generatedFasta;
        InputEvaluator input = new InputEvaluator(args); //evaluate the command line input

        if (input.isValid && input.collector.programInterface.equals("cli"))
        {         
            // run the program in CLI mode
            FastaProcessor processor = new FastaProcessor(input); // process Fasta entries (read or generate)
            System.exit(0);
        }

        else if (input.isValid && input.collector.programInterface.equals("gui")) launch(args); // run in GUI mode
        
        else System.exit(0); // have to manually exit here since JavaFX wants to keep the program running
        
    } 

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        this.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("startPane.fxml"));
        primaryStage.setTitle("FastaGen");
        Scene scene = new Scene(root, 600, 440);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
