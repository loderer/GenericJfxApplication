package sample_app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample_app.events.Controller;
import sample_app.events.Observable;
import sample_app.jfxthread.JFxThread;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    /**
     * Title of the application.
     */
    private static final String TITLE = "Sample";
    private static final String PRIMARY_STAGE_OBSERVABLE = "primaryStage";

    /**
     * Primary stage of the application.
     */
    private static Stage primaryStage;

    private static Observable primaryStageObservable;

    //TODO documentation
    private static Map<Scene, Observable> observables;

    /**
     * This flag indicates the initialization-status of the application.
     * The attributes are initialized if the flag is set.
     */
    private static boolean initialisationCompleted = false;
    private static final Object initialisationCompletedMonitor = new Object();

    @Override
    public void start(Stage primaryStage) throws Exception{
        // Save primary stage to enable restarting the application.
        Main.primaryStage = primaryStage;
        // Do not implicitly shutdown the JavaFX runtime when the last window
        // is closed. This enables restarting the application.
        Platform.setImplicitExit(false);

        primaryStageObservable = new Observable();

        observables = new HashMap<Scene, Observable>();

        primaryStage.setTitle(TITLE);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                primaryStageObservable.notifyListeners("root", "CLOSE");
            }
        });

        synchronized (initialisationCompletedMonitor) {
            initialisationCompleted = true;
            initialisationCompletedMonitor.notify();
        }
    }


    public static UiHandle showScene(final String fxmlFile) {
        final Observable observable = new Observable();

        final JFxThread jfxThread = new JFxThread(fxmlFile);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    Parent root = loader.load(Main.class.getResource(fxmlFile)
                            .openStream());

                    Controller controller =
                            (Controller) loader.getController();
                    controller.setObservable(observable);

                    Scene scene = new Scene(root, 300, 275);

                    if(observables.containsKey(primaryStage.getScene())) {
                        observables.get(primaryStage.getScene()).notifyListeners("root", "CLOSE");
                        observables.remove(primaryStage.getScene());
                    }

                    primaryStage.setScene(scene);
                    primaryStage.show();

                    observables.put(scene, observable);
                    jfxThread.setScene(scene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return new UiHandle(observable, jfxThread);
    }

    /**
     * Starts the ui. A call returns after application termination.
     * @param args
     */
    private static void startGui(final String[] args) {
        try {
            launch(args);
        } catch(IllegalStateException e) {
            // The ui is still running.
        }
    }

    /**
     * Starts the ui in its own thread. A call returns if all public
     * properties are initialized.
     * @param args
     * @return Observable to listen for events on primaryStage level.
     */
    public static Observable startGuiThread(final String[] args)
            throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startGui(args);
            }
        }).start();

        synchronized (initialisationCompletedMonitor) {
            while(!initialisationCompleted) {
                initialisationCompletedMonitor.wait();
            }
        }
        return primaryStageObservable;
    }
}
