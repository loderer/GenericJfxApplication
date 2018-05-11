package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Sample");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        }

    public static void main(final String[] args) {
        launch(args);
    }

    /**
     * Startet die GUI in einem eigenen Thread.
     * @param args
     */
    public static void startGui(final String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                main(args);
            }
        }).start();
    }
}
