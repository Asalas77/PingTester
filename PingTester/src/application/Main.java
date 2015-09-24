
package application;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

public class Main
extends Application {
    public void start(Stage primaryStage) {
        try {
            SplitPane root = (SplitPane)FXMLLoader.load((URL)this.getClass().getResource("GUI.fxml"));
            Scene scene = new Scene((Parent)root, 1200, 400.0);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main.launch((String[])args);
    }
}

