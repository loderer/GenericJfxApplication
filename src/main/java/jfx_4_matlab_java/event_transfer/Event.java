package jfx_4_matlab_java.event_transfer;

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
