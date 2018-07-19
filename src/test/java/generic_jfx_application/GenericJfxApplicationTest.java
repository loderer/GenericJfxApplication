package generic_jfx_application;

import generic_jfx_application.event_transfer.Event;
import generic_jfx_application.event_transfer.EventListener;
import generic_jfx_application.handle.SceneHandle;
import generic_jfx_application.handle.StageHandle;
import javafx.stage.Modality;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class GenericJfxApplicationTest {

    private GenericJfxApplication jfxApp;

    @BeforeMethod
    public void setUp() {
        jfxApp = new GenericJfxApplication();
    }

    @Test
    public void createStageTest() throws Exception {
        StageHandle stageHandle = jfxApp.createStage("title", Modality.NONE, null);

        assertEquals(stageHandle.getStage().getModality(), Modality.NONE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void createWindowModalStageWithoutOwner() throws Exception {
        jfxApp.createStage("title", Modality.WINDOW_MODAL, null);
    }

    @Test
    public void createApplicationModalStage() throws Exception {
        jfxApp.createStage("first stage", Modality.NONE, null);
        StageHandle secondStageHandle = jfxApp.createStage("title", Modality.APPLICATION_MODAL, null);

        assertEquals(secondStageHandle.getStage().getModality(), Modality.APPLICATION_MODAL);
    }

    @Test
    public void createWindowModalStage() throws Exception {
        StageHandle firstStageHandle = jfxApp.createStage("first stage", Modality.NONE, null);
        StageHandle secondStageHandle = jfxApp.createStage("title", Modality.WINDOW_MODAL, firstStageHandle.getStage());

        assertEquals(secondStageHandle.getStage().getModality(), Modality.WINDOW_MODAL);
        assertEquals(secondStageHandle.getStage().getOwner(), firstStageHandle.getStage());
    }

    @Test
    public void showSceneTest() throws Exception {
        StageHandle stageHandle = jfxApp.createStage("title", Modality.NONE, null);
        String sampleFxmlPath = System.getProperty("user.dir") + "\\src\\test\\resources\\sample.fxml";
        SceneHandle firstSceneHandle = jfxApp.showScene(stageHandle.getStage(), sampleFxmlPath);
        EventListener eventListener = Mockito.mock(EventListener.class);
        firstSceneHandle.getObservable().addEventListener(eventListener);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        jfxApp.showScene(stageHandle.getStage(), sampleFxmlPath);

        Mockito.verify(eventListener, Mockito.times(1)).event(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().fxId, "root");
        assertEquals(eventCaptor.getValue().action, "CLOSE");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void showUnknownSceneTest() throws Exception {
        StageHandle stageHandle = jfxApp.createStage("title", Modality.NONE, null);
        String sampleFxmlPath = System.getProperty("user.dir") + "\\src\\test\\resources\\unknown.fxml";
        SceneHandle firstSceneHandle = jfxApp.showScene(stageHandle.getStage(), sampleFxmlPath);
    }
}
