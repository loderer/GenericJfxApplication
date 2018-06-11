package jfx_4_matlab;

import jfx_4_matlab.handle.StageHandle;

/**
 * This is just a main class which enables simple debugging in java IDEs.
 */
public class Debug {

    public static void main(String[] args) throws Exception {
        StageHandle stageHandle = JFXApplication.startGuiThread("Sample application");
        JFXApplication.showScene(stageHandle.getStage(), "sample_app/overview.fxml", 500, 500);
    }

}
