package jfx_4_matlab_java.jfx_thread;

import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class MethodReflectorTest {

    @Test
    public void reflectMethodTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String object = "test";
        MethodReflector mr = new MethodReflector(object, "toString", new ArrayList<Class<?>>());

        Method method = mr.getMethod();

        assertEquals(method.invoke(object), "test");
    }

    @Test
    public void reflectMethodWithParameterTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Class<?>> paramClasses = new ArrayList<>();
        paramClasses.add(Integer.class);
        paramClasses.add(Integer.class);
        String object = "1test";
        MethodReflector mr = new MethodReflector(object, "substring", paramClasses);

        Method method = mr.getMethod();

        assertEquals(method.invoke(object, 1, object.length()), "test");
    }
}
