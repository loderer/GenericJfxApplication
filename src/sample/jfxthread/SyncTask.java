package sample.jfxthread;

import java.lang.reflect.InvocationTargetException;

public class SyncTask implements Runnable{

    private Task task;
    private boolean executionFinished;
    public Object monitor;
    public Object returnValue;

    public SyncTask(final Task task) {
        this.task = task;
        this.executionFinished = false;
        this.monitor = new Object();
        this.returnValue = null;
    }

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
