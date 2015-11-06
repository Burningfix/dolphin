package org.dolphin.http.server.sniffer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by hanyanan on 2015/11/6.
 */
public class BroadCastTest {

    public static void main(String args[]) throws Exception {
        new Thread(){
            public void run(){
                try {
                    receiveBroadcast();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        Thread.sleep(100);
        sendBroadcast();
//        receiveBroadcast();
    }

    public static void sendBroadcast() throws Exception {
        DatagramSocket socket;
        DatagramPacket packet;
        byte[] data ;

        socket = new DatagramSocket();

        for (int i = 0; i < 50; i++) {
            socket.setBroadcast(true); //有没有没啥不同
            //send端指定接受端的端口，自己的端口是随机的
            data = (""+System.nanoTime()).getBytes();
            packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 8300);
            socket.send(packet);
            socket.receive(packet);
            String s = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Client "+packet.getAddress() + " at port " + packet.getPort() + " says " + s);
            Thread.sleep(1000);

        }
    }

    public static void receiveBroadcast() throws Exception {
        byte[] buffer = new byte[65507];
        DatagramSocket server = new DatagramSocket(8300);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        for (; ; ) {
            server.receive(packet);
            String s = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Server "+packet.getAddress() + " at port " + packet.getPort() + " says " + s);
            byte[] res = "ewfwefwefweff".getBytes();
            packet = new DatagramPacket(res, res.length, packet.getAddress(), packet.getPort());
            server.send(packet);
        }
    }

}
