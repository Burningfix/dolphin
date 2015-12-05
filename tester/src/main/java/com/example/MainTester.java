package com.example;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static java.lang.System.out;

public class MainTester {
    public static void main(String[] argv) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        File rootFile = new File("C:\\jobs");
        MyClassLoader classLoader1 = new MyClassLoader(rootFile, ClassLoader.getSystemClassLoader());
        MyClassLoader classLoader2 = new MyClassLoader(rootFile, ClassLoader.getSystemClassLoader());


        out.println("----------------------- Class Type Compare ------------------------");
        {
            Class customClazz1 = classLoader1.loadClass("com.example.TestBean");
            Class customClazz2 = classLoader2.loadClass("com.example.TestBean");
            //加载的都是是系统默认的appclassloader， 是同一个classloader， 所以结果是true
            out.println("customClazz1 == customClazz2?" + (customClazz1 == customClazz2)); // false

            // 加载的class是外部的class，classloader是不同的，所以结果是false
            Class extraClazz1 = classLoader1.loadClass("org.dolphin.job.Observer");
            Class extraClazz2 = classLoader2.loadClass("org.dolphin.job.Observer");
            out.println("extraClazz1 == extraClazz2?" + (extraClazz1 == extraClazz2)); // false

            // 等同于（extraClazz1）extraClazz2
            out.println("extraClazz1.isInstance(extraClazz2)?" + (extraClazz1.isInstance(extraClazz2))); // false
        }
        out.println("-------------------------------------------------------------------\n");


        out.println("------------Static: Two class, One Field, Same Value-------------");
        {
            Class customClazz1 = classLoader1.loadClass("org.dolphin.arch.TestStatic");
            Class customClazz2 = classLoader2.loadClass("org.dolphin.arch.TestStatic");
            Object obj1 = customClazz1.newInstance();
            Object obj2 = customClazz2.newInstance();
            Field indexField = findField(obj1, "index"); // 只有一个filed
            indexField.set(obj1, 111);
            indexField.set(obj2, 222);

            out.println("customClazz1 index = " + indexField.get(obj1)); // 222
            out.println("customClazz2 index = " + indexField.get(obj2)); // 222
        }
        out.println("-------------------------------------------------------------------\n");


        out.println("-----------Static: Two class, Two Field, Not-Same Value------------");
        {
            Class customClazz1 = classLoader1.loadClass("org.dolphin.arch.TestStatic");
            Class customClazz2 = classLoader2.loadClass("org.dolphin.arch.TestStatic");
            Object obj1 = customClazz1.newInstance();
            Object obj2 = customClazz2.newInstance();
            Field indexField1 = findField(obj1, "index"); // 两个不同的field
            Field indexField2 = findField(obj2, "index"); // 两个不同的field
            indexField1.set(obj1, 111);
            indexField2.set(obj2, 222);

            out.println("customClazz1 index = " + indexField1.get(obj1)); // 111
            out.println("customClazz2 index = " + indexField2.get(obj2)); // 222
        }
        out.println("-------------------------------------------------------------------\n");

    }

    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name     field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    public static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    /**
     * Locates a given method anywhere in the class inheritance hierarchy.
     *
     * @param instance       an object to search the method into.
     * @param name           method name
     * @param parameterTypes method parameter types
     * @return a method object
     * @throws NoSuchMethodException if the method cannot be located
     */
    public static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " +
                Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }
}
