package jfx_4_matlab_java.jfx_thread;

import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;

public class SyncTaskExecutionTest {

    @Test
    public void executeTaskTest() throws NoSuchMethodException, InterruptedException {
        String object = "string";
        Method method = String.class.getMethod("substring", int.class);
        Task task = new Task(object, method, new Object[]{object.length() - 1});

        SyncTaskExecution ste = new SyncTaskExecution(task);
        ste.run();
        ste.waitTillExecutionIsFinished();

        assertEquals(ste.getReturnValue(), "g");
    }
}
