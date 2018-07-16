package generic_jfx_application.handle;

import javafx.stage.Stage;
import generic_jfx_application.event_transfer.Observable;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class StageHandleTest {

    private static final Observable observable = Mockito.mock(Observable.class);

    private static final Stage stage = Mockito.mock(Stage.class);

    @Test
    public void initStageHandleTest() {
        StageHandle sut = new StageHandle(observable, stage);

        assertEquals(sut.getObservable(), observable);
        assertEquals(sut.getStage(), stage);
    }
}
