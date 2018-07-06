package mockito_extension;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.mockito.internal.creation.bytebuddy.InlineByteBuddyMockMaker;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.plugins.MockMaker;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This MockMaker was initially created by the stackoverflow user eckig
 * (https://stackoverflow.com/questions/28245555/how-do-you-mock-a-javafx-toolkit-initialization).
 *
 * It allows mocking javaFX-Objects whose initialization depends on the
 * correct initialization of the javaFX-runtime. The code has been adapted
 * to fulfill the coding-guidelines of java 1.7. Ditto the wrapped MockMaker
 * has been replaced by the InlineByteBuddyMockMaker. If mockito 2.x is
 * provided this enables mocking final as well as static functions and classes.
 *
 * The code is licenced under the cc by-sa 3.0 license
 * (https://creativecommons.org/licenses/by-sa/3.0/).
 */
public class JavaFXMockMaker implements MockMaker {

    private final MockMaker wrapped = new InlineByteBuddyMockMaker();
    private boolean jfxIsSetup;

    private void doOnJavaFXThread(final Runnable pRun) throws RuntimeException {
        if (!jfxIsSetup) {
            setupJavaFX();
            jfxIsSetup = true;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                pRun.run();
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setupJavaFX() throws RuntimeException {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JFXPanel(); // initializes JavaFX environment
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T createMock(final MockCreationSettings<T> settings, final MockHandler handler) {
        final AtomicReference<T> result = new AtomicReference<>();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                result.set(wrapped.createMock(settings, handler));
            }
        };


        doOnJavaFXThread(run);
        return result.get();
    }

    @Override
    public MockHandler getHandler(final Object mock) {
        final AtomicReference<MockHandler> result = new AtomicReference<>();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                result.set(wrapped.getHandler(mock));
            }
        };
        doOnJavaFXThread(run);
        return result.get();
    }

    @Override
    public void resetMock(final Object mock, final MockHandler newHandler, @SuppressWarnings("rawtypes") final MockCreationSettings settings) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                wrapped.resetMock(mock, newHandler, settings);
            }
        };
        doOnJavaFXThread(run);
    }

    @Override
    public TypeMockability isTypeMockable(Class<?> aClass) {
        return wrapped.isTypeMockable(aClass);
    }
}