package org.dolphin.http.server.sniffer;

import com.google.gson.Gson;

import org.dolphin.http.server.Main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by hanyanan on 2015/11/6.
 * <p/>
 * 用户通过局域网内广播某个具体的端口，来判断tcp端口是否可用
 */
public class SnifferGuard {
    public static int sMaxBuffSize = 64 * 1024;
    /**
     * udp 端口
     */
    private final int port;

    /**
     * 正在监听的tcp的端口
     */
    private final int tcpPort;

    private Thread thread;

    public SnifferGuard(final int udpPort, final int tcpPort) {
        this.port = udpPort;
        this.tcpPort = tcpPort;
    }

    public synchronized void start() {
        if (null != thread) {
            thread.interrupt();
        }

        thread = new Thread() {
            public void run() {
                try {
                    listen();
                } catch (IOException e) {
                    e.printStackTrace();
                    SnifferGuard.this.stop();
                }
            }
        };
        thread.start();
    }

    protected void listen() throws IOException {
        byte[] buffer = new byte[sMaxBuffSize];
        DatagramSocket server = new DatagramSocket(port);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        for (; ; ) {
            server.receive(packet);
            String s = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Server " + packet.getAddress() + " at port " + packet.getPort() + " says " + s);
            byte[] res = filter(packet.getData());
            packet = new DatagramPacket(res, res.length, packet.getAddress(), packet.getPort());
            server.send(packet);
        }
    }

    /**
     * 从客户端接收信息，并返回内容到客户端
     *
     * @param received 从client接受的内容, 最大不能超过64k
     * @return 返回给client的内容
     */
    protected byte[] filter(byte[] received) {
        if (null == received || received.length <= 0) {
            return null;
        }

        SnifferBean snifferBean = new SnifferBean();
        snifferBean.errorno = 0;
        snifferBean.msg = "success";
        snifferBean.tcpListenPort = this.tcpPort;

        Gson gson = new Gson();

        return gson.toJson(snifferBean).getBytes();
    }

    public synchronized void stop() {
        if (null != thread) {
            thread.interrupt();
        }
        thread = null;

        try {
            // 广播发送监听指令
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); //有没有没啥不同
            DatagramPacket packet;
            SnifferBean bean = new SnifferBean();
            bean.statue = 1;
            Gson gson = new Gson();
            byte[] sendData = gson.toJson(bean).getBytes();
            packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), Main.PORT);
            socket.send(packet);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
