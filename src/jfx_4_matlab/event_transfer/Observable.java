package jfx_4_matlab.event_transfer;

/**
 * Adapted from here:
 * http://undocumentedmatlab.com/blog/matlab-callbacks-for-java-events
 *
 * An instance of this class can be observed from the MATLAB application. It
 * transfers ui events to the MATLAB application.
 */
public class Observable {

    /**
     * All registered listeners.
     */
    private java.util.Vector listeners = new java.util.Vector();

    /**
     * Adds a listener.
     * @param lis   Listener to be added.
     */
    public synchronized void addEventListener(EventListener lis) {
        listeners.addElement(lis);
    }

    /**
     * Removes a listener.
     * @param lis   Listener to be removed.
     */
    public synchronized void removeEventListener(EventListener lis) {
        listeners.removeElement(lis);
    }

    /**
     * This method sends an event to each listener.
     * @param fxId          FxId of the control which initially threw the event_transfer
     * @param action        Action called on the control
     */
    public void notifyListeners(final String fxId, final String action) {
        java.util.Vector dataCopy;
        synchronized(this) {
            dataCopy = (java.util.Vector) listeners.clone();
        }
        for (int i=0; i < dataCopy.size(); i++) {
            ((EventListener)dataCopy.elementAt(i)).event(
                    new Event(this, fxId, action)
                );
        }
    }
}

