package generic_jfx_application;

import javafx.stage.Modality;
import generic_jfx_application.handle.SceneHandle;
import generic_jfx_application.handle.StageHandle;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GenericJfxApplicationTest {

    private GenericJfxApplication jfxApp;

    @BeforeMethod
    public void setUp() {
        jfxApp = new GenericJfxApplication();
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
