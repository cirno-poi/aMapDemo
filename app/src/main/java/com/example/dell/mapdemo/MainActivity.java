package com.example.dell.mapdemo;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.provider.SyncStateContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AMap.OnMarkerClickListener, View.OnClickListener {

    private static final String TAG = "233333";

    private MapView mapView;
    private AMap aMap;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private MyLocationStyle myLocationStyle;

    private FloatingActionButton addFab;
    private Toolbar toolbar;

    public List<Marker> mAddMarkers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        initView();
//        setLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear_all) {
            clearAllMarker();
        }

        return true;
    }

    private void clearAllMarker() {


        ScaleAnimation mADDAnimation = new ScaleAnimation(1, 0, 1, 0);
//            AlphaAnimation mADDAnimation = new AlphaAnimation(0, 1);
        mADDAnimation.setInterpolator(new OvershootInterpolator());
        mADDAnimation.setDuration(300);
//        MyAnimationListener listener = new MyAnimationListener(mAddMarkers);
//        mADDAnimation.setAnimationListener(listener);

        if (mAddMarkers != null) {
            for (Marker marker : mAddMarkers) {
                marker.setAnimation(mADDAnimation);
                marker.startAnimation();
//                marker.remove();
            }

            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (Marker marker : mAddMarkers) {
                        marker.remove();
                    }
                    mAddMarkers.clear();
                }
            }, 250);


        }
    }

    /**
     * marker渐变动画，动画结束后将Marker删除
     */
    class MyAnimationListener implements Animation.AnimationListener {

        private List<Marker> markerList;

        MyAnimationListener(List<Marker> markerList) {
            this.markerList = markerList;
        }

        @Override
        public void onAnimationStart() {

        }

        @Override
        public void onAnimationEnd() {

            for (Marker marker : markerList) {

                marker.remove();
            }

        }
    }

    private void initView() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setMapCustomEnable(true);//允许自定义样式
        aMap.getUiSettings().setTiltGesturesEnabled(false);//禁用双手拖拽3D效果
        aMap.getUiSettings().setRotateGesturesEnabled(false);//禁用地图旋转
        aMap.getUiSettings().setZoomControlsEnabled(false);//禁用缩放控制器
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//定位一次，且将视角移动到地图中心点。
        myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.moveCamera(CameraUpdateFactory.zoomTo(14f));//设置地图缩放

        addFab = findViewById(R.id.fab);
        addFab.setOnClickListener(this);
        setMapCustomStyleFile(this);
    }

    /**
     * 设置自定义样式文件，如果想要纹理生效，则必须设置自定义样式文件
     *
     * @param context
     */
    private void setMapCustomStyleFile(Context context) {
//        String styleName = "style_json_texture.json";
        String styleName = "style.data";
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        String filePath = null;
        try {
            inputStream = context.getAssets().open(styleName);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);

            filePath = context.getFilesDir().getAbsolutePath();
            File file = new File(filePath + "/" + styleName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            outputStream.write(b);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();

                if (outputStream != null)
                    outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        aMap.setCustomMapStylePath(filePath + "/" + styleName);

    }

    private void setLocation() {

        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(31.23, 121.47));//第一个参数是维度，第二个是经度
        markerOptions.title("上海");
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_mark_test));

        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {

                final Marker marker = aMap.addMarker(markerOptions);

                ScaleAnimation mADDAnimation = new ScaleAnimation(0, 1, 0, 1);
//            AlphaAnimation mADDAnimation = new AlphaAnimation(0, 1);
                mADDAnimation.setInterpolator(new OvershootInterpolator());
                mADDAnimation.setDuration(300);
                marker.setAnimation(mADDAnimation);
                marker.startAnimation();

            }
        }, 3000);

        aMap.setOnMarkerClickListener(this);
    }

    /**
     * 添加随机点到地图
     *
     * @param title 点标题
     */
    private void addMarker(String title) {
        final MarkerOptions markerOptions = new MarkerOptions();

//        31.184230, 121.358779
//        31.140600, 121.404906

        double minLat = 31.140600;
        double maxLat = 31.184230;
        double minLng = 121.358779;
        double maxLng = 121.404906;

        Random random = new Random();

        double lat = random.nextDouble() * (maxLat - minLat) + minLat;
        Log.d(TAG, "随机产生的纬度: " + lat);

        double lng = random.nextDouble() * (maxLng - minLng) + minLng;
        Log.d(TAG, "随机产生的纬度: " + lng);


        markerOptions.position(new LatLng(lat, lng));//第一个参数是维度，第二个是经度
        markerOptions.title(title);
        markerOptions.anchor(0.5f, 0.5f);

        View view = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(view);


        markerOptions.icon(bitmapDescriptor);

        final Marker marker = aMap.addMarker(markerOptions);
//        aMap.addMarkers()

        ScaleAnimation mADDAnimation = new ScaleAnimation(0, 1, 0, 1);
//            AlphaAnimation mADDAnimation = new AlphaAnimation(0, 1);
        mADDAnimation.setInterpolator(new OvershootInterpolator());
        mADDAnimation.setDuration(300);
        marker.setAnimation(mADDAnimation);
        marker.startAnimation();

        mAddMarkers.add(marker);

        aMap.setOnMarkerClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!TextUtils.isEmpty(marker.getTitle())) {
            Toast.makeText(this, "点击了" + marker.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.fab) {
            addMarker("添加点");
        }

    }
}