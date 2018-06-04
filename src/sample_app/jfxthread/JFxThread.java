package sample_app.jfxthread;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Allows the invocation of any available method on each existing ui-element
 * from MATLAB.
 */
public class JFxThread {

    /**
     * Scene housing the ui-elements.
     */
    private Scene scene;

    /**
     * Deposited tasks.
     */
    private List<Task> tasks;
    private final Object tasksMonitor;

    /**
     * The corresponding fxml-file.
     */
    private final String fxmlFile;

    private final Stage stage;

    private FXMLLoader fxmlLoader;

    public JFxThread(final String fxmlFile, final Stage stage) {
        this.tasksMonitor = new Object();
        this.tasks = new ArrayList<Task>();
        this.fxmlFile = fxmlFile;
        this.stage = stage;
    }

    public void setScene(final Scene scene ){
        this.scene = scene;
    }

    /**
     * Deposit a task without parameters.
     * @param fxId      The ui-element to invoke the method on.
     * @param method    The method to be invoked on the ui-element.
     */
    public void pushBackTask(final String fxId, final String method){
        pushBackTask(fxId, method, new Object[0]);
    }

    /**
     * Deposit a task.
     * @param fxId      The ui-element to invoke the method on.
     * @param method    The method to be invoked on the ui-element.
     * @param args      The arguments of the method.
     */
    public void pushBackTask(final String fxId, final String method,
                             final Object... args){
        Task task = getTask(fxId, method, args);

        if(task != null) {
            synchronized (tasksMonitor) {
                tasks.add(task);
            }
        }
    }

    /**
     * Tries to generate a task from the given parameters.
     * @param fxId      Id of the ui-element.
     * @param method    Method to be called.
     * @param args      Arguments the method expects.
     * @return          Return-value of the method.
     */
    private Task getTask(String fxId, String method, Object[] args) {
        Task task = null;
        // Fetch parameter classes.
        List<Class<?>> argClasses = new ArrayList<Class<?>>();
        for(Object arg : args) {
            argClasses.add(arg.getClass());
        }

        Object uiControl  = null;

        if(fxmlLoader != null) {
               uiControl = fxmlLoader.getNamespace().get(fxId);
        } else {
            System.err.println("FXMLLoader not set!!!");
        }


        if(uiControl != null) {
            try {
                final Method methodHandle =
                        getMethod(uiControl, method, argClasses);

                task = new Task(uiControl, methodHandle, args);
            } catch (NoSuchMethodException e) {
                String classes = "";
                for(Object arg : args) {
                    classes += ", " + arg.getClass();
                }
                classes = classes.substring(2);

                System.err.println(String.format("The ui-element does not " +
                        "provide a method with the name \"%s\" and parameter " +
                        "of classes \"%s\". (fxId: %s, fxml: %s)", method,
                        classes, fxId, fxmlFile));
            }
        } else {
            System.err.println(String.format("There is no ui-element with the " +
                    "id: \"%s\" in this scene. (fxml: %s)", fxId, fxmlFile));
        }
        return task;
    }

    /**
     * Executes the deposited tasks in the order they were submitted.
     * After execution the list of deposited tasks is cleared.
     */
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

    /**
     * Run a task without parameters synchronous.
     * @param fxId      The ui-element to invoke the method on.
     * @param method    The method to be invoked on the ui-element.
     * @return          Result of the method invocation.
     */
    public Object applyTask(final String fxId, final String method) {
        return applyTask(fxId, method, new Object[0]);
    }

    /**
     * Run a task with parameters synchronous.
     * @param fxId      The ui-element to invoke the method on.
     * @param method    The method to be invoked on the ui-element.
     * @param args      The arguments of the method.
     * @return          Result of the method invocation.
     */
    public Object applyTask(final String fxId, final String method,
                            final Object... args) {
        Object returnValue = null;

        final Task task = getTask(fxId, method, args);

        if(task != null) {
            SyncTaskExecution syncTask = new SyncTaskExecution(task);
            Platform.runLater(syncTask);

            try {
                // Wait till the result is available.
                synchronized (syncTask.monitor) {
                    while(!syncTask.isExecutionFinished()) {
                        syncTask.monitor.wait();
                    }
                }
            } catch (InterruptedException e) {
                // do nothing
            }
            returnValue = syncTask.returnValue;
        }

        return returnValue;
    }

    /**
     * Fetch a method matching the given signature.
     * @param uiControl     The ui-element to invoke the method on.
     * @param method        The methods name.
     * @param argClasses    The classes of the parameter.
     * @return              A method matching the given signature.
     * @throws NoSuchMethodException    If no method matches the signature.
     */
    private Method getMethod(Object uiControl, String method,
                             List<Class<?>> argClasses)
            throws NoSuchMethodException {
        if(argClasses.size() == 0) {
            return uiControl.getClass().getMethod(method,
                    argClasses.toArray(new Class<?>[0]));
        } else {
            // Consider super-classes, interfaces and primitives while
            // reflecting method.
            MethodReflector mr =
                    new MethodReflector(uiControl, method, argClasses);
            return mr.getMethod();
        }
    }

    public void setFxmlLoader(FXMLLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
    }

    public void closeStage() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.close();
            }
        });
    }
}
