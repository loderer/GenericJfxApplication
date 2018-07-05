package jfx_4_matlab_java.jfx_thread;

import java.lang.reflect.InvocationTargetException;

/**
 * The application requires the synchronous execution of an runnable to easily
 * fetch information from the gui. This implementation allows to wait until the
 * execution has finished. It also enables fetching the result of the executed
 * task.
 */
public class SyncTaskExecution implements Runnable{

    /**
     * Task to be executed.
     */
    private Task task;

    /**
     * Flag indicating if the execution has finished.
     */
    private boolean executionFinished;
    public Object monitor;

    /**
     * Return value of the task.
     */
    public Object returnValue;

    public SyncTaskExecution(final Task task) {
        this.task = task;
        this.executionFinished = false;
        this.monitor = new Object();
        this.returnValue = null;
    }

    /**
     * Publishes that the execution has finished and the return value is available.
     */
    public void finishExecution() {
        synchronized (monitor) {
            executionFinished = true;
            monitor.notify();
        }
    }

    public boolean isExecutionFinished() {
        return executionFinished;
    }

    @Override
    public void run() {
        try {
            returnValue = task.method.invoke(task.uiControl, task.args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        finishExecution();
    }
}
