package org.dolphin.secret.browser;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.dolphin.secret.BrowserMainActivity;
import org.dolphin.secret.R;
import org.dolphin.secret.core.ObscureFileInfo;
import org.dolphin.secret.play.ImagePlayerActivity;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by hanyanan on 2016/2/11.
 */
public class ImageFileListPage extends FilePage {
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        BrowserMainActivity mainActivity = (BrowserMainActivity) getActivity();
        if (mainActivity.getNavigationDrawerFragment() != null
                && mainActivity.getNavigationDrawerFragment().isDrawerOpen()) {
            return;
        }
        if (isNormalState()) {
            inflater.inflate(R.menu.menu_image, menu);
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
            importAlbum();
            return true;
        } else if (id == R.id.action_camera) {
            // 拍摄图片
            catchPhoto();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onItemClicked(ObscureFileInfo fileInfo, int position) {
        Intent inten = new Intent(getActivity(), ImagePlayerActivity.class);
        inten.putExtra("position", position);
        startActivity(inten);
    }

    protected String lastCreateFileName = null;

    private void catchPhoto() {
        Intent intent = new Intent();
        // 指定开启系统相机的Action
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        // 根据文件地址创建文件

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

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, CATCH_PHOTO_REQUEST_CODE);
        }
    }

    private void importAlbum() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        pickIntent.setType("image/*");
        pickIntent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true); // 可以多选
        pickIntent.putExtra("android.intent.extra.LOCAL_ONLY", true); // 只选择本地的，忽略server上的
        startActivityForResult(Intent.createChooser(pickIntent, getResources().getString(R.string.select_image)), IMPORT_PHOTO_REQUEST_CODE);
    }

    @Override
    public String getLastCaptureFile() {
        return lastCreateFileName;
    }
}
