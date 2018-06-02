package sample_app;

public class Debug {

    public static void main(String[] args) throws InterruptedException {
        StageHandle stageHandle = Main.startGuiThread("Sample application");
        Main.showScene(stageHandle.getStage(), "sample/overview.fxml", 500, 500);
    }

}
