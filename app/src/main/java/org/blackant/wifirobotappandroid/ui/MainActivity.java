package org.blackant.wifirobotappandroid.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYTextureView;

import org.blackant.wifirobotappandroid.R;
import org.blackant.wifirobotappandroid.utilities.WindowUtils;
import org.blackant.wifirobotappandroid.views.RockerView;

import java.io.IOException;

import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    // the url of video live stream
    private String mVideoUrl;
    // the url of robot control
    private String mRouterUrl;
    // the threshold value of the steering engine
    private int steeringEngineValue_X = 90;
    private int steeringEngineValue_Y = 90;

    Vibrator mVibrator;

    // items in menu bar
    private ImageButton btnSettings;
    private ImageButton btnScreenShot;
    private ImageButton btnAudio;
    private ImageButton btnLight;
    private boolean AudioChange = true;
    private boolean LightChange = true;
    private TextView tvMsg;

    // baidu map
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private LatLng latLng;
    private boolean isFirstLoc = true; // 是否首次定位

    // 播放器的对象
    private KSYTextureView mVideoView;

    // rocker view
    private RockerView mRockerView;
    private RockerView.OnShakeListener mOnShakeListener = new RockerView.OnShakeListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void direction(RockerView.Direction direction) {
            switch (direction) {
                case DIRECTION_UP:
                    Log.i("rockerview", "++++UP++++");
                    break;
                case DIRECTION_DOWN:
                    Log.i("rockerview", "++++DOWN++++");
                    break;
                case DIRECTION_LEFT:
                    Log.i("rockerview", "++++LEFT++++");
                    break;
                case DIRECTION_RIGHT:
                    Log.i("rockerview", "++++RIGHT++++");
                    break;
                case DIRECTION_CENTER:
                    Log.i("rockerview", "++++CENTER++++");
                    break;
            }
        }

        @Override
        public void onFinish() {

        }
    };

    private float baseX, baseY, currentX, currentY, baseSteeringEngineValue_X = 90, baseSteeringEngineValue_Y = 90;
    private View.OnTouchListener mOnTouchListener = (v, event) -> {
        v.performClick();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 按下
                baseX = event.getX();
                baseY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:// 移动
                currentX = event.getX();
                currentY = event.getY();
                Log.i("steering", "currentX=" + currentX + " currentY=" + currentY);
                onScrollSteeringEngine();
                break;
            case MotionEvent.ACTION_UP:// 抬起
            case MotionEvent.ACTION_CANCEL:// 移出区域
                baseX = currentX;
                baseY = currentY;
                baseSteeringEngineValue_X = steeringEngineValue_X;
                baseSteeringEngineValue_Y = steeringEngineValue_Y;
                break;
        }
        return true;
    };






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // load parameters from SharedPreferences
        loadParameters();

        // check permissions
        checkPermissions();

        // items in menu bar
        btnSettings = findViewById(R.id.ButtonCus);
        btnSettings.setOnClickListener(this::onBtnSettingsClicked);
        btnScreenShot = findViewById(R.id.btnScreenShot);
        btnScreenShot.setOnClickListener(this::onBtnScreenshotClicked);
        btnAudio = findViewById(R.id.btnAudio);
        btnAudio.setOnClickListener(this::onBtnAudioClicked);
        btnLight = findViewById(R.id.btnLight);
        btnLight.setOnClickListener(this::onBtnLightClicked);
        tvMsg = findViewById(R.id.tv_msg);
        tvMsg.setText("init");

        // map view
        mMapView = findViewById(R.id.baidu_mv);

        // rocker view
        mRockerView = findViewById(R.id.control_rv);
        if (mRockerView != null) {
            mRockerView.setOnShakeListener(mOnShakeListener);
        }

        // video view
        mVideoView = findViewById(R.id.ksy_tv);
        mVideoView.setOnTouchListener(mOnTouchListener);
        mVideoView.shouldAutoPlay(true);

        // set listeners for the video
//        mVideoView.setOnBufferingUpdateListener(this::onBufferingUpdated);
        // 播放器在准备完成，可以开播时会发出onPrepared回调
        mVideoView.setOnPreparedListener(this::onPrepared);
//        // 播放完成时会发出onCompletion回调
//        mVideoView.setOnCompletionListener(this::onCompletion);
//        mVideoView.setOnInfoListener(this::onInfo);
//        mVideoView.setOnVideoSizeChangedListener(this::onVideoSizeChanged);
        // 播放器遇到错误时会发出onError回调
        mVideoView.setOnErrorListener(this::onError);
//        mVideoView.setOnSeekCompleteListener(this::onSeekCompleted);

        // set parameters for the video player
        mVideoView.setBufferTimeMax(2.0f);
        mVideoView.setTimeout(5, 30);

        // set the url of the video and get prepared
        try {
            mVideoView.setDataSource(mVideoUrl);
        } catch (IOException e) {
            Log.e("MediaPlayerError", "There's sth wrong loading the video data source");
            // TODO: 19-3-24 tell the user that there's sth wrong loading the data source
            e.printStackTrace();
        }
        mVideoView.prepareAsync();
        Log.i("lifecycle", "prepareAsync");

        // init the map
        SDKInitializer.initialize(getApplicationContext());
        initMap();
    }

    @Override
    protected void onResume() {
        Log.i("lifecycle", "onResume");
        super.onResume();

        mMapView.onResume();

        // hide the StatusBar and the NavigationBar
        WindowUtils.setNavigationBarStatusBarHide(MainActivity.this);

        if (mVideoView != null) {
            mVideoView.runInForeground();
            mVideoView.start();
        }
    }

    @Override
    protected void onPause() {
        Log.i("lifecycle", "onPause");
        super.onPause();

        mMapView.onPause();

        if (mVideoView != null) {
            mVideoView.runInBackground(false);
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("lifecycle", "onDestroy");
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        // release the video player
        if(mVideoView != null) {
            mVideoView.release();
        }
        mVideoView = null;

        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView = null;
    }




    protected void checkPermissions() {
        //6.0之后要动态获取权限，重要！！

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝

            // sd卡权限
            String[] SdCardPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, SdCardPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, SdCardPermission, 100);
            }

            //手机状态权限
            String[] readPhoneStatePermission = {Manifest.permission.READ_PHONE_STATE};
            if (ContextCompat.checkSelfPermission(this, readPhoneStatePermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, readPhoneStatePermission, 200);
            }

            //定位权限
            String[] locationPermission = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (ContextCompat.checkSelfPermission(this, locationPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, locationPermission, 300);
            }

            String[] ACCESS_COARSE_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
            if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, ACCESS_COARSE_LOCATION, 400);
            }


            String[] READ_EXTERNAL_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, READ_EXTERNAL_STORAGE, 500);
            }

            String[] WRITE_EXTERNAL_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, WRITE_EXTERNAL_STORAGE, 600);
            }

            String[] VIBRATE = {Manifest.permission.VIBRATE};
            if (ContextCompat.checkSelfPermission(this, VIBRATE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, VIBRATE, 600);
            }
        } else {
            //doSdCardResult();
        }
        //LocationClient.reStart();
    }

    private void onPrepared(IMediaPlayer mp) {
        if (mVideoView != null) {
            // 设置视频伸缩模式，此模式为裁剪模式
            mVideoView.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            // 开始播放视频
            mVideoView.start();
            Log.i("lifecycle", "onPrepared");
        } else {
            Log.e("MediaPlayerError", "mVideoView == null when method onPrepared called");
        }
    }

    private boolean onError(IMediaPlayer mp, int what, int extra) {
        Log.e("MediaPlayerError", "what: " + what + " extra: " + extra);
        return false;
    }


    private void onScrollSteeringEngine() {
            steeringEngineValue_X = (int)(baseSteeringEngineValue_X - (currentX-baseX)/20);
            steeringEngineValue_Y = (int)(baseSteeringEngineValue_Y - (currentY-baseY)/20);
            Log.i("steering", "x=" + steeringEngineValue_X + " y=" + steeringEngineValue_Y);

        if ((steeringEngineValue_X < 10) || (steeringEngineValue_X > 170) || (steeringEngineValue_Y < 10) || (steeringEngineValue_Y > 170)) {
            if (steeringEngineValue_X < 10) {
                steeringEngineValue_X = 10;
            } else if (steeringEngineValue_X > 170) {
                steeringEngineValue_X = 170;
            }
            if (steeringEngineValue_Y < 10) {
                steeringEngineValue_Y = 10;
            } else if (steeringEngineValue_Y > 170) {
                steeringEngineValue_Y = 170;
            }
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 10, 200, 10, 200, 10, 200}; // {Interval time, vibration duration...}
            mVibrator.vibrate(pattern, -1);
        }

        sendSteeringEngineCommand(steeringEngineValue_X, steeringEngineValue_Y);
        tvMsg.setText(String.format("x:%s y:%s", valueOf(steeringEngineValue_X), valueOf(steeringEngineValue_Y)));
    }

    // TODO: 19-5-10 send the commmand
    private void sendSteeringEngineCommand(float steeringEngineValue_X, float steeringEngineValue_Y) {

    }


    private void initMap() {
        //获取地图控件引用
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        // 注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        //配置定位SDK参数
        initLocation();
        mLocationClient.registerLocationListener(myLocationListener);    //注册监听函数
        //开启地图定位图层
        mLocationClient.start();
        //图片点击事件，回到定位点
        mLocationClient.requestLocation();

        //实例化UiSettings类对象
        UiSettings mUiSettings = mBaiduMap.getUiSettings();
        //通过设置enable为true或false 选择是否显示指南针
        mUiSettings.setCompassEnabled(false);
        //通过设置enable为true或false 选择是否启用地图旋转功能
        mUiSettings.setRotateGesturesEnabled(false);
        //通过设置enable为true或false 选择是否启用地图俯视功能
        mUiSettings.setOverlookingGesturesEnabled(false);
        //通过设置enable为true或false 选择是否显示缩放按钮
//        mMapView.showZoomControls(false);
    }

    private void initLocation() {
        // 通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        option.setOpenGps(true); // 打开gps

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

//        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true,null));

        // 设置locationClientOption
        mLocationClient.setLocOption(option);
    }

    private void onBtnSettingsClicked(View v) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void onBtnScreenshotClicked(View v) {
        Bitmap bitmap = mVideoView.getScreenShot();
        // TODO: 19-5-14
    }

    private void onBtnAudioClicked(View v) {
        if (AudioChange) {
            btnAudio.setImageResource(R.drawable.ic_mic_off_grey_50_24dp);
            AudioChange = false;
        } else {
            btnAudio.setImageResource(R.drawable.ic_mic_grey_50_24dp);
            AudioChange = true;
        }
    }

    private void onBtnLightClicked(View v) {
        if (LightChange) {
            btnLight.setImageResource(R.drawable.ic_flash_off_grey_50_24dp);
            LightChange = false;
        } else {
            btnLight.setImageResource(R.drawable.ic_flash_on_grey_50_24dp);
            LightChange = true;
        }
    }

    /**
     * load parameters from SharedPreferences
     */
    private void loadParameters() {
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
            // TODO: 19-4-26
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




    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
//            latLng = new LatLng(22.960000, 113.400000);

            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            MapStatusUpdate mapstatus = MapStatusUpdateFactory.newLatLng(latLng);
            //改变地图状态
            mBaiduMap.setMapStatus(mapstatus);

            if (isFirstLoc) {
                isFirstLoc = false;
//                LatLng ll= new LatLng(22.960000, 113.400000);

                LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }

            int code = location.getLocType();
            Log.e("baidumap", valueOf(code));
        }
    }
}
