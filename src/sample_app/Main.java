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
        this.primaryStage = primaryStage;
        startApplication(primaryStage);
    }

    private static void startApplication(final Stage primaryStage) throws java.io.IOException {
        observable = new Observable();

        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(Main.class.getResource("sample/sample.fxml").openStream());

        AbstractController controller = (AbstractController) loader.getController();
        controller.setObservable(observable);

        primaryStage.setTitle(TITLE);

        Scene scene = new Scene(root, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();

        Platform.setImplicitExit(false);
//        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                primaryStage.hide();
//                event.consume();
//            }
//        });

        jfxThread = new JFxThread(scene);

        synchronized (initialisationCompletedMonitor) {
            initialisationCompleted = true;
            initialisationCompletedMonitor.notify();
        }
    }

    public static void main(final String[] args) {
        try {
            launch(args);
        } catch(IllegalStateException e) {
            System.err.println("Don't launch twice.");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        startApplication(primaryStage);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Starts the ui in its own thread.
     * @param args
     */
    public static void startGui(final String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                main(args);
            }
        }).start();

        synchronized (initialisationCompletedMonitor) {
            try {
                while(!initialisationCompleted) {
                    initialisationCompletedMonitor.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
