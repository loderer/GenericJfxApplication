package sample_app;

public class Debug {

    public static void main(String[] args) throws InterruptedException {
        StageHandle stageHandle = Main.startGuiThread(new String[0]);
        Main.showScene(stageHandle.getStage(), "sample/overview.fxml");
    }

}
