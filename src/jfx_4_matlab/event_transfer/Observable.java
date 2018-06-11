package jfx_4_matlab.event_transfer;

/**
 * Adapted from here:
 * http://undocumentedmatlab.com/blog/matlab-callbacks-for-java-events
 *
 * An instance of this class can be observed from the MATLAB application. It
 * transfers ui event_transfer to the MATLAB application.
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
    public synchronized void addUiEventListener(UiEventListener lis) {
        listeners.addElement(lis);
    }

    /**
     * Removes a listener.
     * @param lis   Listener to be removed.
     */
    public synchronized void removeUiEventListener(UiEventListener lis) {
        listeners.removeElement(lis);
    }

    public interface UiEventListener extends java.util.EventListener {
        void uiEvent(UiEvent event);
    }

    /**
     * Events to be transferred.
     */
    public class UiEvent extends java.util.EventObject {

        private static final long serialVersionUID = 1L;

        /**
         * FxId of the control which initially threw the event_transfer.
         */
        public String fxId;

        /**
         * The action called on the control.
         */
        public String action;

        UiEvent(Object obj, String fxId, String action) {
            super(obj);
            this.fxId = fxId;
            this.action = action;
        }
    }

    /**
     * This method sends an event_transfer to each listener.
     * @param fxId          FxId of the control which initially threw the event_transfer
     * @param action        Action called on the control
     */
    public void notifyListeners(final String fxId, final String action) {
        java.util.Vector dataCopy;
        synchronized(this) {
            dataCopy = (java.util.Vector) listeners.clone();
        }
        for (int i=0; i < dataCopy.size(); i++) {
            ((UiEventListener)dataCopy.elementAt(i)).uiEvent(
                    new UiEvent(this, fxId, action)
                );
        }
    }
}

