package org.dolphin.http.server.tester;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * Created by hanyanan on 2015/11/24.
 */
public class UnZipJarFileTester {

    public static void readJARList(String fileName, List<String> fileList) throws IOException {// 显示JAR文件内容列表
        JarFile jarFile = new JarFile(fileName); // 创建JAR文件对象
        Enumeration en = jarFile.entries(); // 枚举获得JAR文件内的实体,即相对路径
        System.out.println("文件名\t文件大小\t压缩后的大小");
        while (en.hasMoreElements()) { // 遍历显示JAR文件中的内容信息
            process(en.nextElement(), fileList); // 调用方法显示内容
        }
    }

    private static void process(Object obj, List<String> fileList) {// 显示对象信息
        JarEntry entry = (JarEntry) obj;// 对象转化成Jar对象
        String name = entry.getName();// 文件名称
        long size = entry.getSize();// 文件大小
        long compressedSize = entry.getCompressedSize();// 压缩后的大小
        System.out.println(name + "\t" + size + "\t" + compressedSize);
        fileList.add(name);
    }

    public static boolean match(String fileName, List<String> extra){
        fileName = fileName.replace("/", ".");
        for(String e : extra){
            if(fileName.startsWith(e)) return true;
        }
        return false;
    }

//    private static void zipFileEntry(String base, File inFile, JarOutputStream ops) throws IOException{
//        if(inFile.isDirectory()){
//            File[] files = inFile.listFiles();
//            ops.putNextEntry(new ZipEntry(base + "/"));
//            base = base.length() == 0 ? "" : base + "/";
//            for(File file:files){
//                zipFileEntry(base+file.getName(), file, ops);
//            }
//        }else{
//            ops.putNextEntry(new JarEntry(base));
//            InputStream ips = new FileInputStream(inFile);
//            int len = 0;
//            byte[] buffer = new byte[1024];
//            while((len = ips.read(buffer)) != -1){
//                ops.write(buffer,0,len);
//                ops.flush();
//            }
//            ips.close();
//        }
//    }

    public static void copy(File jar, File outJar1File, File extraOutJarFile, List<String> extraFiles) throws IOException{
        JarFile jarFile = new JarFile(jar); // 创建JAR文件对象
        JarOutputStream outJar1os = new JarOutputStream(new FileOutputStream(outJar1File));
        JarOutputStream extraOutJar1os = new JarOutputStream(new FileOutputStream(extraOutJarFile));
        Enumeration en = jarFile.entries(); // 枚举获得JAR文件内的实体,即相对路径
        while (en.hasMoreElements()) { // 遍历显示JAR文件中的内容信息
            JarEntry entry = (JarEntry) en.nextElement();// 对象转化成Jar对象
            String name = entry.getName();// 文件名称
            long size = entry.getSize();// 文件大小
            long compressedSize = entry.getCompressedSize();// 压缩后的大小
            System.out.println(name + "\t" + size + "\t" + compressedSize);
            JarOutputStream ops = outJar1os;
            InputStream ips = jarFile.getInputStream(entry);
            if(match(name, extraFiles)){
                System.out.println(name + " Match!");
                ops = extraOutJar1os;
            }
            ops.putNextEntry(new JarEntry(name));
            int len = 0;
            byte[] buffer = new byte[1024];
            while((len = ips.read(buffer)) != -1){
                ops.write(buffer,0,len);
                ops.flush();
            }
        }
        outJar1os.close();
        extraOutJar1os.close();
    }

    public static void main(String []argv) {
        String jarFileName = "D:\\6.1\\BaiNuoApp\\build\\intermediates\\classes-proguard\\common\\release\\classes.jar";// 获得键盘输入的值
        final List<String> extra = new ArrayList<String>();
        extra.add("com.baidu.pano");
        extra.add("com.baidu.panosdk");
        extra.add("com.baidu.cloudsdk");
        try {
            File jarFile = new File(jarFileName);
            File class1JarFile = new File(jarFile.getParentFile(), "classes1.jar");
            File extraJarFile = new File(jarFile.getParentFile(), "classes2.jar");
            copy(jarFile, class1JarFile, extraJarFile, extra);

//            unZipFile(jarFileName, outDir);
//            zipFile(outDir, jarFileName1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void backupJarFile(String jarPath) throws IOException{
        dealJarFile(jarPath+"/ihpones.jar", jarPath+"/Oldiphones/ihpones.jar");
    }

    public static void recoverJarFile(String jarPath) throws IOException{
        dealJarFile(jarPath+"/Oldiphones/ihpones.jar", jarPath+"/ihpones.jar");
        deleteFile(jarPath + "/Oldiphones");
    }

    private static void dealJarFile(String filePath, String outputPath) throws IOException{
        File jarFile = new File(filePath);
        if(jarFile.exists() && jarFile!=null){
            makeSupDir(outputPath);
            writeFile(jarFile, new File(outputPath));
        }
    }

    private static void makeSupDir(String outFileName) {
        Pattern p = Pattern.compile("[/\\" + File.separator + "]");
        Matcher m = p.matcher(outFileName);
        while (m.find()) {
            int index = m.start();
            String subDir = outFileName.substring(0, index);
            File subDirFile = new File(subDir);
            if (!subDirFile.exists())
                subDirFile.mkdir();
        }
    }

    private static void zipFile(String jarDir, String jarPath) throws IOException{
        JarOutputStream ops = new JarOutputStream(new FileOutputStream(jarPath));
        File inFile = new File(jarDir);
        zipFileEntry("", inFile, ops);
        ops.close();
    }

    private static void zipFileEntry(String base, File inFile, JarOutputStream ops) throws IOException{
        if(inFile.isDirectory()){
            File[] files = inFile.listFiles();
            ops.putNextEntry(new ZipEntry(base + "/"));
            base = base.length() == 0 ? "" : base + "/";
            for(File file:files){
                zipFileEntry(base+file.getName(), file, ops);
            }
        }else{
            ops.putNextEntry(new JarEntry(base));
            InputStream ips = new FileInputStream(inFile);
            int len = 0;
            byte[] buffer = new byte[1024];
            while((len = ips.read(buffer)) != -1){
                ops.write(buffer,0,len);
                ops.flush();
            }
            ips.close();
        }
    }

    private static void unZipFile(String jarPath, String outDir) throws IOException{
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> jarEntrys = jarFile.entries();
        while(jarEntrys.hasMoreElements()){
            JarEntry jarEntry = jarEntrys.nextElement();
            jarEntry.getName();
            String outFileName = outDir + jarEntry.getName();
//            if(outFileName.contains("com/") ){
//                outFileName = outFileName.replace("com/", "");
//            }
            File f = new File(outFileName);
            makeSupDir(outFileName);
            if(jarEntry.isDirectory()){
                continue;
            }
            writeFile(jarFile.getInputStream(jarEntry), f);
        }
    }

    private static void deleteFile(String jarPath) throws IOException {
        File delFile = delFile = new File(jarPath);
        if(delFile.exists() && delFile.isDirectory()){
            if(delFile.listFiles().length==0){
                delFile.delete();
            } else {
                for(File file:delFile.listFiles()){
                    if(file.isDirectory()){
                        deleteFile(file.getAbsolutePath());
                    }
                    file.delete();
                }
            }
        }
        if(delFile.exists() && delFile.isDirectory() && delFile.listFiles().length==0){
            delFile.delete();
        }
    }

    private static void writeFile(File inputFile, File outputFile) throws IOException{
        writeFile(new FileInputStream(inputFile), outputFile);
    }

    private static void writeFile(InputStream ips, File outputFile) throws IOException{
        OutputStream ops = new BufferedOutputStream(new FileOutputStream(outputFile));
        writeFile(ips, ops);
    }

    private static void writeFile(InputStream ips, OutputStream outputStream) throws IOException{
        OutputStream ops = outputStream;
        try{
            byte[] buffer = new byte[1024];
            int nBytes = 0;
            while ((nBytes = ips.read(buffer)) > 0){
                ops.write(buffer, 0, nBytes);
            }
        }catch (IOException ioe){
            throw ioe;
        } finally {
            try {
                if (null != ops){
                    ops.flush();
                    ops.close();
                }
            } catch (IOException ioe){
                throw ioe;
            } finally{
                if (null != ips){
                    ips.close();
                }
            }
        }
    }

//    public static void changeJarFile(String jarPath){
//        try {
//            backupJarFile(jarPath);
//            unZipFile(jarPath);
//            zipFile(jarPath);
//            deleteFile(jarPath+"/iphones");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
