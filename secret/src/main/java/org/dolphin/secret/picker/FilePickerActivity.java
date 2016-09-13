package org.dolphin.secret.picker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import org.dolphin.secret.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by hanyanan on 2016/2/16.
 */
public class FilePickerActivity extends AppCompatActivity {
    public static final String TAG = "FilePickerActivity";
    private final Set<AndroidFileInfo> selectedFile = new HashSet<AndroidFileInfo>();
    private GridView contentView;
    private View submitView;
    private FileListAdapter fileListAdapter = new FileListAdapter();
    private String type = "image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_picker_layout);

        type = getIntent().getType();
        if (TextUtils.isEmpty(type)) {
            type = "image";
        } else {
            int index = type.indexOf("/");
            index = index <= 0 ? type.length() : index;
            type = type.substring(0, index);
        }

        contentView = (GridView) findViewById(R.id.file_pick_gridview);
        submitView = findViewById(R.id.file_pick_submit);
        contentView.setAdapter(fileListAdapter);
        fileListAdapter.setData(AndroidFileProvider.requestType(this, type));
        fileListAdapter.notifyDataSetChanged();
        contentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder holder = (ViewHolder) view.getTag();
                if (holder.checkbox.isChecked()) {
                    selectedFile.remove(fileListAdapter.getItem(position));
                    holder.checkbox.setChecked(false);
                } else {
                    selectedFile.add((AndroidFileInfo) fileListAdapter.getItem(position));
                    holder.checkbox.setChecked(true);
                }
                onCheckChanged();
            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        onCheckChanged();

        submitView.animate().translationY(300).setDuration(200).start();
    }

    public void submit(View view) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("data", new ArrayList<Parcelable>(selectedFile));
        setResult(RESULT_OK, intent);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pick, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void onCheckChanged() {
        setTitle(getString(R.string.selection, selectedFile.size()));
        if (selectedFile.size() > 0) {
            // display submit view
            if (submitView.getTranslationY() <= 0) {
                return;
            }
            submitView.animate().translationY(0).setDuration(200).start();
            return;
        }

        // hide submit view
        if (submitView.getTranslationY() > 0) {
            return;
        }
        submitView.animate().translationY(300).setDuration(200).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_select) {
            selectedFile.addAll(fileListAdapter.fileEntryList);
            fileListAdapter.notifyDataSetChanged();
            onCheckChanged();
            return true;
        } else if (id == R.id.menu_unselect) {
            selectedFile.clear();
            fileListAdapter.notifyDataSetChanged();
            onCheckChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class ViewHolder {
        private PickImageView image;
        private TextView nameTextView;
        private CheckBox checkbox;
    }

    private class FileListAdapter extends BaseAdapter {
        private List<AndroidFileInfo> fileEntryList = null;

        public void setData(List<AndroidFileInfo> fileEntryList) {
            this.fileEntryList = fileEntryList;
        }

        @Override
        public int getCount() {
            if (null == this.fileEntryList) return 0;
            return fileEntryList.size();
        }

        @Override
        public Object getItem(int position) {
            if (position > getCount()) return null;
            return fileEntryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AndroidFileInfo fileEntry = (AndroidFileInfo) getItem(position);
            if (convertView == null) {
                convertView = View.inflate(FilePickerActivity.this, R.layout.file_picker_item, null);
            }
            ViewHolder holder;
            if (convertView.getTag() == null) {
                holder = new ViewHolder();
                holder.image = (PickImageView) convertView.findViewById(R.id.pick_file_item_image);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.pick_file_item_check);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.pick_file_item_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

//            holder.nameTextView.setText(fileEntry.name); // 不显示名称
            if (selectedFile.contains(fileEntry)) {
                holder.checkbox.setChecked(true);
            } else {
                holder.checkbox.setChecked(false);
            }
            holder.image.display(fileEntry);
            return convertView;
        }
    }
}
