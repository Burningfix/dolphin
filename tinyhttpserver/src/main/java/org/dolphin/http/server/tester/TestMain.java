package org.dolphin.http.server.tester;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyanan on 2015/11/24.
 */
public class TestMain {
    public static class Printer {
        private List<String> stringList = new ArrayList<String>();

        public void init() {
            for (int i = 0; i < 1000; ++i) {
                stringList.add("" + i);
            }
        }

        public void print() {
            for (String s : stringList) {
                System.err.println("" + Thread.currentThread().getId() + "\t" + s);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] argv) {
        final Printer printer = new Printer();
        printer.init();

        new Thread() {
            public void run() {
                try {
                    Field jlrField = findField(printer, "stringList");
                    List<String> stringList = new ArrayList<String>();
                    for (int i = 0; i < 10; ++i) {
                        stringList.add("" + System.currentTimeMillis());
                    }
                    jlrField.set(printer, stringList);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                System.err.println("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                printer.print();
            }
        }.start();
        printer.print();
    }


    private static Field findField(Object instance, String name) throws NoSuchFieldException {
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
}
