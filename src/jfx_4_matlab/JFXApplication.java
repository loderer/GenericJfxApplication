package jfx_4_matlab;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import jfx_4_matlab.event_transfer.Controller;
import jfx_4_matlab.event_transfer.Observable;
import jfx_4_matlab.handle.SceneHandle;
import jfx_4_matlab.handle.StageHandle;
import jfx_4_matlab.jfxthread.JFXThread;

import java.io.File;
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
    private static Map<Scene, Observable> scene2Observable;
    /**
     * This flag indicates the initialization-status of the application.
     * The attributes are initialized if the flag is set.
     */
    private static boolean initializationCompleted = false;
    private static final Object initializationCompletedMonitor = new Object();

    @Override
    public void start(Stage primaryStage) throws Exception{
        // Save primary stage to enable restarting the application.
        JFXApplication.primaryStage = primaryStage;
        // Do not implicitly shutdown the JavaFX runtime when the last window
        // is closed. This enables restarting the application.
        Platform.setImplicitExit(false);

        JFXApplication.primaryStageObservable = new Observable();

        JFXApplication.scene2Observable = new HashMap<>();

        primaryStage.setTitle(JFXApplication.primaryStageTitle);

        setOnCloseRequest(JFXApplication.primaryStageObservable, primaryStage);

        synchronized (JFXApplication.initializationCompletedMonitor) {
            JFXApplication.initializationCompleted = true;
            JFXApplication.initializationCompletedMonitor.notifyAll();
        }
    }

    /**
     * Registers a callback which allows observing the shutdown of a stage.
     * @param observable    Observable to be notified.
     * @param stage         The stage to be observed.
     */
    private void setOnCloseRequest(final Observable observable,
                                         final Stage stage) {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                observable.notifyObserver("root", "CLOSE");
                event.consume();
            }
        });
    }

    /**
     * Creates a new stage. The parameters modality and owner control the
     * modality of the stage.
     * @param title The title of the stage to be created.
     * @param modality The modality of the stage.
     * @param owner   The owner of this stage or null.
     * @return  The appropriate stage handle.
     */
    public StageHandle createStage(final String title,
                                    final Modality modality,
                                    final Window owner) throws Exception {
        if(modality == Modality.WINDOW_MODAL && owner == null) {
            throw new IllegalArgumentException("If the stage should be set " +
                    "window-modal an owner must be given.");
        }

        boolean initializationCompleted = false;
        synchronized (initializationCompletedMonitor) {
            initializationCompleted = JFXApplication.initializationCompleted;
        }

        StageHandle stageHandle;

        if(initializationCompleted) {
            stageHandle = createAnotherStage(title, modality, owner);
        } else {
            if(!modality.equals(Modality.NONE)) {
                throw new IllegalArgumentException("The first stage has to be non-modal.");
            }
            stageHandle = createPrimaryStage(title);
        }
        return stageHandle;
    }

    /**
     * Creates each stage except the primary stage. The new stage is modal if
     * an owner is given.
     * @param title The title of the stage.
     * @param modality The modality of the stage.
     * @param owner   The owner of this stage or null.
     * @return  The appropriate stage handle.
     * @throws Exception
     */
    private StageHandle createAnotherStage(String title,
                                           final Modality modality,
                                           Window owner) throws Exception {
        StageHandle stageHandle;
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

        switch (modality) {
            case NONE:
                stage.initModality(Modality.NONE);
                break;
            case WINDOW_MODAL:
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
                break;
            case APPLICATION_MODAL:
                stage.initModality(Modality.APPLICATION_MODAL);
                break;
        }

        stageHandle =  new StageHandle(observable, stage);
        return stageHandle;
    }

    /**
     * Starts the ui in its own thread. Also creates the primary stage. A call
     * returns if all properties are initialized.
     * @param stageTitle Title of the primary stage.
     * @return Observable to listen for an event on the primaryStage.
     */
    private StageHandle createPrimaryStage(final String stageTitle)
            throws Exception {
        SyncApplicationInitialization syncApplicationInitialization = new SyncApplicationInitialization(stageTitle);
        new Thread(syncApplicationInitialization).start();

        synchronized (initializationCompletedMonitor) {
            while(!initializationCompleted) {
                initializationCompletedMonitor.wait();
            }
        }

        if(syncApplicationInitialization.getException() != null) {
            throw syncApplicationInitialization.getException();
        }

        setOnCloseRequest(primaryStageObservable, primaryStage);
        return new StageHandle(primaryStageObservable, primaryStage);
    }

    /**
     * Creates a new scene.
     * @param stage The stage of the scene to be created.
     * @param fxmlFile  The appropriate fxml file.
     * @return  The appropriate scene handle.
     */
    public SceneHandle showScene(final Stage stage, final String fxmlFile)
            throws Exception {
        final Observable observable = new Observable();

        final JFXThread jfxThread = new JFXThread(fxmlFile);

        final SyncSceneCreation syncSceneCreation =
                new SyncSceneCreation(stage, fxmlFile, observable);

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

        if(scene2Observable.containsKey(syncSceneCreation.getOldScene())) {
            scene2Observable.get(syncSceneCreation.getOldScene())
                    .notifyObserver("root", "CLOSE");
            scene2Observable.remove(syncSceneCreation.getOldScene());
        }

        scene2Observable.put(syncSceneCreation.getNewScene(), observable);
        jfxThread.setFxmlLoader(syncSceneCreation.getFxmlLoader());

        return new SceneHandle(observable, jfxThread);
    }

    // nested classes =========================================================

    // SyncSceneCreation ------------------------------------------------------
    /**
     * Allows creating a new scene on the JavaFX Application Thread synchronous.
     */
    public class SyncSceneCreation extends Thread{

        /**
         * The stage of the scene.
         */
        private final Stage stage;
        /**
         * The appropriate fxml file.
         */
        private final String fxmlFile;
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

        public SyncSceneCreation(Stage stage, String fxmlFile,
                                 Observable observable) {
            this.stage = stage;
            this.fxmlFile = fxmlFile;
            this.observable = observable;
        }

        @Override
        public void run() {
            try {
                FXMLLoader tmpLoader = new FXMLLoader();

                Parent root;
                File file = new File(fxmlFile);
                if (file.canRead()) {
                    URL url = file.toURI().toURL();
                    tmpLoader.setLocation(url);
                    root = tmpLoader.load();
                } else {
                    throw new Exception(String.format("No fxml file \"%s\" does exist.", fxmlFile));
                }

                Controller controller =
                        (Controller) tmpLoader.getController();
                if(controller != null) {
                    controller.setObservable(observable);
                }

                Scene tmpNewScene = new Scene(root);
                Scene tmpOldScene = stage.getScene();

                stage.setScene(tmpNewScene);
                stage.show();

                synchronized (sceneMonitor) {
                    this.newScene = tmpNewScene;
                    this.oldScene = tmpOldScene;
                    this.fxmlLoader = tmpLoader;
                    sceneMonitor.notifyAll();
                }
            } catch (Exception e) {
                this.exception = e;
                synchronized (sceneMonitor) {
                    sceneMonitor.notifyAll();
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
    public class SyncStageCreation implements Runnable {

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
                    stageMonitor.notifyAll();
                }
            } catch (Exception e) {
                synchronized (stageMonitor) {
                    this.exception = e;
                    stageMonitor.notifyAll();
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

    // SyncApplicationInitialization ------------------------------------------
    /**
     * Allows launching the application in its own thread.
     */
    public class SyncApplicationInitialization implements Runnable {

        /**
         * The title of the primary stage.
         */
        private final String title;
        /**
         * Any exception occurred while execution is in progress.
         */
        private Exception exception;

        public SyncApplicationInitialization(final String title) {
            this.title = title;
        }

        @Override
        public void run() {
            try {
                JFXApplication.primaryStageTitle = title;
                launch(JFXApplication.class);
            } catch(IllegalStateException e) {
                // The ui is still running.
                synchronized (JFXApplication.initializationCompletedMonitor) {
                    JFXApplication.initializationCompleted = true;
                    JFXApplication.initializationCompletedMonitor.notifyAll();
                }
            } catch(Exception e) {
                exception = e;
                synchronized (JFXApplication.initializationCompletedMonitor) {
                    JFXApplication.initializationCompleted = true;
                    JFXApplication.initializationCompletedMonitor.notifyAll();
                }
            }
        }

        public Exception getException() {
            return exception;
        }
    }
}
