package org.dolphin.secret.play;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import org.dolphin.secret.R;

/**
 * Created by hanyanan on 2016/5/10.
 */
public class VideoPlayerActivity extends Activity implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {
    private VideoView videoView;
    private ProgressDialog progressDialog;
    private int currPosition = -1;
    private String url = null;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.video_play);
        String filePath = getIntent().getStringExtra("path");
        videoView = (VideoView) findViewById(R.id.myvideoview);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setOnCompletionListener(this);
        videoView.setOnPreparedListener(this);

        url = filePath;

        startPlaying();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String filePath = getIntent().getStringExtra("path");
        url = filePath;
        startPlaying();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        currPosition = videoView.getCurrentPosition();
    }

    public void startPlaying() {
        showProgressDialog();
        videoView.setVideoPath(url);
        videoView.requestFocus();
        videoView.start();
    }

    public void stopPlaying() {
        dismissProgressDialog();
        videoView.clearFocus();
        videoView.stopPlayback();
    }

    public void pausePlaying() {
        videoView.clearFocus();
        videoView.pause();
    }

    @Override
    protected void onPause() {
        pausePlaying();
        super.onPause();
    }

    @Override
    protected void onStop() {
        stopPlaying();
        super.onStop();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        dismissProgressDialog();
        finish();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (currPosition > 0) {
            mp.seekTo(currPosition);
            currPosition = -1;
        }
        int width = mp.getVideoWidth();
        int height = mp.getVideoHeight();
        int orient = getRequestedOrientation();
        if (width < height) { // 需要竖直播放
            if (orient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (orient != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        dismissProgressDialog();
        mp.setLooping(true);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void showProgressDialog() {
        if (null == progressDialog) {
            // create a progress bar while the video file is loading
            progressDialog = new ProgressDialog(this);
            // set a title for the progress bar
//            progressDialog.setTitle("HHHHHHH");
//            // set a message for the progress bar
//            progressDialog.setMessage("Loading...");
            //set the progress bar not cancelable on users' touch
            progressDialog.setCancelable(false);
        }
        // show the progress bar
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
