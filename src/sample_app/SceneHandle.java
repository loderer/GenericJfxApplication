package sample_app;

import sample_app.events.Observable;
import sample_app.jfxthread.JFxThread;
import sample_app.jfxthread.JFxThreadInterface;

public class SceneHandle implements JFxThreadInterface {

    private final Observable observable;

    private final JFxThread jfxThread;

    public SceneHandle(final Observable observable, final JFxThread jfxThread) {
        this.observable = observable;
        this.jfxThread = jfxThread;
    }

    public Observable getObservable() {
        return observable;
    }

    @Override
    public void pushBackTask(String fxId, String method) {
        jfxThread.pushBackTask(fxId, method);
    }

    @Override
    public void pushBackTask(String fxId, String method, Object... args) {
        jfxThread.pushBackTask(fxId, method, args);
    }

    @Override
    public void applyTasks() {
        jfxThread.applyTasks();
    }

    @Override
    public Object applyTask(String fxId, String method) {
        return jfxThread.applyTask(fxId, method);
    }

    @Override
    public Object applyTask(String fxId, String method, Object... args) {
        return jfxThread.applyTask(fxId, method, args);
    }
}
