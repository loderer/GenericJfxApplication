package generic_jfx_application.event_transfer;

import javafx.event.Event;
import javafx.scene.control.Control;

/**
 * Each controllers base class. It allows to send event_transfer via the observable.
 */
public class Controller {

    /**
     * Will be notified if handleEvent is called.
     */
    private Observable observable;

    /**
     * Init Controller
     * @param observable    This observable will be notified if handleEvent is called.
     */
    public void setObservable(final Observable observable) {
        this.observable = observable;
    }

    /**
     * Notifies each observer of the observable.
     * @param evt   Event to be published.
     */
    public void handleEvent(Event evt) {
        observable.notifyListeners(
                ((Control)evt.getSource()).getId(),
                evt.getEventType().getName());
    }
}
