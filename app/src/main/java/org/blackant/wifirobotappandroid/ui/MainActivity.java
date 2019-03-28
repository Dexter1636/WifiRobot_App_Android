package org.blackant.wifirobotappandroid.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYTextureView;

import org.blackant.wifirobotappandroid.R;
import org.blackant.wifirobotappandroid.utilities.WindowUtils;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    // TODO: 19-3-24 let the user to set the url
    private final String mVideoUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks1";

    private KSYTextureView mVideoView;
    private ImageButton btnSettings;

    // 播放器的对象
    private KSYMediaPlayer ksyMediaPlayer;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    // 播放器在准备完成，可以开播时会发出onPrepared回调
    private IMediaPlayer.OnPreparedListener mOnPreparedListener = this::onPrepared;
    // 播放完成时会发出onCompletion回调
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    // 播放器遇到错误时会发出onError回调
    private IMediaPlayer.OnErrorListener mOnErrorListener = MainActivity::onError;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangeListener;
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompletedListener;

    private final View.OnClickListener jumpToSettingsListener = this::jumpToSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // hide the StatusBar and the NavigationBar
        WindowUtils.setNavigationBarStatusBarHide(MainActivity.this);

        mVideoView = findViewById(R.id.ksy_tv);
        mVideoView.shouldAutoPlay(true);
        mVideoView.prepareAsync();

        btnSettings = findViewById(R.id.ButtonCus);
        btnSettings.setOnClickListener(jumpToSettingsListener);

        //设置监听器
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangeListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setOnSeekCompleteListener(mOnSeekCompletedListener);
        //设置播放参数
        mVideoView.setBufferTimeMax(2.0f);
        mVideoView.setTimeout(5, 30);
        //......
        //(其它参数设置)
        //......
        //设置播放地址并准备
        try {
            mVideoView.setDataSource(mVideoUrl);
        } catch (IOException e) {
            // TODO: 19-3-24 tell the user that there's sth wrong loading the data source
            e.printStackTrace();
        }
        mVideoView.prepareAsync();
    }


    private void onPrepared(IMediaPlayer mp) {
        if (mVideoView != null) {
            // 设置视频伸缩模式，此模式为裁剪模式
            mVideoView.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            // 开始播放视频
            mVideoView.start();
        }
    }

    private static boolean onError(IMediaPlayer mp, int what, int extra) {
        Log.e("MediaPlayerError", "what: " + what + " extra: " + extra);
        return false;
    }

    private void jumpToSettings(View v) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}
