package org.dolphin.http.server;


/**
 * Created by hanyanan on 2015/11/3.
 */
public class Main {
    public static void main(String []argv) {
        HttpGetServer getServer = new HttpGetServer(8877);
        getServer.start();
    }
}
