package sample_app.events;

/**
 * Adapted from here:
 * http://undocumentedmatlab.com/blog/matlab-callbacks-for-java-events
 *
 * An instance of this class can be observed from the MATLAB-backend. It
 * transfers ui events to the backend.
 */
public class Observable {

    private java.util.Vector listeners = new java.util.Vector();

    public synchronized void addUiEventListener(UiEventListener lis) {
        listeners.addElement(lis);
    }

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
         * FxId of the control which initially threw the event.
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
     * This method sends an event to each observer.
     * @param fxId          FxId of the control which initially threw the event
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

