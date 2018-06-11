package jfx_4_matlab;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfx_4_matlab.event_transfer.Controller;
import jfx_4_matlab.event_transfer.Observable;
import jfx_4_matlab.handle.SceneHandle;
import jfx_4_matlab.handle.StageHandle;
import jfx_4_matlab.jfxthread.JFXThread;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JFXApplication extends Application {

    /**
     * Title of the application.
     */
    private static String primaryStageTitle = "";

    /**
     * Primary stage of the application.
     */
    private static Stage primaryStage;
    /**
     * Observable of the primary stage.
     */
    private static Observable primaryStageObservable;

    /**
     * Contains each running scene and its appropriate observable.
     */
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
        JFXApplication.primaryStage = primaryStage;
        // Do not implicitly shutdown the JavaFX runtime when the last window
        // is closed. This enables restarting the application.
        Platform.setImplicitExit(false);

        primaryStageObservable = new Observable();

        observables = new HashMap<Scene, Observable>();

        primaryStage.setTitle(primaryStageTitle);

        setOnCloseRequest(primaryStageObservable, primaryStage);

        synchronized (initialisationCompletedMonitor) {
            initialisationCompleted = true;
            initialisationCompletedMonitor.notify();
        }
    }

    /**
     * Registers a callback which allows observing the shutdown of a stage.
     * @param observable    Observable to be notified.
     * @param stage         The stage to be observed.
     */
    public static void setOnCloseRequest(final Observable observable,
                                         final Stage stage) {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                observable.notifyListeners("root", "CLOSE");
                event.consume();
            }
        });
    }

    /**
     * Creates a new non-modal stage.
     * @param title The title of the stage.
     * @return  The appropriate stage handle.
     */
    public static StageHandle newStage(final String title) throws Exception {
        return newStage(title, null);
    }

    /**
     * Creates a new modal stage. The given stage is the parent stage.
     * @param title The title of the stage to be created.
     * @param parentStage   The parent stage.
     * @return  The appropriate stage handle.
     */
    public static StageHandle newStage(final String title,
                                       final Stage parentStage) throws Exception {
        SyncStageCreation syncStageCreation = new SyncStageCreation(title);
        Platform.runLater(syncStageCreation);

        synchronized (syncStageCreation.getStageMonitor()) {
            try {
                while(syncStageCreation.getStage() == null
                        && syncStageCreation.getException() == null) {
                    syncStageCreation.getStageMonitor().wait();
                }
            } catch (InterruptedException e) {
                // do nothing;
            }
        }

        if(syncStageCreation.getException() != null) {
            throw syncStageCreation.getException();
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

    /**
     * Creates a new scene.
     * @param stage The stage of the scene to be created.
     * @param fxmlFile  The appropriate fxml file.
     * @param width The width of the scene.
     * @param height    The height of the scene.
     * @return  The appropriate scene handle.
     */
    public static SceneHandle showScene(final Stage stage, final String fxmlFile,
            final double width, final double height )
            throws Exception {
        final Observable observable = new Observable();

        final JFXThread jfxThread = new JFXThread(fxmlFile);

        final SyncSceneCreation syncSceneCreation =
                new SyncSceneCreation(stage, fxmlFile, width, height, observable);

        Platform.runLater(syncSceneCreation);

        synchronized (syncSceneCreation.getSceneMonitor()) {
            try {
                while(syncSceneCreation.getNewScene() == null
                        && syncSceneCreation.getException() == null) {
                    syncSceneCreation.getSceneMonitor().wait();
                }
            } catch (InterruptedException e) {
                // do nothing;
            }
        }

        if(syncSceneCreation.getException() != null) {
            throw syncSceneCreation.getException();
        }

        if(observables.containsKey(syncSceneCreation.getOldScene())) {
            observables.get(syncSceneCreation.getOldScene())
                    .notifyListeners("root", "CLOSE");
            observables.remove(syncSceneCreation.getOldScene());
        }

        observables.put(syncSceneCreation.getNewScene(), observable);
        jfxThread.setFxmlLoader(syncSceneCreation.getFxmlLoader());

        return new SceneHandle(observable, jfxThread);
    }

    /**
     * Starts the ui. A call returns after application termination.
     * @param primaryStageTitle Title of the primary stage.
     */
    private static void startGui(final String primaryStageTitle) {
        try {
            JFXApplication.primaryStageTitle = primaryStageTitle;
            launch(new String[0]);
        } catch(IllegalStateException e) {
            // The ui is still running.
        }
    }

    /**
     * Starts the ui in its own thread. A call returns if all
     * properties are initialized.
     * @param primaryStageTitle Title of the primary stage.
     * @return Observable to listen for an event_transfer on primaryStage.
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

        setOnCloseRequest(primaryStageObservable, primaryStage);
        return new StageHandle(primaryStageObservable, primaryStage);
    }

    // nested classes =========================================================

    // SyncSceneCreation ------------------------------------------------------
    /**
     * Allows creating a new scene on the JavaFX Application Thread synchronous.
     */
    public static class SyncSceneCreation extends Thread{

        /**
         * The stage of the scene.
         */
        private final Stage stage;
        /**
         * The appropriate fxml file.
         */
        private final String fxmlFile;
        /**
         * The width of the scene.
         */
        private final double width;
        /**
         * The height of the scene.
         */
        private final double height;
        /**
         * The observable which propagates the events of the scene.
         */
        private final Observable observable;
        /**
         * The scene which was assigned to the stage before.
         */
        private Scene newScene = null;
        /**
         * The new scene of the stage.
         */
        private Scene oldScene = null;
        /**
         * The fxml loader which is used to load the scene.
         */
        private FXMLLoader fxmlLoader = null;
        /**
         * This monitor indicates whether the scene is completely initialized or not.
         */
        private final Object sceneMonitor = new Object();
        /**
         * Any exception occurred while execution is in progress.
         */
        private Exception exception;

        public SyncSceneCreation(Stage stage, String fxmlFile, double width,
                                 double height, Observable observable) {
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
                URL fxmlUrl = JFXApplication.class.getResource(fxmlFile);
                if(fxmlUrl == null) {
                    throw new Exception(String.format("No fxml file \"%s\" does exist.", fxmlFile));
                }
                Parent root = tmpLoader.load(fxmlUrl.openStream());

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
            } catch (Exception e) {
                this.exception = e;
                synchronized (sceneMonitor) {
                    sceneMonitor.notify();
                }
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

        public Exception getException() {
            return exception;
        }
    }

    // SyncStageCreation ------------------------------------------------------
    /**
     * Allows creating a new stage on the JavaFX Application Thread synchronous.
     */
    public static class SyncStageCreation implements Runnable {

        /**
         * The title of the stage.
         */
        private final String title;
        /**
         * The stage.
         */
        private Stage stage = null;
        /**
         * This monitor indicates whether the stage is completely initialized or not.
         */
        private final Object stageMonitor = new Object();
        /**
         * Any exception occurred while execution is in progress.
         */
        private Exception exception;

        public SyncStageCreation(final String title) {
            this.title = title;
        }

        @Override
        public void run() {
            try {
                Stage tmpStage = new Stage();
                tmpStage.setTitle(title);

                synchronized (stageMonitor) {
                    this.stage = tmpStage;
                    stageMonitor.notify();
                }
            } catch (Exception e) {
                synchronized (stageMonitor) {
                    this.exception = e;
                    stageMonitor.notify();
                }
            }
        }

        public Stage getStage() {
            return stage;
        }

        public Object getStageMonitor() {
            return stageMonitor;
        }

        public Exception getException() {
            return exception;
        }
    }
}
