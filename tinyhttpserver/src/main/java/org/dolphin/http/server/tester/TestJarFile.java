package org.dolphin.http.server.tester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by hanyanan on 2015/11/24.
 */
public class TestJarFile {
    public static void readJARList(String fileName) throws IOException {// 显示JAR文件内容列表
        JarFile jarFile = new JarFile(fileName); // 创建JAR文件对象
        Enumeration en = jarFile.entries(); // 枚举获得JAR文件内的实体,即相对路径
        System.out.println("文件名\t文件大小\t压缩后的大小");
        while (en.hasMoreElements()) { // 遍历显示JAR文件中的内容信息
            process(en.nextElement()); // 调用方法显示内容
        }
    }

    private static void process(Object obj) {// 显示对象信息
        JarEntry entry = (JarEntry) obj;// 对象转化成Jar对象
        String name = entry.getName();// 文件名称
        long size = entry.getSize();// 文件大小
        long compressedSize = entry.getCompressedSize();// 压缩后的大小
        System.out.println(name + "\t" + size + "\t" + compressedSize);
    }

    public static void readJARFile(String jarFileName, String fileName)
            throws IOException {// 读取JAR文件中的单个文件信息
        JarFile jarFile = new JarFile(jarFileName);// 根据传入JAR文件创建JAR文件对象
        JarEntry entry = jarFile.getJarEntry(fileName);// 获得JAR文件中的单个文件的JAR实体
        InputStream input = jarFile.getInputStream(entry);// 根据实体创建输入流
        readFile(input);// 调用方法获得文件信息
        jarFile.close();// 关闭JAR文件对象流
    }

    public static void readFile(InputStream input) throws IOException {// 读出JAR文件中单个文件信息
        InputStreamReader in = new InputStreamReader(input);// 创建输入读流
        BufferedReader reader = new BufferedReader(in);// 创建缓冲读流
        String line;
        while ((line = reader.readLine()) != null) {// 循环显示文件内容
            System.out.println(line);
        }
        reader.close();// 关闭缓冲读流
    }

    public static void main(String args[]) throws IOException {// java程序主入口处
        String jarFileName = "D:\\6.1\\BaiNuoApp\\build\\intermediates\\classes-proguard\\common\\release\\classes.jar";;// 获得键盘输入的值
        readJARList(jarFileName);// 调用方法显示JAR文件中的文件信息
//        System.out.println("2.查看该JAR文件中的哪个文件信息?");
        String fileName = "com/crashlytics/android/c/a/a/g.class";// 键盘输入值

        readJARFile(jarFileName, fileName);// 获得键盘输入的值
    }
}
