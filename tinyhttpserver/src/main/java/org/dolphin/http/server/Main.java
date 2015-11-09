package org.dolphin.http.server;


import org.dolphin.http.server.HttpGetServer;
import org.dolphin.http.server.QueryFilesRequestHandler;
import org.dolphin.http.server.sniffer.SnifferGuard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;

/**
 * Created by hanyanan on 2015/11/3.
 */
public class Main {
    public static final String QUERY_FILE_LIST_PATH = "/query";
    public static final String QUERY_FILE_PARAM_KEY = "file";
    public static final String QUERY_FILE_PARAM_TYPE_KEY = "type"; // audio, video, photo, all
    public static final String REQUEST_FILE_PATH = "/get";
    public static final int PORT = 18592;
    public static void main(String[] argv) {
        HttpGetServer getServer = new HttpGetServer(PORT);
        QueryFilesRequestHandler handler = new QueryFilesRequestHandler("D:\\movie");
        getServer.registerRequestHandler(QUERY_FILE_LIST_PATH, handler);
        getServer.registerRequestHandler(REQUEST_FILE_PATH, handler);
        getServer.start();

        SnifferGuard snifferGuard = new SnifferGuard(PORT, PORT);
        snifferGuard.start();

//        try {
//            Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();
//            while(e.hasMoreElements()){
//                NetworkInterface networkInterface = e.nextElement();
//                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
//                while (inetAddresses.hasMoreElements()) {
//                    InetAddress inetAddress = inetAddresses.nextElement();
//                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
//                        String ip = inetAddress.getHostAddress();
//                        if(inetAddress.isLoopbackAddress()) {
//                            System.out.println(ip+ " is isLoopbackAddress, useless!" );
//                        }else {
//                            System.out.println(inetAddress. +"\t"+ip+ " is isReachable " + inetAddress.isReachable(500));
//                        }
//                    }
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try {
//            Process pro = Runtime.getRuntime()
//                    .exec("cmd /c ipconfig /all");
//            InputStreamReader isr = new InputStreamReader(pro.getInputStream());
//            BufferedReader br = new BufferedReader(isr);
//            String str = br.readLine();
//            while(str!=null){
//                System.out.println(new String(str.trim().getBytes(), "gb2312"));
//                str = br.readLine();
//            }
//            br.close();
//            isr.close();
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

//        try {
//
//
//            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
//            while (e.hasMoreElements()) {
//                NetworkInterface networkInterface = e.nextElement();
//
//                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
//                    InetAddress inetAddress = address.getAddress();
//                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
//                        String ip = inetAddress.getHostAddress();
//                        if (inetAddress.isLoopbackAddress()) {
//                            System.out.println(ip + " is isLoopbackAddress, useless!");
//                        } else {
//                            System.out.println(address.getNetworkPrefixLength() + "\t" + ip);
//                        }
//                    }
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}
