package sample_app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample_app.events.Observable;
import sample_app.jfxthread.JFxThread;

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


    public static SceneHandle showScene(final Stage stage, final String fxmlFile,
                                        final double width, final double height ) {
        final Observable observable = new Observable();

        final JFxThread jfxThread = new JFxThread(fxmlFile);

        final SyncSceneCreation syncSceneCreation = new SyncSceneCreation(stage, fxmlFile, width, height, observable);

        Platform.runLater(syncSceneCreation);

        synchronized (syncSceneCreation.getSceneMonitor()) {
            try {
                while(syncSceneCreation.getNewScene() == null) {
                    syncSceneCreation.getSceneMonitor().wait();
                }
            } catch (InterruptedException e) {
                // do nothing;
            }
        }

        if(observables.containsKey(syncSceneCreation.getOldScene())) {
            observables.get(syncSceneCreation.getOldScene()).notifyListeners("root", "CLOSE");
            observables.remove(syncSceneCreation.getOldScene());
        }

        observables.put(syncSceneCreation.getNewScene(), observable);
        jfxThread.setScene(syncSceneCreation.getNewScene());
        jfxThread.setFxmlLoader(syncSceneCreation.getFxmlLoader());

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
