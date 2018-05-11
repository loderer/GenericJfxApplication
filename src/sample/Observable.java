package sample;

import javafx.event.Event;

public class Observable {

    private java.util.Vector data = new java.util.Vector();

    public synchronized void addUiEventListener(UiEventListener lis) {
        data.addElement(lis);
    }

    public synchronized void removeUiEventListener(UiEventListener lis) {
        data.removeElement(lis);
    }

    public interface UiEventListener extends java.util.EventListener {
        void uiEvent(UiEvent event);
    }

    public class UiEvent extends java.util.EventObject {

        private static final long serialVersionUID = 1L;

        public String fxId;

        public String action;

        UiEvent(Object obj, String fxId, String action) {
            super(obj);
            this.fxId = fxId;
            this.action = action;
        }
    }

    public void notifyListeners(final Event event) {
        java.util.Vector dataCopy;
        synchronized(this) {
            dataCopy = (java.util.Vector)data.clone();
        }
        for (int i=0; i < dataCopy.size(); i++) {
            ((UiEventListener)dataCopy.elementAt(i)).uiEvent(
                    new UiEvent(this, "fxid", "action")
            );
        }
    }
}

