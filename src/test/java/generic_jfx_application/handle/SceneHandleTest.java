package generic_jfx_application.handle;

import generic_jfx_application.event_transfer.Observable;
import generic_jfx_application.jfx_thread.JFXThread;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SceneHandleTest {

    private static final Observable observable = Mockito.mock(Observable.class);

    private static final JFXThread jfxThread = Mockito.mock(JFXThread.class);

    @Test
    public void initSceneHandleTest() {
        SceneHandle sut = new SceneHandle(observable, jfxThread);

        assertEquals(sut.getObservable(), observable);
        assertEquals(sut.getJfxThread(), jfxThread);
    }
}
