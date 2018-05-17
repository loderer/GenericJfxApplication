package sample_app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample_app.events.AbstractController;
import sample_app.events.Observable;
import sample_app.jfxthread.JFxThread;

import java.io.IOException;

public class Main extends Application {

    /**
     * Title of the application.
     */
    private static final String TITLE = "Sample";

    /**
     * Primary stage of the application.
     */
    private static Stage primaryStage;

    /**
     * The target of the MATLAB-observer.
     */
    private static Observable observable;

    /**
     * This object allows changing the gui from MATLAB.
     */
    private static JFxThread jfxThread;

    /**
     * This flag indicates the initialization-status of the application.
     * The attributes are initialized if the flag is set.
     */
    private static boolean initialisationCompleted = false;
    private static final Object initialisationCompletedMonitor = new Object();

    public static Observable getObservable() {
        return observable;
    }

    public static JFxThread getJfxThread() {
        return jfxThread;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        // Save primary stage to enable restarting the application.
        Main.primaryStage = primaryStage;
        // Do not implicitly shutdown the JavaFX runtime when the last window
        // is closed. This enables restarting the application.
        Platform.setImplicitExit(false);
        initAndShowApplication();
    }

    /**
     * Initialize the contents of the user interface.
     * @throws java.io.IOException  If the specified fxml-file is not
     * available.
     */
    private static void initAndShowApplication() throws IOException {
        observable = new Observable();

        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(Main.class.getResource("sample/sample.fxml")
                .openStream());

        AbstractController controller =
                (AbstractController) loader.getController();
        controller.setObservable(observable);

        primaryStage.setTitle(TITLE);

        Scene scene = new Scene(root, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();

        jfxThread = new JFxThread(scene);

        synchronized (initialisationCompletedMonitor) {
            initialisationCompleted = true;
            initialisationCompletedMonitor.notify();
        }
    }

    /**
     * Starts the ui. A call returns after application termination.
     * @param args
     */
    private static void startGui(final String[] args) {
        try {
            launch(args);
        } catch(IllegalStateException e) {
            // The ui is still running. Reset controls and show primary stage.
            restartGui();
        }
    }
    /**

     * Resets and shows a running JavaFX-application.
     */
    private static void restartGui() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    initAndShowApplication();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     * Starts the ui in its own thread. A call returns if all public
     * properties are initialized.
     * @param args
     */
    public static void startGuiThread(final String[] args) throws InterruptedException {
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
    }
}
