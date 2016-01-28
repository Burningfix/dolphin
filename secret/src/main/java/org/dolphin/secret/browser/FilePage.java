package org.dolphin.secret.browser;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.secret.core.FileInfo;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yananh on 2016/1/23.
 */
public class FilePage extends Fragment implements BrowserManager.FileChangeListener {
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
        }
    };
}
