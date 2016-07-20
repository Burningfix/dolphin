package org.dolphin.secret.browser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.dolphin.lib.util.ValueUtil;
import org.dolphin.secret.BrowserMainActivity;
import org.dolphin.secret.R;
import org.dolphin.secret.SecretApplication;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secret.http.HttpContainer;
import org.dolphin.secret.picker.AndroidFileProvider;
import org.dolphin.secret.play.VideoPlayerActivity;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by hanyanan on 2016/2/11.
 */
public class VideoFileListPage extends FilePage {

    protected List<FileInfo> getFileList() {
        return BrowserManager.getInstance().getVideoFileList();
    }

    protected void addListener() {
        BrowserManager.getInstance().addVideoFileChangeListener(this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        BrowserMainActivity mainActivity = (BrowserMainActivity) getActivity();
        if (mainActivity.getNavigationDrawerFragment() != null
                && mainActivity.getNavigationDrawerFragment().isDrawerOpen()) {
            return;
        }
        if (isNormalState()) {
            inflater.inflate(R.menu.menu_video, menu);
        } else {
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_import) {
            importVideo();
            return true;
        } else if (id == R.id.action_camera) {
            // 拍摄图片
            catchVideo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onItemClicked(FileInfo fileInfo, int position) {
        Intent videoPlaybackActivity = new Intent(getActivity(), VideoPlayerActivity.class);
        String id = HttpContainer.getInstance().deliveryId(fileInfo);
        String path = SecretApplication.getInstance().getHttpServer().wrapObscurePath(id);
        videoPlaybackActivity.putExtra("path", path);
        startActivity(videoPlaybackActivity);
    }

    private String lastCreateFileName = null;

    private void catchVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.addCategory(Intent.CATEGORY_DEFAULT);
        // 根据文件地址创建文件
        new DateFormat();
        String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.getDefault())) + ".mp4";
        File file = new File(BrowserManager.sRootDir, name);
        if (file.exists()) {
            file.delete();
        }
        lastCreateFileName = name;
        // 把文件地址转换成Uri格式
        Uri uri = Uri.fromFile(file);
        // 设置系统相机拍摄照片完成后图片文件的存放地址
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, CATCH_VIDEO_REQUEST_CODE);
        }
    }

    private void importVideo() {
        Intent pickIntent = new Intent();
        pickIntent.setType("video/*");
        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickIntent.setAction("android.intent.action.PICK");
        startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), IMPORT_VIDEO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CATCH_VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (!ValueUtil.isEmpty(lastCreateFileName)) {
                BrowserManager.getInstance().obscureFile(lastCreateFileName);
            }
        } else if (IMPORT_VIDEO_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK) {
            // 导入成功
            if (null == data) {
                return;
            }

            Uri videoUri = data.getData();
            String path = videoUri.getPath();


            List<AndroidFileProvider.FileEntry> selectedFileList = data.getParcelableArrayListExtra("data");
            importFileEntryList(selectedFileList);
        }
    }
}
