package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String TITLE = "Sample";

    private static Observable observable;

    public static Observable getObservable() {
        return observable;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResource("sample.fxml").openStream());

        AbstractController controller = (AbstractController) loader.getController();
        controller.setObservable(observable);

        primaryStage.setTitle(TITLE);

        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        }

    public static void main(final String[] args) {
        observable = new Observable();
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
