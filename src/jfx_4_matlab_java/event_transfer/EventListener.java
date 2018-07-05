package jfx_4_matlab_java.event_transfer;

/**
 * This interface has to be implemented by each listener which will
 * register to the observable.
 */
public interface EventListener extends java.util.EventListener {
    void event(Event event);
}
