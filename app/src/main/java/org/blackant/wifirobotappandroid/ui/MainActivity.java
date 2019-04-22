package org.blackant.wifirobotappandroid.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.baidu.mapapi.map.MapView;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYTextureView;

import org.blackant.wifirobotappandroid.R;
import org.blackant.wifirobotappandroid.utilities.WindowUtils;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    // TODO: 19-3-24 let the user to set the url
    // the url of video live stream
    private String mVideoUrl;
    // the url of robot control
    private String mRouterUrl;


    private ImageButton btnSettings;
    private ImageButton btnAudio;
    private ImageButton btnLight;
    private boolean AudioChange = true;
    private boolean LightChange = true;

    // 播放器的对象
    private KSYTextureView mVideoView;
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

    private MapView mMapView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // load parameters from SharedPreferences
        loadParameters();

        mMapView = findViewById(R.id.bmapView);

        // video view
        mVideoView = findViewById(R.id.ksy_tv);
        mVideoView.shouldAutoPlay(true);
        mVideoView.prepareAsync();

        // buttons
        btnSettings = findViewById(R.id.ButtonCus);
        btnSettings.setOnClickListener(jumpToSettingsListener);
        btnAudio = findViewById(R.id.btnAudio);
        btnAudio.setOnClickListener(changeAudioImgListener);
        btnLight = findViewById(R.id.btnLight);
        btnLight.setOnClickListener(changeLightImgListener);


        // set listeners for the video
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangeListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setOnSeekCompleteListener(mOnSeekCompletedListener);
        // set parameters for the video player
        mVideoView.setBufferTimeMax(2.0f);
        mVideoView.setTimeout(5, 30);
        //......
        // other parameters
        //......
        // set the url of the video and get prepared
        try {
            mVideoView.setDataSource(mVideoUrl);
        } catch (IOException e) {
            Log.e("MediaPlayerError", "There's sth wrong loading the data source");
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

        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        // 释放播放器
        if(mVideoView != null) {
            mVideoView.release();
        }
        mVideoView = null;

        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
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

    private void loadParameters() {
        /**
         * load parameters from SharedPreferences
         */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mRouterUrl = sharedPreferences.getString(getString(R.string.pref_key_router_url), getString(R.string.pref_key_router_url_default));
        mVideoUrl = sharedPreferences.getString(getString(R.string.pref_key_camera_url), getString(R.string.pref_key_camera_url_default));
//        sharedPreferences.getBoolean(getString(R.string.pref_key_test_enabled),getResources().getBoolean(R.bool.pref_key_test_enabled_default));
//        sharedPreferences.getString(getString(R.string.pref_key_camera_url_test), getString(R.string.pref_key_camera_url_test_default));
//        sharedPreferences.getString(getString(R.string.pref_key_router_url_test), getString(R.string.pref_key_router_url_test_default));
//        sharedPreferences.getString(getString(R.string.pref_key_left_motor_speed), getString(R.string.pref_key_left_motor_speed_default));
//        sharedPreferences.getString(getString(R.string.pref_key_right_motor_speed), getString(R.string.pref_key_right_motor_speed_default));
//        sharedPreferences.getString(getString(R.string.pref_key_len_on), getString(R.string.pref_key_len_on_default));
//        sharedPreferences.getString(getString(R.string.pref_key_len_off), getString(R.string.pref_key_len_off_default));
    }

    // TODO: 19-4-21 put this method to SettingsActivity, this method is useless here
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_router_url))) {
            mRouterUrl = sharedPreferences.getString(getString(R.string.pref_key_router_url), getString(R.string.pref_key_router_url_default));
        } else if (key.equals(getString(R.string.pref_key_camera_url))) {
            mVideoUrl = sharedPreferences.getString(getString(R.string.pref_key_camera_url), getString(R.string.pref_key_camera_url_default));
            Log.i("Test", "PreferenceChanged");
            // reload the video view
            mVideoView.reload(mVideoUrl, true);
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
