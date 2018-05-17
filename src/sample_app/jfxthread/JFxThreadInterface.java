package sample_app.jfxthread;

public interface JFxThreadInterface {
    /**
     * Deposit a task without parameters.
     * @param fxId      The ui-element to invoke the method on.
     * @param method    The method to be invoked on the ui-element.
     */
    void pushBackTask(final String fxId, final String method);

    /**
     * Deposit a task.
     * @param fxId      The ui-element to invoke the method on.
     * @param method    The method to be invoked on the ui-element.
     * @param args      The arguments of the method.
     */
    void pushBackTask(final String fxId, final String method,
                             final Object... args);

    /**
     * Executes the deposited tasks in the order they were submitted.
     * After execution the list of deposited tasks is cleared.
     */
    void applyTasks();

    /**
     * Run a task without parameters synchronous.
     * @param fxId      The ui-element to invoke the method on.
     * @param method    The method to be invoked on the ui-element.
     * @return          Result of the method invocation.
     */
    Object applyTask(final String fxId, final String method);

    /**
     * Run a task with parameters synchronous.
     * @param fxId      The ui-element to invoke the method on.
     * @param method    The method to be invoked on the ui-element.
     * @param args      The arguments of the method.
     * @return          Result of the method invocation.
     */
    Object applyTask(final String fxId, final String method,
                            final Object... args);
}
