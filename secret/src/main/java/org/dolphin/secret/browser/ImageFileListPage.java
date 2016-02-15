package org.dolphin.secret.browser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.dolphin.lib.ValueUtil;
import org.dolphin.secret.R;
import org.dolphin.secret.core.FileInfo;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by hanyanan on 2016/2/11.
 */
public class ImageFileListPage extends FilePage {
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_image, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
//            mFileManager.sync();
            return true;
        } else if (id == R.id.action_camera) {
//            catchPhoto();
            importAlbum();
            //拍摄图片
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onItemClicked(FileInfo fileInfo) {
        // TODO
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CATCH_PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (!ValueUtil.isEmpty(lastCreateFileName)) {
                BrowserManager.getInstance().addFile(lastCreateFileName);
            }
        }
    }

    private String lastCreateFileName = null;

    private void catchPhoto() {
        Intent intent = new Intent();
        // 指定开启系统相机的Action
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        // 根据文件地址创建文件
        new DateFormat();

        String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.getDefault())) + ".jpg";
        File file = new File(BrowserManager.sRootDir, name);
        if (file.exists()) {
            file.delete();
        }
        lastCreateFileName = name;
        // 把文件地址转换成Uri格式
        Uri uri = Uri.fromFile(file);
        // 设置系统相机拍摄照片完成后图片文件的存放地址
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CATCH_PHOTO_REQUEST_CODE);
    }

    private void importAlbum() {
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), IMPORT_PHOTO_REQUEST_CODE);
    }
}
