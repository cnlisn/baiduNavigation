package com.lisn.baidumapplugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemClock;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import android.content.pm.PackageManager;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
//import com.tdkj.baidumap.offlineMapActivity;
//import com.tdkj.baidumap.BaiDuMapPlugin;
//import org.apache.cordova.CallbackContext;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public class MapNavigation extends Activity {

    private ImageView back;
    private TextView tv_title;
    private ImageView iv_info;
    private ImageView iv_offline;
    private LinearLayout ll_title;
    private MapView mMapView;
    private LinearLayout activity_main;
    private BaiduMap baiduMap;
    private LatLng centerLatLng;
    private LocationClient mLocationClient;
    private FakeR R;
    private Button bt_search;
    private EditText et_input;
    private PoiSearch mPoiSearch;
    private ListView lv_search;
    private Activity T;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        T = this;
        R = new FakeR(this);
        setContentView(R.getId("layout", "activity_map_navigation"));
        initView();
        title();
        initMap();
        // initLocation();
        hasPermission();
    }

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0x003;

    private void hasPermission() {
        if (ContextCompat.checkSelfPermission(MapNavigation.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapNavigation.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            initLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocation();
            } else {
                Toast.makeText(MapNavigation.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    private void initMap() {
        baiduMap = mMapView.getMap();
        MapStatusUpdate zoomStatusUpdate = MapStatusUpdateFactory.zoomTo(13);
        baiduMap.setMapStatus(zoomStatusUpdate);
        centerLatLng = new LatLng(32.030057, 118.788997);
        MapStatusUpdate centerStatusUpdate = MapStatusUpdateFactory.newLatLng(centerLatLng);
        baiduMap.setMapStatus(centerStatusUpdate);
        initSearch();
    }

    private void initSearch() {
        lv_search = (ListView) findViewById(R.getId("id", "Lv_search"));

        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lv_search.setVisibility(View.GONE);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(T, marker.getTitle(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener poiSeachListener = new OnGetPoiSearchResultListener() {
            public String TAG = "search==";

            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult == null || poiResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //详情检索失败
                    // result.error请参考SearchResult.ERRORNO
                    Toast.makeText(MapNavigation.this, "查询失败", Toast.LENGTH_SHORT).show();
                    lv_search.setVisibility(View.GONE);
                } else {
                    //检索成功
//                    Toast.makeText(MapNavigation.this, "检索成功" + poiResult.getCurrentPageCapacity(), Toast.LENGTH_SHORT).show();
                    int i = 0;
                    List<PoiInfo> allPoi = poiResult.getAllPoi();
                    ArrayList<SearchBean> Datas = new ArrayList<>();
                    for (PoiInfo poiInfo : allPoi) {
                        SearchBean data = new SearchBean(poiInfo.name, poiInfo.address,poiInfo.location);

                        Datas.add(data);
                    }
                    lv_search.setVisibility(View.VISIBLE);
                    SearchAdapter searchAdapter = new SearchAdapter(T) {
                        @Override
                        public void ItemClickListener(SearchBean data) {
                            baiduMap.clear();//添加之前，先清空所有的覆盖物
                            MarkerOptions poiOverlay = new MarkerOptions();
                            poiOverlay.title(data.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.getId("mipmap","icon_search")))
                                    .position(data.location);
                            baiduMap.addOverlay(poiOverlay);
                            MapStatusUpdate centerStatusUpdate = MapStatusUpdateFactory.newLatLng(data.location);
                            baiduMap.animateMapStatus(centerStatusUpdate);
                            lv_search.setVisibility(View.GONE);
                        }
                    };
                    searchAdapter.setData(Datas);
                    lv_search.setAdapter(searchAdapter);

                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                Toast.makeText(MapNavigation.this,
                        "地址：" + poiDetailResult.address + "==" + poiDetailResult.getAddress(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        };
        mPoiSearch.setOnGetPoiSearchResultListener(poiSeachListener);
    }

    class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap arg0) {
            super(arg0);
        }

        /**
         * 覆写此方法以改变默认点击行为
         * <p/>
         * index 点击哪一个 PoiInfo
         */
        @Override
        public boolean onPoiClick(int index) {
            //获取所有的poi数据
            PoiResult poiResult = getPoiResult();
            List<PoiInfo> allPoi = poiResult.getAllPoi();
            PoiInfo clickedPoiInfo = allPoi.get(index);
            Toast.makeText(getApplicationContext(),
                    clickedPoiInfo.name, Toast.LENGTH_SHORT).show();

            //发起详情搜索
            PoiDetailSearchOption detailOption = new PoiDetailSearchOption();
            //设置检索的poi的uid
            detailOption.poiUid(clickedPoiInfo.uid);
            mPoiSearch.searchPoiDetail(detailOption);
            return super.onPoiClick(index);
        }

    }

    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {

            try {
                MyLocationData locationData = new MyLocationData.Builder().latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                if (locationData != null) {
                    baiduMap.setMyLocationData(locationData);
                    //                 Toast.makeText(MapNavigation.this, location.getLatitude() + "===" + location.getLongitude(),
                    //                         Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                String errMsg = e.getMessage();
            } finally {
                mLocationClient.stop();
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private void initLocation() {
        if (mLocationClient == null) {
            // 声明LocationClient类
            mLocationClient = new LocationClient(MapNavigation.this);
            // 定位结果监听
            BDLocationListener myListener = new MyLocationListener();
            mLocationClient.registerLocationListener(myListener); // 注册监听函数
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
            int span = 1000;
            option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
            option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
            option.setOpenGps(true);//可选，默认false,设置是否使用gps
            option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
            option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
            option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
            option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
            option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
            mLocationClient.setLocOption(option);
        }

        baiduMap.setMyLocationEnabled(true);
        BitmapDescriptor locationIcon = BitmapDescriptorFactory.fromResource(R.getId("mipmap", "icon_geo"));
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, //跟随模式
                true, locationIcon));
        //开启定位
        mLocationClient.start();
    }

    private void initView() {
        back = (ImageView) findViewById(R.getId("id", "back"));
        tv_title = (TextView) findViewById(R.getId("id", "tv_title"));
        iv_info = (ImageView) findViewById(R.getId("id", "iv_info"));
        iv_offline = (ImageView) findViewById(R.getId("id", "iv_offline"));
        ll_title = (LinearLayout) findViewById(R.getId("id", "ll_title"));
        mMapView = (MapView) findViewById(R.getId("id", "mapView"));
        activity_main = (LinearLayout) findViewById(R.getId("id", "activity_main"));
        bt_search = (Button) findViewById(R.getId("id", "bt_search"));
        et_input = (EditText) findViewById(R.getId("id", "et_input"));

        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ss = et_input.getText().toString().trim();
                if (ss != null && !TextUtils.isEmpty(ss)) {

                    PoiCitySearchOption CitySearch = new PoiCitySearchOption();
                    CitySearch.city("南京");
                    CitySearch.keyword(ss);
                    CitySearch.pageCapacity(50); //查询结果每页容量 默认是10个
                    CitySearch.pageNum(1); //查询第几页
                    mPoiSearch.searchInCity(CitySearch);
                } else {
                    Toast.makeText(MapNavigation.this, "请输入查询内容", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private String titleColor = "#a266c4";
    private int mNrequestCode = 12388;

    private void title() {
        tv_title.setText("地图导航");
        String sColor = getIntent().getStringExtra("titleColor");
        if (!TextUtils.isEmpty(sColor)) {
            titleColor = sColor;
        }
        ll_title.setBackgroundColor(Color.parseColor(titleColor));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("qylb", "hhh");
                setResult(mNrequestCode, intent);
                finish();
            }
        });

        iv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BaiDuMapPlugin.MnCtx.success(titleColor);
                //企业列表
                MapNavigation.this.finish();
            }
        });
        iv_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //离线地图
                Toast.makeText(MapNavigation.this, "离线地图", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(MapNavigation.this, offlineMapActivity.class);
//                intent.putExtra("titleColor", titleColor);
//                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();//mapview的生命周期方法
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}
