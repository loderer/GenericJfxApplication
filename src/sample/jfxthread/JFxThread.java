package sample.jfxthread;

import javafx.application.Platform;
import javafx.scene.Scene;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JFxThread {

    private final Scene scene;
    private final Object tasksMonitor;
    private List<Task> tasks;

    public JFxThread(final Scene scene) {
        this.scene = scene;
        this.tasksMonitor = new Object();
        this.tasks = new ArrayList<Task>();
    }

    public void pushBackTask(final String fxId, final String method){
        pushBackTask(fxId, method, new Object[0]);
    }

    public void pushBackTask(final String fxId, final String method, final Object... args){
        try {
            List<Class<?>> argsClasses = new ArrayList<Class<?>>();
            for(Object arg : args) {
                argsClasses.add(arg.getClass());
            }

            final Object uiControl = scene.lookup("#" + fxId);
            final java.lang.reflect.Method methodHandle = uiControl.getClass().getMethod(method, argsClasses.toArray(new Class<?>[0]));

            synchronized (tasksMonitor) {
                tasks.add(new Task(uiControl, methodHandle, args));
            }
        } catch (NoSuchMethodException e) {
            String classes = "";
            for(Object arg : args) {
                classes += ", " + arg.getClass();
            }
            classes = classes.substring(2);

            System.err.println(String.format("The ui-element with the fxId \"%s\" " +
                    "does not provide a method with the name \"%s\" and parameter " +
                    "of classes \"%s\".", fxId, method, classes));
        }
    }

    public void applyTasks() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Iterator<Task> iterator = tasks.iterator();
                    Task task;
                    while(iterator.hasNext()) {
                        synchronized (tasksMonitor) {
                            task = iterator.next();
                            iterator.remove();
                        }
                        task.method.invoke(task.uiControl, task.args);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Object applyTask(final String fxId, final String method) {
        return applyTask(fxId, method, new Object[0]);
    }

    public Object applyTask(final String fxId, final String method, final Object... args) {
        Object returnValue = null;
        try {
            List<Class<?>> argsClasses = new ArrayList<Class<?>>();
            for(Object arg : args) {
                argsClasses.add(arg.getClass());
            }

            final Object uiControl = scene.lookup("#" + fxId);
            final java.lang.reflect.Method methodHandle = uiControl.getClass().getMethod(method, argsClasses.toArray(new Class<?>[0]));

            final Task task = new Task(uiControl, methodHandle, args);

            SyncTask syncTask = new SyncTask(task);
            Platform.runLater(syncTask);

            synchronized (syncTask.monitor) {
                while(!syncTask.isExecutionFinished()) {
                    syncTask.monitor.wait();
                }
            }

            returnValue = syncTask.returnValue;
        } catch (NoSuchMethodException e) {
            String classes = "";
            for(Object arg : args) {
                classes += ", " + arg.getClass();
            }
            classes = classes.substring(2);

            System.err.println(String.format("The ui-element with the fxId \"%s\" " +
                    "does not provide a method with the name \"%s\" and parameter " +
                    "of classes \"%s\".", fxId, method, classes));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

//    // TODO
//    private Method getMethod(Object uiControl, String method, List<Class<?>> argsClasses, List<Class<?>> permutation) throws NoSuchMethodException {
//        try {
//            return uiControl.getClass().getMethod(method, permutation.toArray(new Class<?>[0]));
//        } catch (NoSuchMethodException e) {
//            boolean finished = true;
//            for(Class<?> clss : permutation) {
//                if(!clss.equals(Object.class)) {
//                    finished = false;
//                    break;
//                }
//            }
//            if(finished) {
//                throw e;
//            } else {
//                Iterator<Class<?> >
//            }
//        }
//
//    }
}
