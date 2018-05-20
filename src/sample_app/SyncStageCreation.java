package sample_app;

import javafx.stage.Stage;

public class SyncStageCreation implements Runnable {

    private final String title;

    private Stage stage = null;
    private final Object stageMonitor = new Object();

    public SyncStageCreation(final String title) {
        this.title = title;
    }

    @Override
    public void run() {
        Stage tmpStage = new Stage();
        tmpStage.setTitle(title);

        synchronized (stageMonitor) {
            this.stage = tmpStage;
            stageMonitor.notify();
        }
    }

    public Stage getStage() {
        return stage;
    }

    public Object getStageMonitor() {
        return stageMonitor;
    }
}