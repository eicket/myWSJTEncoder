// Erik Icket, ON4PB - 2022

package test.displayFrequencyDeviation;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Start extends Application
{

    @Override
    public void start(Stage stage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Frequency deviation by ON4PB");
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
