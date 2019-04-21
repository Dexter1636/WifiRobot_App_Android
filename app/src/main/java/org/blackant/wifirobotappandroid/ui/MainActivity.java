package org.blackant.wifirobotappandroid.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYTextureView;

import org.blackant.wifirobotappandroid.R;
import org.blackant.wifirobotappandroid.utilities.WindowUtils;

import java.io.IOException;

// just test

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    // TODO: 19-3-24 let the user to set the url
    private String mVideoUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks1";
    private String mRouterUrl;


    private KSYTextureView mVideoView;
    private ImageButton btnSettings;
    private ImageButton btnAudio;
    private ImageButton btnLight;
    private boolean AudioChange = true;
    private boolean LightChange = true;

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
    private final View.OnClickListener changeAudioImgListener = this::changeAudioImg;
    private final View.OnClickListener changeLightImgListener = this::changeLightImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // load data from sharedpreference
        setupSharedPreferences();

        // video view
        mVideoView = findViewById(R.id.ksy_tv);
        mVideoView.shouldAutoPlay(true);
        mVideoView.prepareAsync();

        //some buttons
        btnSettings = findViewById(R.id.ButtonCus);
        btnSettings.setOnClickListener(jumpToSettingsListener);
        btnAudio = findViewById(R.id.btnAudio);
        btnAudio.setOnClickListener(changeAudioImgListener);
        btnLight = findViewById(R.id.btnLight);
        btnLight.setOnClickListener(changeLightImgListener);


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

    @Override
    protected void onResume() {
        super.onResume();

        // hide the StatusBar and the NavigationBar
        WindowUtils.setNavigationBarStatusBarHide(MainActivity.this);
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mRouterUrl = sharedPreferences.getString(getString(R.string.pref_key_router_url), getString(R.string.pref_key_router_url_default));
        mVideoUrl = sharedPreferences.getString(getString(R.string.pref_key_camera_url), getString(R.string.pref_key_camera_url_default));
        sharedPreferences.getBoolean(getString(R.string.pref_key_test_enabled),getResources().getBoolean(R.bool.pref_key_test_enabled_default));
        sharedPreferences.getString(getString(R.string.pref_key_camera_url_test), getString(R.string.pref_key_camera_url_test_default));
        sharedPreferences.getString(getString(R.string.pref_key_router_url_test), getString(R.string.pref_key_router_url_test_default));
        sharedPreferences.getString(getString(R.string.pref_key_left_motor_speed), getString(R.string.pref_key_left_motor_speed_default));
        sharedPreferences.getString(getString(R.string.pref_key_right_motor_speed), getString(R.string.pref_key_right_motor_speed_default));
        sharedPreferences.getString(getString(R.string.pref_key_len_on), getString(R.string.pref_key_len_on_default));
        sharedPreferences.getString(getString(R.string.pref_key_len_off), getString(R.string.pref_key_len_off_default));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
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

    private void changeAudioImg(View v) {
        if (AudioChange) {
            btnAudio.setImageResource(R.drawable.ic_mic_off_grey_50_24dp);
            AudioChange = false;
        } else {
            btnAudio.setImageResource(R.drawable.ic_mic_grey_50_24dp);
            AudioChange = true;
        }
    }

    private void changeLightImg(View v) {
        if (LightChange) {
            btnLight.setImageResource(R.drawable.ic_flash_off_grey_50_24dp);
            LightChange = false;
        } else {
            btnLight.setImageResource(R.drawable.ic_flash_on_grey_50_24dp);
            LightChange = true;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_router_url))) {
            mRouterUrl = sharedPreferences.getString(getString(R.string.pref_key_router_url), getString(R.string.pref_key_router_url_default));
        } else if (key.equals(getString(R.string.pref_key_camera_url))) {
            mVideoUrl = sharedPreferences.getString(getString(R.string.pref_key_camera_url), getString(R.string.pref_key_camera_url_default));
        } else if (key.equals(getString(R.string.pref_key_test_enabled))) {
            sharedPreferences.getBoolean(getString(R.string.pref_key_test_enabled),getResources().getBoolean(R.bool.pref_key_test_enabled_default));
        } else if (key.equals(getString(R.string.pref_key_camera_url_test))) {
            sharedPreferences.getString(getString(R.string.pref_key_camera_url_test), getString(R.string.pref_key_camera_url_test_default));
        } else if (key.equals(getString(R.string.pref_key_router_url_test))) {
            sharedPreferences.getString(getString(R.string.pref_key_router_url_test), getString(R.string.pref_key_router_url_test_default));
        } else if (key.equals(getString(R.string.pref_key_left_motor_speed))) {
            sharedPreferences.getString(getString(R.string.pref_key_left_motor_speed), getString(R.string.pref_key_left_motor_speed_default));
        } else if (key.equals(getString(R.string.pref_key_right_motor_speed))) {
            sharedPreferences.getString(getString(R.string.pref_key_right_motor_speed), getString(R.string.pref_key_right_motor_speed_default));
        } else if (key.equals(getString(R.string.pref_key_len_on))) {
            sharedPreferences.getString(getString(R.string.pref_key_len_on), getString(R.string.pref_key_len_on_default));
        } else if (key.equals(getString(R.string.pref_key_len_off))) {
            sharedPreferences.getString(getString(R.string.pref_key_len_off), getString(R.string.pref_key_len_off_default));
        }
    }
}
