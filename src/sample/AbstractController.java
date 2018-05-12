package sample;

import javafx.event.Event;
import javafx.scene.control.Control;

/**
 * Each controllers base class. It allows to send events via the observable.
 */
public abstract class AbstractController {
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
                getControllerName(),
                ((Control)evt.getSource()).getId(),
                evt.getEventType().getName());
    }

    /**
     * Discovers the class-name of the derivative.
     * @return
     */
    abstract String getControllerName();
}
