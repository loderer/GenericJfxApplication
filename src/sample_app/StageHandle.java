package sample_app;

import javafx.stage.Stage;
import sample_app.events.Observable;

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
