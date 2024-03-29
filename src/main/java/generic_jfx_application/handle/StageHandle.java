package generic_jfx_application.handle;

import javafx.stage.Stage;
import generic_jfx_application.event_transfer.Observable;

/**
 * Contains all stage data which is required at the MATLAB application.
 */
public class StageHandle {

    private final Observable observable;

    private final Stage stage;


    public StageHandle(final Observable observable, final Stage stage) {
        this.observable = observable;
        this.stage = stage;
    }

    public Observable getObservable() {
        return observable;
    }

    public Stage getStage() {
        return stage;
    }
}
