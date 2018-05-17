package sample_app.events;

import javafx.event.Event;
import javafx.scene.control.Control;

/**
 * Each controllers base class. It allows to send events via the observable.
 */
public class Controller {

    /**
     * Will be executionFinished if sendEvent is called.
     */
    private Observable observable;

    /**
     * Init Controller
     * @param observable    This observable will be executionFinished if sendEvent is called.
     */
    public void setObservable(final Observable observable) {
        this.observable = observable;
    }

    /**
     * Notifies each observer of the observable.
     * @param evt
     */
    public void sendEvent(Event evt) {
        observable.notifyListeners(
                ((Control)evt.getSource()).getId(),
                evt.getEventType().getName());
    }
}
