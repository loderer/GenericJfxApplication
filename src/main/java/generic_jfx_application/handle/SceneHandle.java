package generic_jfx_application.handle;

import generic_jfx_application.event_transfer.Observable;
import generic_jfx_application.jfx_thread.JFXThread;

/**
 * Contains all scene data which is required at the MATLAB application.
 */
public class SceneHandle {

    private final Observable observable;

    private final JFXThread jfxThread;

    public SceneHandle(final Observable observable, final JFXThread jfxThread) {
        this.observable = observable;
        this.jfxThread = jfxThread;
    }

    public Observable getObservable() {
        return observable;
    }

    public JFXThread getJfxThread() {
        return jfxThread;
    }
}
