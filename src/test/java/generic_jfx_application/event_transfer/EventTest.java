package generic_jfx_application.event_transfer;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class EventTest {

    private static final Object SOURCE = new Object();
    private static final String FX_ID = "fxId";
    private static final String ACTION = "action";

    @Test
    public void initEventTest() {
        Event event = new Event(SOURCE, FX_ID, ACTION);

        assertEquals(event.getSource(), SOURCE);
        assertEquals(event.fxId, FX_ID);
        assertEquals(event.action, ACTION);
    }
}
