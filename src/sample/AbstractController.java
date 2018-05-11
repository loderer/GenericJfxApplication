package sample;

import javafx.event.Event;
import javafx.scene.control.Control;

public abstract class AbstractController {
    private Observable observable;

    public void setObservable(final Observable observable) {
        this.observable = observable;
    }

    public void sendEvent(Event evt) {
        observable.notifyListeners(
                getControllerName(),
                ((Control)evt.getSource()).getId(),
                evt.getEventType().getName());
    }

    abstract String getControllerName();
}
