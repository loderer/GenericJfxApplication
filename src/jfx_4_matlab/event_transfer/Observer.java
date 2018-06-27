package jfx_4_matlab.event_transfer;

/**
 * This interface has to be implemented by each observer listening for events
 * on the Observable class.
 */
public interface Observer extends java.util.EventListener {
    void notify(Event event);
}
