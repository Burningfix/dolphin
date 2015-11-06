package org.dolphin.sharelink;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.dolphin.http.server.sniffer.SnifferBean;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.listView = (ListView) findViewById(R.id.list_view);
        tv = (TextView) findViewById(R.id.empty_tip);

        listView.setEmptyView(tv);
        listView.setAdapter(new FileAdapter());
        startSniffer();
    }

    private void startSniffer(){
        Job snifferJob = new Job("");
        snifferJob.until(new Operator<Object, SnifferBean>() {
            @Override
            public SnifferBean operate(Object input) throws Throwable {
                DatagramSocket socket;
                DatagramPacket packet;
                byte[] data ;

                socket = new DatagramSocket();

                    socket.setBroadcast(true); //有没有没啥不同
                    //send端指定接受端的端口，自己的端口是随机的
                    data = (""+System.nanoTime()).getBytes();
                    packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 8300);
                    socket.send(packet);
                    socket.receive(packet);
                    String s = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Client "+packet.getAddress() + " at port " + packet.getPort() + " says " + s);

                return null;
            }
        })
    }

    private class FileAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 100;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv = new TextView(MainActivity.this);
            tv.setText(""+i);
            tv.setTextSize(20);
            return tv;
        }
    }
}
