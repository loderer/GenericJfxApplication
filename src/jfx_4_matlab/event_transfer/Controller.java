package jfx_4_matlab.event_transfer;

import javafx.event.Event;
import javafx.scene.control.Control;

/**
 * Each controllers base class. It allows to send event_transfer via the observable.
 */
public class Controller {

    /**
     * Will be executionFinished if notifyListeners is called.
     */
    private Observable observable;

    /**
     * Init Controller
     * @param observable    This observable will be executionFinished if notifyListeners is called.
     */
    public void setObservable(final Observable observable) {
        this.observable = observable;
    }

    /**
     * Notifies each observer of the observable.
     * @param evt
     */
    public void notifyListeners(Event evt) {
        observable.notifyListeners(
                ((Control)evt.getSource()).getId(),
                evt.getEventType().getName());
    }
}
