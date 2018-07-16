package generic_jfx_application.jfx_thread;

import java.lang.reflect.Method;

/**
 * A task represents the invocation of a method on an object.
 */
public class Task {
    /**
     * FxId of an ui element.
     */
    private final Object object;

    /**
     * Name of the method to be called on the ui element.
     */
    private final java.lang.reflect.Method method;

    /**
     * Input parameter of the method.
     */
    private final Object[] args;

    public Task(final Object object, final java.lang.reflect.Method method, final Object[] args) {
        this.object = object;
        this.method = method;
        this.args = args;
    }

    public Object getObject() {
        return object;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }
}
