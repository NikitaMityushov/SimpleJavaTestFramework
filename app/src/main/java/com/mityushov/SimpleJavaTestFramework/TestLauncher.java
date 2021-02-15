package com.mityushov.SimpleJavaTestFramework;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.mityushov.SimpleJavaTestFramework.annotations.*;

/**
 *
 */

public class TestLauncher {
    /**
     * Static method, launch the test.
     * Accepts full name of test class
     * @param testClassName
     */
    public static synchronized void startTest(String testClassName) {
        try {
            final Class<?> testClass = Class.forName(testClassName);
            final List<Method> beforeMethods = getListOfAnnotatedMethods(testClass, Before.class);
            final List<Method> testMethods = getListOfAnnotatedMethods(testClass, Test.class);
            final List<Method> afterMethods = getListOfAnnotatedMethods(testClass, After.class);
            //output statistics
            Map<String, Integer> statistics = startTestAndGetStatistics(testClass, beforeMethods,
                                                                        testMethods, afterMethods);
            statistics.forEach((k,v) -> System.out.printf("\n%s%s", k, v));

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.out.println("The class is not found");
            e.printStackTrace();
        }
    }

    /**
     * Private static method, which returns List of annotated methods.
     * Accepts Class object of test class and Class object of chosen annotation
     * @param testClass
     * @param annotation
     * @return
     */
    private static List<Method> getListOfAnnotatedMethods(Class<?> testClass,
                                                             Class<? extends Annotation> annotation) {
        Method[] methods = testClass.getDeclaredMethods();
        final List<Method> methodsList = new ArrayList<>();
        /*
        for (Method method: methods) {
            if (method.isAnnotationPresent(annotation)) {
                methodsList.add(method);
            }
        }
        */
        Stream.of(methods).filter(x -> x.isAnnotationPresent(annotation))
                          .forEach(x -> methodsList.add(x));

        return methodsList;
    }

    /**
     * Private methods, which launch the chosen tests and return Map with
     * statistics of successful, failed and overall tests
     * @param testClass
     * @param beforeMethods
     * @param testMethods
     * @param afterMethods
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    private static Map<String, Integer> startTestAndGetStatistics(Class<?> testClass,
                                                                  List<Method> beforeMethods,
                                                                  List<Method> testMethods,
                                                                  List<Method> afterMethods)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        int successCounter = 0;
        int failsCounter = 0;

        for (Method testMethod: testMethods) {
            final Object testInstance = testClass.newInstance();
            //beforeMethods.forEach(beforeMethod -> beforeMethod.invoke(testInstance));
            for (Method beforeMethod: beforeMethods) {
                beforeMethod.invoke(testInstance);
            }

            try {
                testMethod.invoke(testInstance);
                ++successCounter;
            } catch (Exception e) {
                ++failsCounter;
            } finally {
                for (Method afterMethod: afterMethods) {
                    afterMethod.invoke(testInstance);
                }
            }
        }

        return Stream.of(new Object[][] {
                {"Overall tests: ", successCounter + failsCounter},
                {"Successful tests: ", successCounter},
                {"Failed tests: ", failsCounter}
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));
    }

}
