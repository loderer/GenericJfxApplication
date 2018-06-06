package sample_app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Modality;
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
    private static Stage primaryStageParent;

    public static void setOnCloseRequest(final Observable observable, final Stage stage) {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                observable.notifyListeners("root", "CLOSE");
                event.consume();
            }
        });
    }

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

        if(primaryStageParent != null) {
            primaryStage.initOwner(primaryStageParent);
            primaryStage.initModality(Modality.WINDOW_MODAL);
        }

        synchronized (initialisationCompletedMonitor) {
            initialisationCompleted = true;
            initialisationCompletedMonitor.notify();
        }
    }

    public static StageHandle newStage(final String title) {
        return newStage(title, null);
    }

    public static StageHandle newStage(final String title, final Stage parentStage) {
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
            setOnCloseRequest(observable, stage);
        }

        if(parentStage != null) {
            stage.initOwner(parentStage);
            stage.initModality(Modality.WINDOW_MODAL);
        }

        return new StageHandle(observable, stage);
    }


    public static SceneHandle showScene(final Stage stage, final String fxmlFile,
                                        final double width, final double height ) {
        final Observable observable = new Observable();

        final JFxThread jfxThread = new JFxThread(fxmlFile, stage);

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

    public static StageHandle startGuiThread(final String primaryStageTitle)
            throws InterruptedException {
        return startGuiThread(primaryStageTitle, null);
    }

    /**
     * Starts the ui in its own thread. A call returns if all public
     * properties are initialized.
     * @param primaryStageTitle Initial title of the primary stage.
     * @return Observable to listen for events on primaryStage level.
     */
    public static StageHandle startGuiThread(final String primaryStageTitle, final Stage parentStage)
            throws InterruptedException {
        Main.primaryStageParent = parentStage;
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

        setOnCloseRequest(primaryStageObservable, primaryStage);
        return new StageHandle(primaryStageObservable, primaryStage);
    }
}
