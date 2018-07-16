package generic_jfx_application.jfx_thread;

import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JFXThreadTest {

    private JFXThread jfxThread;
    private FXMLLoader fxmlLoader;

    @BeforeMethod
    public void setUp() {
        this.fxmlLoader = Mockito.mock(FXMLLoader.class);
        this.jfxThread = new JFXThread("sample.fxml");
    }

    @Test
    public void applyTask1() throws NoSuchMethodException {
        this.jfxThread.setFxmlLoader(fxmlLoader);
        String result = (String) jfxThread.applyTask("1test", "substring", 1);

        assertEquals(result, "test");
    }

    @Test
    public void applyTask2() throws NoSuchMethodException {
        this.jfxThread.setFxmlLoader(fxmlLoader);
        String result = (String) jfxThread.applyTask(new Integer(42), "toString");

        assertEquals(result, "42");
    }

    @Test
    public void pushBackTaskTest() throws InterruptedException, NoSuchMethodException {
        this.jfxThread.setFxmlLoader(fxmlLoader);
        Incrementable incrementable = new Incrementable(0);

        jfxThread.pushBackTask(incrementable, "incrementBy", 1);
        jfxThread.pushBackTask(incrementable, "incrementBy", 2);
        jfxThread.pushBackTask(incrementable, "finish");
        jfxThread.applyTasks();
        incrementable.waitTillFinished();

        assertEquals(incrementable.getValue(), 3);
    }

    private class Incrementable {
        private int value;

        private boolean finished = false;
        private final Object monitor = new Object();

        public Incrementable(final int value) {
            this.value = value;
        }

        public void incrementBy(final int value) {
            this.value = this.value + value;
        }

        public void waitTillFinished() throws InterruptedException {
            synchronized (monitor) {
                while(!finished) {
                    monitor.wait();
                }
            }
        }

        public void finish() {
            synchronized (monitor) {
                finished = true;
                monitor.notify();
            }
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void getUiElementTest() throws Exception {
        this.jfxThread.setFxmlLoader(fxmlLoader);
        ObservableMap<String, Object> nameSpace = Mockito.mock( ObservableMap.class);
        Mockito.when(nameSpace.get("fxId")).thenReturn(new String("heureka"));
        Mockito.when(fxmlLoader.getNamespace()).thenReturn(nameSpace);

        String returnValue = (String) jfxThread.getUiElement("fxId");

        assertEquals(returnValue, "heureka");
    }

    @Test(expectedExceptions = NoSuchMethodException.class)
    public void noSuchMethodTest1() throws NoSuchMethodException {
        this.jfxThread.setFxmlLoader(fxmlLoader);
        jfxThread.applyTask(new Integer(42), "noMethodName");
    }

    @Test(expectedExceptions = NoSuchMethodException.class)
    public void noSuchMethodTest2() throws NoSuchMethodException {
        this.jfxThread.setFxmlLoader(fxmlLoader);
        jfxThread.applyTask(new Integer(42), "noMethodName", 2.5, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void noUiElementTest() throws NoSuchMethodException {
        this.jfxThread.setFxmlLoader(fxmlLoader);
        jfxThread.applyTask(null, "toString");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void fxmlLoaderNotInitializedTest() {
        jfxThread.getUiElement("fxId");
    }
}
