package sample.jfxthread;

public class Task {
    /**
     * FxId of an ui element.
     */
    public final Object uiControl;

    /**
     * Name of the method to be called on the ui element.
     */
    public final java.lang.reflect.Method method;

    /**
     * Input parameter of the method.
     */
    public final Object[] args;

    public Task(final Object uiControl, final java.lang.reflect.Method method, final Object[] args) {
        this.uiControl = uiControl;
        this.method = method;
        this.args = args;
    }
}
