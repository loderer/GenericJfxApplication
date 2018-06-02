package sample_app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample_app.events.Controller;
import sample_app.events.Observable;

import java.io.IOException;

public class SyncSceneCreation implements Runnable {

    private final Stage stage;
    private final String fxmlFile;
    private final double width;
    private final double height;
    private final Observable observable;

    private Scene newScene = null;
    private Scene oldScene = null;
    private FXMLLoader fxmlLoader = null;
    private final Object sceneMonitor = new Object();

    public SyncSceneCreation(Stage stage, String fxmlFile, double width, double height, Observable observable) {
        this.stage = stage;
        this.fxmlFile = fxmlFile;
        this.width = width;
        this.height = height;
        this.observable = observable;
    }

    @Override
    public void run() {
        try {
            FXMLLoader tmpLoader = new FXMLLoader();
            Parent root = tmpLoader.load(Main.class.getResource(fxmlFile)
                    .openStream());

            Controller controller =
                    (Controller) tmpLoader.getController();
            controller.setObservable(observable);

            Scene tmpNewScene = new Scene(root, width, height);
            Scene tmpOldScene = stage.getScene();

            stage.setScene(tmpNewScene);
            stage.show();

            synchronized (sceneMonitor) {
                this.newScene = tmpNewScene;
                this.oldScene = tmpOldScene;
                this.fxmlLoader = tmpLoader;
                sceneMonitor.notify();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Scene getNewScene() {
        return newScene;
    }

    public Scene getOldScene() {
        return oldScene;
    }

    public FXMLLoader getFxmlLoader() {
        return fxmlLoader;
    }

    public Object getSceneMonitor() {
        return sceneMonitor;
    }
}