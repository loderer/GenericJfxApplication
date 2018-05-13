package sample.jfxthread;

import javafx.application.Platform;
import javafx.scene.Scene;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
            List<Class<?>> argClasses = new ArrayList<Class<?>>();
            for(Object arg : args) {
                argClasses.add(arg.getClass());
            }

            final Object uiControl = scene.lookup("#" + fxId);
            final java.lang.reflect.Method methodHandle = getMethod(uiControl, method, argClasses);

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
            List<Class<?>> argClasses = new ArrayList<Class<?>>();
            for(Object arg : args) {
                argClasses.add(arg.getClass());
            }

            final Object uiControl = scene.lookup("#" + fxId);
            final java.lang.reflect.Method methodHandle = getMethod(uiControl, method, argClasses);

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

    private Method getMethod(Object uiControl, String method, List<Class<?>> argClasses) throws NoSuchMethodException {
        if(argClasses.size() == 0) {
            return uiControl.getClass().getMethod(method, argClasses.toArray(new Class<?>[0]));
        } else {
            MethodReflector mr = new MethodReflector(uiControl, method, argClasses);
            return mr.getMethod();
        }
    }

    // TODO: teste mit primitiven
    private class MethodReflector {

        private final Object uiControl;
        private final String method;
        private final List<Class<?>> argClasses;

        private List<List<Class<?>>> permutations;
        private List<Iterator<Class<?>>> permutationIterators;
        private List<Class<?>> actPermutation;

        public MethodReflector(Object uiControl, String method, List<Class<?>> argClasses) {

            this.uiControl = uiControl;
            this.method = method;
            this.argClasses = argClasses;

            this.permutations  = new ArrayList<List<Class<?>>>();
            this.permutationIterators = new ArrayList<Iterator<Class<?>>>();
            this.actPermutation = new ArrayList<Class<?>>();


            initPermutations(argClasses);
        }

        private void initPermutations(List<Class<?>> argClasses) {
            for(int i = 0; i < argClasses.size(); i++) {
                ArrayList<Class<?>> permutation = new ArrayList<Class<?>>();
                Class<?> clss = argClasses.get(i);
                while(!clss.equals(Object.class)) {
                    permutation.add(clss);
                    permutation.addAll(Arrays.asList(clss.getInterfaces()));
                    clss = clss.getSuperclass();
                }
                permutation.add(clss);  // add Object.class
                permutations.add(permutation);
                Iterator<Class<?>> permutationIterator = permutation.iterator();
                actPermutation.add(permutationIterator.next());
                permutationIterators.add(permutationIterator);
            }
        }

        public Method getMethod() throws NoSuchMethodException {
            try {
                return uiControl.getClass().getMethod(method, actPermutation.toArray(new Class<?>[0]));
            } catch (NoSuchMethodException e) {
                if(nextPermutation(0))
                    return getMethod();
                else
                    throw e;
            }
        }

        private boolean nextPermutation(final int position) {
            if(position >= permutationIterators.size())
                return false;
            Iterator<Class<?>> permutationIterator = permutationIterators.get(position);
            if(permutationIterator.hasNext()) {
                actPermutation.set(position, permutationIterator.next());
                return true;
            } else {
                // Überlauf auf den nächst höherwertigen Iterator
                permutationIterators.set(position, permutations.get(position).iterator());
                actPermutation.set(position, permutationIterators.get(position).next());
                return nextPermutation(position + 1);
            }
        }
    }

}
