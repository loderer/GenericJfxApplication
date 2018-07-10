package jfx_4_matlab_java.event_transfer;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ObservableTest {

    private static final String FX_ID = "fxId";
    private static final String ACTION = "action";

    private Observable observable;
    private EventListener eventListener;

    @BeforeMethod
    public void setUp() {
        this.observable = new Observable();
        this.eventListener = Mockito.mock(EventListener.class);
        this.observable.addEventListener(eventListener);
    }

    @Test
    public void notifyListenersTest() {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        this.observable.notifyListeners(FX_ID, ACTION);

        Mockito.verify(eventListener, Mockito.times(1))
                .event(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().getSource(), this.observable);
        assertEquals(eventCaptor.getValue().fxId, FX_ID);
        assertEquals(eventCaptor.getValue().action, ACTION);
    }

    @Test
    public void removeEventListenerTest() {
        this.observable.removeEventListener(this.eventListener);
        this.observable.notifyListeners(FX_ID, ACTION);

        Mockito.verifyZeroInteractions(eventListener);
    }
}
