package org.dolphin.secret.browser;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.lib.IOUtil;
import org.dolphin.secret.core.FileDecodeOperator;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secret.core.ReadableFileInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yananh on 2016/1/23.
 */
public class FilePage extends Fragment implements BrowserManager.FileChangeListener, AdapterView.OnItemClickListener {


    public enum State {
        Normal,
        Selectable,
    }
    private final List<FileInfo> fileList = new LinkedList<FileInfo>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        listView = new ListView(inflater.getContext());
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
        this.fileList.addAll(BrowserManager.getInstance().getImageFileList());
        notifyStateChange();
        BrowserManager.getInstance().addImageFileChangeListener(this);
        return listView;
    }

    private State state = State.Normal;
    private ListView listView;
    public void setState(State state) {
        this.state = state;
        notifyStateChange();
    }

    public void notifyStateChange() {
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFileList(List<FileInfo> files) {
        if(null != files) {
            fileList.addAll(files);
        }
        notifyStateChange();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo item = (FileInfo) listAdapter.getItem(position);
        FileDecodeOperator fileDecodeOperator = new FileDecodeOperator();
        File file = new File(BrowserManager.sRootDir, item.proguardFileName);
        try {
            File outFile = new File(BrowserManager.sRootDir, "out");
            if(!outFile.exists()) outFile.createNewFile();
            ReadableFileInputStream inputStream = new ReadableFileInputStream(file, item);
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            IOUtil.copy(inputStream, fileOutputStream);
            IOUtil.safeClose(inputStream);
            IOUtil.safeClose(fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private BaseAdapter listAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return fileList.size();
        }

        @Override
        public Object getItem(int position) {
            return fileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ThumbnailImageVIew imageVIew = new ThumbnailImageVIew(FilePage.this.getActivity());
            FileInfo item = (FileInfo) getItem(position);
            imageVIew.setFile(new File(BrowserManager.sRootDir, item.proguardFileName).getPath(), item);
            return imageVIew;
//            TextView tv = new TextView(FilePage.this.getActivity());
//            FileInfo item = (FileInfo) getItem(position);
//            tv.setText(item.originalFileName);
//            return tv;
        }
    };
}
