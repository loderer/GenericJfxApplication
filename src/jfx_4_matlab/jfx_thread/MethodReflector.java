package jfx_4_matlab.jfx_thread;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Uses reflection to determine a method. Considers
 * super-classes, interfaces and primitives.
 */
public class MethodReflector {

    /**
     * Enables translating box-types to primitive types.
     */
    public static final Map<Class<?>, Class<?>> BOX_TYPE_2_PRIMITIVE = new HashMap<Class<?>, Class<?>>() {
        {
            put(Boolean.class, boolean.class);
            put(Byte.class, byte.class);
            put(Character.class, char.class);
            put(Float.class, float.class);
            put(Integer.class, int.class);
            put(Long.class, long.class);
            put(Short.class, short.class);
            put(Double.class, double.class);
        }
    };

    /**
     * The object to determine method for.
     */
    private final Object object;

    /**
     * The name of the required method.
     */
    private final String method;

    /**
     * The classes of the parameters of the method.
     */
    private final List<Class<?>> argClasses;

    /**
     * All available permutations of the parameter-classes considering
     * super-classes, interfaces and primitive types.
     */
    private List<List<Class<?>>> permutations;

    /**
     * An iterator for each list in permutations.
     */
    private List<Iterator<Class<?>>> permutationIterators;

    /**
     * The permutation to be checked next.
     */
    private List<Class<?>> actPermutation;

    public MethodReflector(Object object, String method, List<Class<?>> argClasses) {

        this.object = object;
        this.method = method;
        this.argClasses = argClasses;

        this.permutations  = new ArrayList<List<Class<?>>>();
        this.permutationIterators = new ArrayList<Iterator<Class<?>>>();
        this.actPermutation = new ArrayList<Class<?>>();

        initPermutations(argClasses);
    }

    /**
     * Determine available permutations.
     * @param argClasses    Initial parameter-classes.
     */
    private void initPermutations(List<Class<?>> argClasses) {
        for(int i = 0; i < argClasses.size(); i++) {
            ArrayList<Class<?>> permutation = new ArrayList<Class<?>>();
            Class<?> clss = argClasses.get(i);
            while(!clss.equals(Object.class)) {
                permutation.add(clss);

                // primitive class
                if(BOX_TYPE_2_PRIMITIVE.containsKey(clss))
                    permutation.add(BOX_TYPE_2_PRIMITIVE.get(clss));

                // interfaces
                permutation.addAll(Arrays.asList(clss.getInterfaces()));
                // super-class
                clss = clss.getSuperclass();
            }
            permutation.add(clss);  // add Object.class
            permutations.add(permutation);

            // Init permutationIterators and actPermutation.
            Iterator<Class<?>> permutationIterator = permutation.iterator();
            actPermutation.add(permutationIterator.next());
            permutationIterators.add(permutationIterator);
        }
    }

    /**
     * Get method matching signature.
     * @return  Method matching the signature given at ctor.
     * @throws NoSuchMethodException
     */
    public Method getMethod() throws NoSuchMethodException {
        try {
            return object.getClass().getMethod(method, actPermutation.toArray(new Class<?>[0]));
        } catch (NoSuchMethodException e) {
            if(nextPermutation(0))
                return getMethod();
            else
                throw e;
        }
    }

    /**
     * nextPermutation(0) - Determines the next permutation by calling itself
     * recursively.
     * @param position  The position of the first iterator to be called.
     * @return  true - If there is a new permutation available.
     *          false - If there is no more permutation available.
     */
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
