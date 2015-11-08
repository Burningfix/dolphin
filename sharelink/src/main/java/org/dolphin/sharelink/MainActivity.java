package org.dolphin.sharelink;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.dolphin.http.HttpRequest;
import org.dolphin.http.server.FileBean;
import org.dolphin.http.server.Main;
import org.dolphin.http.server.QueryFilesRequestHandler;
import org.dolphin.http.server.sniffer.SnifferBean;
import org.dolphin.job.Job;
import org.dolphin.job.JobErrorHandler;
import org.dolphin.job.Jobs;
import org.dolphin.job.Observer;
import org.dolphin.job.Operator;
import org.dolphin.job.http.HttpJobs;
import org.dolphin.job.internal.HttpErrorHandler;
import org.dolphin.job.operator.BytesToStringOperator;
import org.dolphin.job.operator.HttpPerformOperator;
import org.dolphin.job.operator.HttpResponseToBytes;
import org.dolphin.job.operator.PrintLogOperator;
import org.dolphin.job.operator.StringToGson;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.job.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listView;
    private TextView tv;
    FileAdapter fileAdapter;
    private SharedDiskManager sharedDiskManager;
    private SharedDisk currSharedDisk = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.listView = (ListView) findViewById(R.id.list_view);
        tv = (TextView) findViewById(R.id.empty_tip);

        listView.setEmptyView(tv);
        fileAdapter = new FileAdapter(null);
        listView.setAdapter(fileAdapter);
        fileAdapter.notifyDataSetChanged();

        sharedDiskManager = SharedDiskManager.instance();
        sharedDiskManager.addObserver(sharedDiskChangedObserver);
        sharedDiskManager.start();
    }

    private Observer<List<SharedDisk>, List<SharedDisk>> sharedDiskChangedObserver = new Observer.SimpleObserver<List<SharedDisk>, List<SharedDisk>>() {
        @Override
        public void onNext(Job job, List<SharedDisk> next) {
            if (null == currSharedDisk && next.size() > 0) {
                currSharedDisk = next.get(0);
                visite(currSharedDisk);
            }
        }
    };

    private void visite(SharedDisk sharedDisk) {
        Log.d(TAG, "Visit disk " + sharedDisk.toString());
        sharedDisk.queryFileList("video", fileListQueryObservre);
    }

    private Observer<Void, List<FileBean>> fileListQueryObservre = new Observer.SimpleObserver<Void, List<FileBean>>() {
        @Override
        public void onCompleted(Job job, List<FileBean> result) {
            fileAdapter.setFiles(result.toArray(new FileBean[0]));
            fileAdapter.notifyDataSetChanged();
        }
    };

//    private void startSniffer() {
//        Job snifferJob = new Job("");
//        snifferJob.until(new Operator<Object, SnifferBean>() {
//            @Override
//            public SnifferBean operate(Object input) throws Throwable {
//                DatagramSocket socket;
//                DatagramPacket packet;
//                byte[] data = new byte[64*1024];
//
//                socket = new DatagramSocket();
//
//                socket.setBroadcast(true); //有没有没啥不同
//                //send端指定接受端的端口，自己的端口是随机的
//                byte[] sendData = ("" + System.nanoTime()).getBytes();
//                packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), Main.PORT);
//                socket.send(packet);
//
//                packet = new DatagramPacket(data, data.length);
//                socket.receive(packet);
//
//                String s = new String(packet.getData(), 0, packet.getLength());
//                Log.d(TAG, "Client " + packet.getAddress() + " at port " + packet.getPort() + " says " + s);
//                Gson gson = new Gson();
//
//                SnifferBean res =  gson.fromJson(s, SnifferBean.class);
//                res.ip = packet.getAddress().toString();
//                return res;
//            }
//        }, true).observer(new Observer<SnifferBean, Object>() {
//
//            @Override
//            public void onNext(Job job, SnifferBean next) {
//                Log.d(TAG, "next SnifferBean " + next.tcpListenPort);
//                update("http:/" + next.ip+":"+next.tcpListenPort+""+Main.QUERY_FILE_LIST_PATH);
//            }
//
//            @Override
//            public void onCompleted(Job job, Object result) {
//
//            }
//
//            @Override
//            public void onFailed(Job job, Throwable error) {
//
//            }
//
//            @Override
//            public void onCancellation(Job job) {
//
//            }
//        }).handleError(new JobErrorHandler() {
//            @Override
//            public Job handleError(Job job, Throwable throwable) throws Throwable {
//                return job;
//            }
//        }).workPeriodic(100, 20000, TimeUnit.MILLISECONDS);
//    }
//
//
//    private void update(String url) {
//        Log.d(TAG, "Load Url "+url);
//        final HttpRequest request = HttpJobs.create(url);
//        Job job = new Job(request);
//        job.append(new HttpPerformOperator());
//        job.append(new HttpResponseToBytes());
//        job.append(new BytesToStringOperator());
//        job.append(new Operator<String, QueryFilesRequestHandler.FileTreeBean>(){
//            @Override
//            public QueryFilesRequestHandler.FileTreeBean operate(String input) throws Throwable {
//                Log.d(TAG, "Get Response "+input);
//                Gson gson = new Gson();
//                return gson.fromJson(input, QueryFilesRequestHandler.FileTreeBean.class);
//            }
//        }).workOn(Schedulers.computation())
//        .observer(new Observer<Void, QueryFilesRequestHandler.FileTreeBean>() {
//            @Override
//            public void onNext(Job job, Void next) {
//
//            }
//
//            @Override
//            public void onCompleted(Job job, final QueryFilesRequestHandler.FileTreeBean result) {
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        fileAdapter.setFiles(result.files);
//                        fileAdapter.notifyDataSetChanged();
//                    }
//                });
//            }
//
//            @Override
//            public void onFailed(Job job, Throwable error) {
//
//            }
//
//            @Override
//            public void onCancellation(Job job) {
//
//            }
//        })
//        .work();
//    }

    private class FileAdapter extends BaseAdapter {
        private FileBean[] files;

        private FileAdapter(FileBean[] files) {
            this.files = files;
        }

        public void setFiles(FileBean[] files) {
            this.files = files;
        }

        @Override
        public int getCount() {
            return null != files ? files.length : 0;
        }

        @Override
        public Object getItem(int i) {
            return files[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv = new TextView(MainActivity.this);
            tv.setText("" + files[i].name);
            tv.setTextSize(20);
            return tv;
        }
    }
}
