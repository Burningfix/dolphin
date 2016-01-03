package org.dolphin.sharelink;

import android.os.SystemClock;

import com.google.gson.Gson;

import org.dolphin.http.server.Main;
import org.dolphin.http.server.sniffer.SnifferBean;
import org.dolphin.job.Job;
import org.dolphin.job.Observer;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.job.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yananh on 2015/11/7.
 */
public class SharedDiskManager {
    private static final String TAG = "ShareLink";

    private static SharedDiskManager sInstance = null;

    public synchronized static SharedDiskManager instance() {
        if (null == sInstance) {
            sInstance = new SharedDiskManager();
        }

        return sInstance;
    }

    private final List<Observer<List<SharedDisk>, List<SharedDisk>>> observers = new LinkedList<Observer<List<SharedDisk>, List<SharedDisk>>>();
    /**
     * ip到SharedDisk和更新时间的mapping
     */
    private final LinkedHashMap<String, TwoTuple<SharedDisk, Long>> ipToSharedDiskMapping = new LinkedHashMap<String, TwoTuple<SharedDisk, Long>>();

    private final long sOutOfWorkTimeDivide = 5000;// 5s
    /**
     *
     * */
    private Job sendQueryJob;
    private Job listenNodeJob;

    private SharedDiskManager() {

    }

    public synchronized void start() {
        sendQueryRequest();
        startServerSniffer();
    }

    public synchronized void stop() {
        if (null != sendQueryJob) {
            sendQueryJob.abort();
        }
        sendQueryJob = null;

        if (null != listenNodeJob) {
            listenNodeJob.abort();
        }
        listenNodeJob = null;
    }

    private synchronized void sendQueryRequest() {
        if (null != sendQueryJob) {
            sendQueryJob.abort();
        }
        String desc = "sendQueryRequest";
        sendQueryJob = new Job(desc)
                .then(new Operator() { // 广播发送客户端上线请求
                    @Override
                    public Object operate(Object input) throws Throwable {
                        // 广播发送监听指令
                        DatagramSocket socket;
                        DatagramPacket packet;

                        socket = new DatagramSocket();

                        socket.setBroadcast(true); //有没有没啥不同
                        //send端指定接受端的端口，自己的端口是随机的
                        byte[] sendData = ("" + System.nanoTime()).getBytes();
                        packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), Main.PORT);
                        socket.send(packet);

                        return input;
                    }
                })
                .workOn(Schedulers.computation())
                .observerOn(AndroidMainThreadScheduler.INSTANCE)
                .observer(new Observer.SimpleObserver<Void, Object>() {
                    @Override
                    public void onCompleted(Job job, Object result) {
                        checkValid();
                    }
                })
                .workPeriodic(0, 3000, TimeUnit.MILLISECONDS); // 没3秒发送请求一次
    }


    private synchronized void startServerSniffer() {
        if (null != listenNodeJob) {
            listenNodeJob.abort();
        }
        final Gson gson = new Gson();
        String desc = "startServerSniffer";
        listenNodeJob = new Job(desc)
                .then(new Operator() {
                    @Override
                    public Object operate(Object input) throws Throwable {
                        ipToSharedDiskMapping.clear();
                        return input;
                    }
                })
                .then(new Operator<Object, DatagramSocket>() {
                    @Override
                    public DatagramSocket operate(Object input) throws Throwable {
                        DatagramSocket server = new DatagramSocket(Main.PORT);
                        return server;
                    }
                })
                .until(new Operator<DatagramSocket, SnifferBean>() {
                    @Override
                    public SnifferBean operate(DatagramSocket socket) throws Throwable {
                        DatagramPacket packet;
                        byte[] data = new byte[64 * 1024];
                        packet = new DatagramPacket(data, data.length);
                        socket.receive(packet);
                        String s = new String(packet.getData(), 0, packet.getLength());
                        Log.d(TAG, "Client Receive " + packet.getAddress() + " at port " + packet.getPort() + " says " + s);
                        try {
                            SnifferBean res = gson.fromJson(s, SnifferBean.class);
                            res.ip = packet.getAddress().toString().replace("/","");
                            return res;
                        } catch (Throwable throwable) {
                            SnifferBean bean = new SnifferBean();
                            return bean;
                        }
                    }
                }, true)
                .observerOn(Schedulers.computation())
                .observerOn(AndroidMainThreadScheduler.INSTANCE)
                .observer(new Observer<SnifferBean, Object>() {
                    @Override
                    public void onNext(Job job, SnifferBean next) {
                        Log.d(TAG, "next SnifferBean " + next.tcpListenPort);
                        onServerFound(next);
                    }

                    @Override
                    public void onCompleted(Job job, Object result) {

                    }

                    @Override
                    public void onFailed(Job job, Throwable error) {

                    }

                    @Override
                    public void onCancellation(Job job) {

                    }
                }).work();
    }

    private synchronized void onServerFound(SnifferBean server) {
        String ip = server.ip;
        long time = SystemClock.elapsedRealtime();
        boolean changed = false;
        if (ipToSharedDiskMapping.containsValue(ip)) {
            TwoTuple<SharedDisk, Long> entry = ipToSharedDiskMapping.get(ip);
            if (server.statue == 1) { // offline
                Log.d(TAG, "remove a server " + server.toString());
                ipToSharedDiskMapping.remove(ip);
                changed = true;
            } else { // online
                // no change
                Log.d(TAG, "Found a exits server " + server.toString());
            }
        } else {
            if (server.statue == 1) { // offline
                Log.d(TAG, "Found a not used server " + server.toString());
            } else {
                Log.d(TAG, "Found a new server " + server.toString());
                SharedDisk newSharedDisk = new SharedDisk(ip, String.valueOf(server.tcpListenPort));
                changed = true;
                ipToSharedDiskMapping.put(ip, new TwoTuple<SharedDisk, Long>(newSharedDisk, time));
            }
        }

        if (changed) {
            Collection<TwoTuple<SharedDisk, Long>> servers = ipToSharedDiskMapping.values();
            if (null == servers || servers.isEmpty()) {
                notifySharedDisks(new LinkedList<SharedDisk>());
            } else {
                List<SharedDisk> sharedDisks = new LinkedList<SharedDisk>();
                for (TwoTuple<SharedDisk, Long> entry : servers) {
                    sharedDisks.add(entry.value1);
                }
                notifySharedDisks(sharedDisks);
            }
        }
    }

    private synchronized void checkValid() {
        long time = SystemClock.elapsedRealtime();
        Map<String, SharedDisk> outOfWorks = new HashMap<String, SharedDisk>();
        for (Map.Entry<String, TwoTuple<SharedDisk, Long>> entry : ipToSharedDiskMapping.entrySet()) {
            if (time - entry.getValue().value2 > sOutOfWorkTimeDivide) {
                // out of work
                outOfWorks.put(entry.getKey(), entry.getValue().value1);
            }
        }

        if (outOfWorks.isEmpty()) return;
        for (Map.Entry<String, SharedDisk> e : outOfWorks.entrySet()) {
            ipToSharedDiskMapping.remove(e.getKey());
        }


        Collection<TwoTuple<SharedDisk, Long>> servers = ipToSharedDiskMapping.values();
        if (null == servers || servers.isEmpty()) {
            notifySharedDisks(new LinkedList<SharedDisk>());
        } else {
            List<SharedDisk> sharedDisks = new LinkedList<SharedDisk>();
            for (TwoTuple<SharedDisk, Long> entry : servers) {
                sharedDisks.add(entry.value1);
            }
            notifySharedDisks(sharedDisks);
        }
    }

    public void addObserver(Observer<List<SharedDisk>, List<SharedDisk>> observer) {
        synchronized (this) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    public void removeObserver(Observer<List<SharedDisk>, List<SharedDisk>> observer) {
        synchronized (this) {
            observers.remove(observer);
        }
    }

    private void notifySharedDisks(List<SharedDisk> sharedDisks) {
        List<Observer<List<SharedDisk>, List<SharedDisk>>> observers = new LinkedList<Observer<List<SharedDisk>, List<SharedDisk>>>();
        List<SharedDisk> sharedDiskList = new LinkedList<SharedDisk>();
        synchronized (this) {
            observers.addAll(this.observers);
            sharedDiskList.addAll(sharedDisks);
        }
        for (Observer<List<SharedDisk>, List<SharedDisk>> observer : observers) {
            observer.onNext(null, sharedDiskList);
        }
    }
}
