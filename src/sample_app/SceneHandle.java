package sample_app;

import sample_app.events.Observable;
import sample_app.jfxthread.JFxThread;

public class SceneHandle {

    private final Observable observable;

    private final JFxThread jfxThread;

    public SceneHandle(final Observable observable, final JFxThread jfxThread) {
        this.observable = observable;
        this.jfxThread = jfxThread;
    }

    public Observable getObservable() {
        return observable;
    }

    public JFxThread getJfxThread() {
        return jfxThread;
    }
}
