package org.dolphin.secret;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.lib.IOUtil;
import org.dolphin.secret.browser.BrowserManager;
import org.dolphin.secret.browser.FilePage;
import org.dolphin.secret.browser.ImageFileListPage;
import org.dolphin.secret.browser.NavigationDrawerFragment;
import org.dolphin.secret.core.FileDecodeOperator;
import org.dolphin.secret.core.FileEncodeOperator;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secret.core.FileInfoContentCache;
import org.dolphin.secret.core.ReadableFileInputStream;
import org.dolphin.secret.util.ContextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String TAG = "MainActivity";
    public static final FileEncodeOperator fileEncodeOperator = new FileEncodeOperator();
    public static final FileDecodeOperator fileDecodeOperator = new FileDecodeOperator();
    TextView tv1, tv2;
    ImageView imageView;
    private BrowserManager browserManager;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private int position = -1;

    private long mPrevPressedTime = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FilePage filePage = new FilePage();
        getFragmentManager().beginTransaction().replace(R.id.container, filePage).commit();


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        int enterCount = ContextUtils.getAndIncreaseFromSharedPreferences(this, "enter_count");
        if (enterCount <= 0) {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setMessage(getString(R.string.user_first_enter));
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.i_known), (DialogInterface.OnClickListener) null);
            dialog.show();
        } else {
            String[] tips = getResources().getStringArray(R.array.user_manual);
            if (null != tips && enterCount <= tips.length) {
                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setMessage(tips[enterCount - 1]);
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.i_known), (DialogInterface.OnClickListener) null);
                dialog.show();
            }
        }
    }

//    @Override
//    public void onNavigationDrawerItemSelected(int position) {
//        // update the main content by replacing fragments
//        this.position = position;
//        FragmentManager fragmentManager = getSupportFragmentManager();
////        fragmentManager.beginTransaction()
////                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
////                .commit();
//        Bundle bundle = new Bundle();
//        switch (position) {
//            case 0:
//                bundle.putInt("type",FileListFragment.VIDEO);
//                break;
//            case 1:
//                bundle.putInt("type",FileListFragment.AUDIO);
//                break;
//            case 2:
//                bundle.putInt("type",FileListFragment.PHOTO);
//                break;
//            case 3:
//                bundle.putInt("type",FileListFragment.DOC);
//                break;
//            case 4:
//                bundle.putInt("type",FileListFragment.BIN);
//                break;
//        }
//        fileListFragment.setArguments(bundle);
//        fragmentManager.beginTransaction().replace(R.id.container, fileListFragment) .commit();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return false;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_refresh) {
////            mFileManager.sync();
//            return true;
//        }else if(id == R.id.action_camera){
//            if(position == 0){//拍摄视频
//                catchVideo();
//            }else{
//                catchPhoto();
//            }
//            mFileManager.sync();
//            //拍摄图片
//            return true;
//        }else if(id == R.id.action_settings){
//            Intent intent = new Intent(this, MoreActivity.class);
//            startActivity(intent);
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

//    private void catchPhoto(){
//        Intent intent=new Intent();
//        // 指定开启系统相机的Action
//        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        // 根据文件地址创建文件
//        new DateFormat();
//
//        Environment environment = AmazingApplication.getInstance().getEnvironment();
//        if(null == environment) return ;
//        String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.getDefault()))+ ".jpg";
//        File file=new File(environment.getRepository()+File.separator+name);
//        if (file.exists()) {
//            file.delete();
//        }
//        // 把文件地址转换成Uri格式
//        Uri uri=Uri.fromFile(file);
//        // 设置系统相机拍摄照片完成后图片文件的存放地址
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        startActivityForResult(intent, PHOTO_REQUEST_CODE);
//    }


//    private void catchVideo(){
////        MediaStore.EXTRA_OUTPUT：设置媒体文件的保存路径。
////        MediaStore.EXTRA_VIDEO_QUALITY：设置视频录制的质量，0为低质量，1为高质量。
////        MediaStore.EXTRA_DURATION_LIMIT：设置视频最大允许录制的时长，单位为毫秒。
////        MediaStore.EXTRA_SIZE_LIMIT：指定视频最大允许的尺寸，单位为byte。
//        Intent intent=new Intent();
//        // 指定开启系统相机的Action
//        intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        // 根据文件地址创建文件
//        new DateFormat();
//
//        Environment environment = AmazingApplication.getInstance().getEnvironment();
//        if(null == environment) return ;
//        String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.getDefault()))+ ".mp4";
//        File file=new File(environment.getRepository()+File.separator+name);
//        if (file.exists()) {
//            file.delete();
//        }
//        // 把文件地址转换成Uri格式
//        Uri uri=Uri.fromFile(file);
//        // 设置系统相机拍摄照片完成后图片文件的存放地址
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        startActivityForResult(intent, VIDEO_REQUEST_CODE);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if((requestCode == VIDEO_REQUEST_CODE
//                || requestCode == PHOTO_REQUEST_CODE) && resultCode == Activity.RESULT_OK){
//            mFileManager.sync();
//        }
//    }

    @Override
    public void onBackPressed() {
        long timeMillis = System.currentTimeMillis();
        if (timeMillis - mPrevPressedTime <= 1500) {
            mPrevPressedTime = timeMillis;
            super.onBackPressed();
            return;
        }

        mPrevPressedTime = timeMillis;
        Toast.makeText(this, R.string.exit_tips, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(this.position == position) {
            Log.d(TAG, "Select the same position, do nothing!");
            return;
        }
        this.position = position;
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new ImageFileListPage();
                break;
            case 1:
                fragment = new ImageFileListPage();
                break;
            case 2:
                fragment = new ImageFileListPage();
                break;
        }
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        invalidateOptionsMenu();
    }
}
