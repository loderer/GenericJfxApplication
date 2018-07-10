package jfx_4_matlab_java;

import javafx.stage.Modality;
import jfx_4_matlab_java.handle.SceneHandle;
import jfx_4_matlab_java.handle.StageHandle;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JFXApplicationTest {

    private JFXApplication jfxApp;

    @BeforeMethod
    public void setUp() {
        jfxApp = new JFXApplication();
    }

    @Test
    public void createStageTest() throws Exception {
        jfxApp.createStage("title", Modality.NONE, null);
    }

    @Test
    public void showSceneTest() throws Exception {
        StageHandle stageHandle = jfxApp.createStage("title", Modality.NONE, null);
        String sampleFxmlPath = System.getProperty("user.dir") + "\\src\\test\\resources\\sample.fxml";
        SceneHandle sceneHandle = jfxApp.showScene(stageHandle.getStage(), sampleFxmlPath);
    }
}
