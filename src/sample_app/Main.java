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
    private static String primaryStageTitle = "";
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

        primaryStage.setTitle(primaryStageTitle);

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

    public static StageHandle newStage(final String title) {
        SyncStageCreation syncStageCreation = new SyncStageCreation(title);
        Platform.runLater(syncStageCreation);

        synchronized (syncStageCreation.getStageMonitor()) {
            try {
                while(syncStageCreation.getStage() == null) {
                    syncStageCreation.getStageMonitor().wait();
                }
            } catch (InterruptedException e) {
                // do nothing;
            }
        }

        Stage stage = syncStageCreation.getStage();
        final Observable observable = new Observable();

        if(stage != null) {
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    observable.notifyListeners("root", "CLOSE");
                }
            });
        }

        return new StageHandle(observable, stage);
    }


    public static SceneHandle showScene(final Stage stage, final String fxmlFile) {
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

                    if(observables.containsKey(stage.getScene())) {
                        observables.get(stage.getScene()).notifyListeners("root", "CLOSE");
                        observables.remove(stage.getScene());
                    }

                    stage.setScene(scene);
                    stage.show();

                    observables.put(scene, observable);
                    jfxThread.setScene(scene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return new SceneHandle(observable, jfxThread);
    }

    /**
     * Starts the ui. A call returns after application termination.
     * @param primaryStageTitle Initial title of the primary stage.
     */
    private static void startGui(final String primaryStageTitle) {
        try {
            Main.primaryStageTitle = primaryStageTitle;
            launch(new String[0]);
        } catch(IllegalStateException e) {
            // The ui is still running.
        }
    }

    /**
     * Starts the ui in its own thread. A call returns if all public
     * properties are initialized.
     * @param primaryStageTitle Initial title of the primary stage.
     * @return Observable to listen for events on primaryStage level.
     */
    public static StageHandle startGuiThread(final String primaryStageTitle)
            throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startGui(primaryStageTitle);
            }
        }).start();

        synchronized (initialisationCompletedMonitor) {
            while(!initialisationCompleted) {
                initialisationCompletedMonitor.wait();
            }
        }
        return new StageHandle(primaryStageObservable, primaryStage);
    }
}
