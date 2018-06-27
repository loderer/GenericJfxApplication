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
     * All registered observer.
     */
    private java.util.Vector observer = new java.util.Vector();

    /**
     * Adds a listener.
     * @param lis   Listener to be added.
     */
    public synchronized void addObserver(Observer lis) {
        observer.addElement(lis);
    }

    /**
     * Removes a listener.
     * @param lis   Listener to be removed.
     */
    public synchronized void removeObserver(Observer lis) {
        observer.removeElement(lis);
    }

    /**
     * This method sends an event_transfer to each listener.
     * @param fxId          FxId of the control which initially threw the event_transfer
     * @param action        Action called on the control
     */
    public void notifyObserver(final String fxId, final String action) {
        java.util.Vector dataCopy;
        synchronized(this) {
            dataCopy = (java.util.Vector) observer.clone();
        }
        for (int i=0; i < dataCopy.size(); i++) {
            ((Observer)dataCopy.elementAt(i)).notify(
                    new Event(this, fxId, action)
                );
        }
    }

    /**
     * This interface has to be implemented by each observer listening for events
     * on the Observable class.
     */
    public interface Observer extends java.util.EventListener {
        void notify(Event event);
    }

    /**
     * Events to be transferred.
     */
    public class Event extends java.util.EventObject {

        private static final long serialVersionUID = 1L;

        /**
         * FxId of the control which initially threw the event_transfer.
         */
        public String fxId;

        /**
         * The action called on the control.
         */
        public String action;

        Event(Object obj, String fxId, String action) {
            super(obj);
            this.fxId = fxId;
            this.action = action;
        }
    }
}

