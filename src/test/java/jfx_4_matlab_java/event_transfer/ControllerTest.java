package jfx_4_matlab_java.event_transfer;

import javafx.event.EventType;
import javafx.event.Event;
import javafx.scene.control.Control;
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.BDDMockito.given;

@Test
public class ControllerTest {

    private Observable observable;
    private Controller controller;

    @BeforeTest
    public void setUp() {
        this.observable = Mockito.mock(Observable.class);
        this.controller = new Controller();
        this.controller.setObservable(this.observable);
    }

    @Test
    private void testHandleEvent() {
        EventType eventType = Mockito.mock(EventType.class);
        given(eventType.getName()).willReturn("ACTION");
        Event event = Mockito.mock(Event.class);
        Mockito.when(event.getEventType()).thenReturn(eventType);
        Control control = Mockito.mock(Control.class);
        Mockito.when(control.getId()).thenReturn("btn");
        Mockito.when(event.getSource()).thenReturn(control);
        // mock to generate a Event like this new Event(this, "btn", "ACTION")

        controller.handleEvent(event);

        Mockito.verify(observable, Mockito.times(1)).notifyListeners("btn", "ACTION");
    }

}
