package generic_jfx_application.jfx_thread;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TaskTest {

    @Test
    public void initTaskTest() {
        Object uiControl = new Object();
        Method method = Mockito.mock(Method.class);
        Object[] args = new Object[]{new Object(), new Object()};
        Task sut = new Task(uiControl, method, args);

        assertEquals(sut.getObject(), uiControl);
        assertEquals(sut.getMethod(), method);
        assertTrue(Arrays.deepEquals(sut.getArgs(), args));
    }
}
