package org.dolphin.secret.browser;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.dolphin.lib.DateUtils;
import org.dolphin.lib.FileInfoUtil;
import org.dolphin.lib.IOUtil;
import org.dolphin.secret.R;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secret.core.ReadableFileInputStream;
import org.dolphin.secret.picker.FileRequestProvider;

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
    protected final int CATCH_PHOTO_REQUEST_CODE = 1234;
    protected final int VIDEO_REQUEST_CODE = 1235;
    protected final int AUDIO_REQUEST_CODE = 1236;
    protected final int IMPORT_PHOTO_REQUEST_CODE = 1344;


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
        setHasOptionsMenu(true);
        Log.d("DDD", "FilePage onCreateView");
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
        if (null != files) {
            fileList.clear();
            fileList.addAll(files);
        }
        notifyStateChange();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo item = (FileInfo) listAdapter.getItem(position);
        onItemClicked(item);
        File file = new File(BrowserManager.sRootDir, item.proguardFileName);
        try {
            File outFile = new File(BrowserManager.sRootDir, "out");
            if (!outFile.exists()) outFile.createNewFile();
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

    protected void onItemClicked(FileInfo fileInfo) {
        // TODO
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
            FileInfo item = (FileInfo) getItem(position);
            View root = View.inflate(FilePage.this.getActivity(), R.layout.file_item, null);
            ThumbnailImageView imageVIew = (ThumbnailImageView) root.findViewById(R.id.thumbnail);
            TextView nameView = (TextView) root.findViewById(R.id.name);
            TextView size = (TextView) root.findViewById(R.id.size);
            TextView duration = (TextView) root.findViewById(R.id.duration);
            TextView encodeTime = (TextView) root.findViewById(R.id.encode_time);
            imageVIew.setFile(new File(BrowserManager.sRootDir, item.proguardFileName).getPath(), item);
            nameView.setText(item.originalFileName);
            size.setText(FileInfoUtil.formatSize(item.originalFileLength));
//            duration.setText("12:22:22");
            encodeTime.setText(DateUtils.formatDate(item.encodeTime));
            return root;
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void importFileEntryList(List<FileRequestProvider.FileEntry> selectedFileList) {
        if (null == selectedFileList || selectedFileList.isEmpty()) {
            return;
        }
        final int count = selectedFileList.size();
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Progress");
        progressDialog.setMessage("Progress");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMax(count);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        BrowserManager.getInstance().importFiles(selectedFileList, new ImportCallback() {
            private final int totalCount = count;
            private int currFinished = 0;

            @Override
            public void onImportSucced(String originalPath, FileInfo obscurePath) {
                Toast.makeText(getActivity(), "import file " + originalPath + " success", Toast.LENGTH_SHORT).show();
                Log.d("import", "onImportSucced " + originalPath + " To " + obscurePath);
                BrowserManager.getInstance().onFileFound(obscurePath);
                progressDialog.setMessage(originalPath);
                ++currFinished;
                progressDialog.incrementProgressBy(1);
                if (currFinished == totalCount) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onImportFailed(String originalPath, Throwable error) {
                Toast.makeText(getActivity(), "import file " + originalPath + " failed", Toast.LENGTH_SHORT).show();
                Log.d("import", "onImportFailed " + originalPath + " To " + error);
                progressDialog.setMessage(originalPath);
                ++currFinished;
                progressDialog.incrementProgressBy(1);
                if (currFinished == totalCount) {
                    progressDialog.dismiss();
                }
            }
        });
    }
}
