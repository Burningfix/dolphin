package org.dolphin.secret.browser;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.dolphin.lib.util.DateUtils;
import org.dolphin.lib.util.FileInfoUtil;
import org.dolphin.lib.util.ValueUtil;
import org.dolphin.secret.R;
import org.dolphin.secret.core.ObscureFileInfo;
import org.dolphin.secret.picker.AndroidFileInfo;
import org.dolphin.secret.picker.AndroidFileProvider;
import org.dolphin.secret.util.DialogUtil;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by yananh on 2016/1/23.
 */
public abstract class FilePage extends Fragment implements BrowserManager.FileChangeListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    protected final int CATCH_PHOTO_REQUEST_CODE = 1234;
    protected final int CATCH_VIDEO_REQUEST_CODE = 1235;
    protected final int CATCH_AUDIO_REQUEST_CODE = 1236;
    protected final int IMPORT_PHOTO_REQUEST_CODE = 1344;
    protected final int IMPORT_VIDEO_REQUEST_CODE = 1345;
    protected final int IMPORT_AUDIO_REQUEST_CODE = 1349;
    protected final List<ObscureFileInfo> fileList = new LinkedList<ObscureFileInfo>();
    protected final Set<ObscureFileInfo> selected = new HashSet<ObscureFileInfo>();
    private State state = State.Normal;
    private ListView listView;
    private View editLayout;
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
            ObscureFileInfo item = (ObscureFileInfo) getItem(position);
            return crateItemView(item, convertView);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.file_browser_layout, null);
        listView = (ListView) root.findViewById(R.id.list_view);
        editLayout = root.findViewById(R.id.edit_layout);
        Drawable drawable = new ColorDrawable(0xFFEEEEEE);
        drawable.setBounds(0, 0, 1000, 1);
        listView.setDividerHeight(1);
        listView.setDivider(drawable);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        editLayout.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowserManager.getInstance().deleteFiles(selected);
                selected.clear();
                setState(State.Normal);
            }
        });
        editLayout.findViewById(R.id.recover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowserManager.getInstance().exportFiles(selected);
                selected.clear();
                setState(State.Normal);
            }
        });
        this.fileList.addAll(getFileList());
        notifyStateChange();
        addListener();
        setHasOptionsMenu(true);
        return root;
    }

    protected List<ObscureFileInfo> getFileList() {
        return BrowserManager.getInstance().getImageFileList();
    }

    protected void addListener() {
        BrowserManager.getInstance().addImageFileChangeListener(this);
    }

    public void notifyStateChange() {
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFileListChanged(List<ObscureFileInfo> files) {
        if (null != files) {
            fileList.clear();
            fileList.addAll(files);
        }
        notifyStateChange();
    }

    protected void setState(State state) {
        if (this.state == state) {
            return;
        }
        this.state = state;
        if (state == State.Normal) {
            this.selected.clear();
            editLayout.animate().cancel();
            editLayout.animate().setDuration(500).translationY(1000).start();
        } else {
            editLayout.animate().cancel();
            editLayout.animate().setDuration(500).translationY(0).start();
        }
        notifyStateChange();
        getActivity().invalidateOptionsMenu();
    }

    protected final boolean isNormalState() {
        return this.state == State.Normal;
    }

    protected final boolean isSelectableState() {
        return this.state == State.Selectable;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ObscureFileInfo item = (ObscureFileInfo) listAdapter.getItem(position);
        if (isSelectableState()) {
            if (selected.contains(item)) {
                selected.remove(item);
                view.setBackgroundResource(R.color.deep_select_color);
            } else {
                selected.add(item);
                view.setBackgroundResource(R.color.deep_deep_select_color);
            }
        } else {
            onItemClicked(item, position);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (isNormalState()) {
            this.selected.add(((ItemViewHolder) view.getTag()).fileInfo);
            setState(State.Selectable);
        }
        return true;
    }

    public boolean onBackPressed() {
        if (isNormalState()) {
            return false;
        }
        setState(State.Normal);
        return true;
    }


    protected void onItemClicked(ObscureFileInfo fileInfo, int position) {

    }

    protected View crateItemView(ObscureFileInfo item, View convertView) {
        View root = View.inflate(FilePage.this.getActivity(), R.layout.file_item, null);
        ItemViewHolder viewHolder = new ItemViewHolder();
        viewHolder.fileInfo = item;
        viewHolder.imageVIew = (ThumbnailImageView) root.findViewById(R.id.thumbnail);
        viewHolder.nameView = (TextView) root.findViewById(R.id.name);
        viewHolder.size = (TextView) root.findViewById(R.id.size);
        viewHolder.duration = (TextView) root.findViewById(R.id.duration);
        viewHolder.encodeTime = (TextView) root.findViewById(R.id.encode_time);
        viewHolder.imageVIew.setFile(new File(BrowserManager.sRootDir, item.obscuredFileName).getPath(), item);
        viewHolder.nameView.setText(item.originalFileName);
        viewHolder.size.setText(FileInfoUtil.formatSize(item.originalFileLength));
        viewHolder.encodeTime.setText(DateUtils.formatDate(item.encodeTime));
        if (this.state == State.Normal) {
            root.setBackgroundResource(R.drawable.list_item_view_backgroud_color);
        } else {
            if (this.selected.contains(item)) {
                root.setBackgroundResource(R.color.deep_deep_select_color);
            } else {
                root.setBackgroundResource(R.color.deep_select_color);
            }
        }
        root.setTag(viewHolder);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isNormalState()) {
            super.onCreateOptionsMenu(menu, inflater);
        } else {
            inflater.inflate(R.menu.menu_pick, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_select && isSelectableState()) { // select all
            selected.addAll(fileList);
            notifyStateChange();
            return true;
        } else if (id == R.id.menu_unselect && isSelectableState()) { // unselect all
            selected.clear();
            notifyStateChange();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public abstract String getLastCaptureFile();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((IMPORT_PHOTO_REQUEST_CODE == requestCode
                || IMPORT_VIDEO_REQUEST_CODE == requestCode
                || IMPORT_AUDIO_REQUEST_CODE == requestCode)
                && resultCode == Activity.RESULT_OK) {
            // 调用系统导入成功
            if (null == data) {
                return;
            }
            List<AndroidFileInfo> selectedFileList = data.getParcelableArrayListExtra("data");
            if (null != selectedFileList) {
                importAndroidFileList(selectedFileList);
                return;
            }
            Uri uri = data.getData();
            if (null == uri) {
                return;
            }
            importAndroidFileList(Arrays.<AndroidFileInfo>asList(AndroidFileProvider.requestFile(getActivity(), uri)));
            return;
        }

        if ((requestCode == CATCH_PHOTO_REQUEST_CODE
                || requestCode == CATCH_VIDEO_REQUEST_CODE
                || requestCode == CATCH_AUDIO_REQUEST_CODE)
                && resultCode == Activity.RESULT_OK) {
            String lastCreateFileName = getLastCaptureFile();
            if (!ValueUtil.isEmpty(lastCreateFileName)) {
                BrowserManager.getInstance().obscureFile(lastCreateFileName);
            }
        }
    }

    protected void importAndroidFileList(List<AndroidFileInfo> selectedFileList) {
        if (null == selectedFileList || selectedFileList.isEmpty()) {
            return;
        }

        final int count = selectedFileList.size();
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.on_obscuring));
        progressDialog.setMessage(getString(R.string.on_obscuring));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMax(count);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        BrowserManager.getInstance().importFiles(selectedFileList, new ImportFileListener() {
            private final int totalCount = count;
            private int currFinished = 0;
            private int failedCount = 0;

            private void onFinish() {
                if (failedCount > 0) {
                    DialogUtil.createDialog(getActivity(), null, getString(R.string.on_obscuring_failed, failedCount)).show();
                }
            }

            @Override
            public void onImportSuccess(String originalPath, ObscureFileInfo obscurePath) {
                BrowserManager.getInstance().onObscureFileFound(obscurePath);
                ++currFinished;
                progressDialog.incrementProgressBy(1);
                if (currFinished == totalCount) {
                    progressDialog.dismiss();
                    onFinish();
                }
            }

            @Override
            public void onImportFailed(String originalPath, Throwable error) {
                ++currFinished;
                ++failedCount;
                progressDialog.incrementProgressBy(1);
                if (currFinished == totalCount) {
                    progressDialog.dismiss();
                    onFinish();
                }
            }
        });
    }

    public enum State {
        Normal,
        Selectable,
    }

    protected static class ItemViewHolder {
        ThumbnailImageView imageVIew;
        TextView nameView;
        TextView size;
        TextView duration;
        TextView encodeTime;
        ObscureFileInfo fileInfo;
    }
}
