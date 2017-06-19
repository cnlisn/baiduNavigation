package com.lisn.baidumapplugin.Test;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.lisn.baidumapplugin.BNGuideActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/6/19.
 */

public class NavigationHelper {
    private static final String[] authBaseArr = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private static final String[] authComArr = {Manifest.permission.READ_PHONE_STATE};
    private static final int authBaseRequestCode = 11001;
    private static final int authComRequestCode = 11002;
    private static final String TTS_APP_ID = "9624919";
    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;
    public static final String ROUTE_PLAN_NODE = "routePlanNode";

    private Button button;
    private Context mContext;
    private Activity T;

    private String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "BNSDK";

    public NavigationHelper(Activity activity) {
        mContext = activity;
        T = activity;
        if (initDirs()) {
            initNavi();
        } else {
            Toast.makeText(T, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
        }
    }


    /*    判断是否具有基本权限     */
    private boolean hasBasePhoneAuth() {
        PackageManager pm = T.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, T.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
            // showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            // showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    // showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    // showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void initSetting() {
        // BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, TTS_APP_ID);
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    String authinfo = null;

    /*导航初始化*/
    private void initNavi() {
        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (!hasBasePhoneAuth()) {
                T.requestPermissions(authBaseArr, authBaseRequestCode);
                return;
            }
        }

        BaiduNaviManager.getInstance().init(T, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + msg;
                }
//                T.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(T, authinfo, Toast.LENGTH_LONG).show();
//                    }
//                });
                Log.e("---", "onAuthResult: " + authinfo);
            }

            public void initSuccess() {
//                Toast.makeText(T, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
//                Toast.makeText(T, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(T, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }

        }, null, ttsHandler, ttsPlayStateListener);

    }

    //region Description
    //    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == authBaseRequestCode) {
//            for (int ret : grantResults) {
//                if (ret == PackageManager.PERMISSION_GRANTED) {
//                    continue;
//                } else {
//                    Toast.makeText(mContext, "缺少基本的权限!", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            }
//            initNavi();
//        } else if (requestCode == authComRequestCode) {
//            for (int ret : grantResults) {
//                if (ret == PackageManager.PERMISSION_GRANTED) {
//                    continue;
//                }
//            }
//            routeplanToNavi(100, 32, 118.805325, 32.094316);
//        }
//
//    }
    //endregion

    /*是否完成权限认证*/
    private boolean hasCompletePhoneAuth() {
        PackageManager pm = T.getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, T.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /*开始导航*/
    public void routeplanToNavi(double sLo, double sLa, double eLo, double eLa) {
        BNRoutePlanNode.CoordinateType coType = BNRoutePlanNode.CoordinateType.BD09LL;
        if (!hasInitSuccess) {
            Toast.makeText(T, "地图还未初始化完成!", Toast.LENGTH_SHORT).show();
        }
        // 权限申请
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    T.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
                    Toast.makeText(T, "没有完备的权限!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        switch (coType) {
//            case GCJ02: {
//                sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
//                break;
//            }
//            case WGS84: {
//                sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
//                break;
//            }
//            case BD09_MC: {
//                sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
//                break;
//            }
            case BD09LL: {
                sNode = new BNRoutePlanNode(sLo, sLa, "南----京南", null, coType);
                eNode = new BNRoutePlanNode(eLo, eLa, "南京。。。。", null, coType);
                break;
            }
            default:
                ;
        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);
            BaiduNaviManager.getInstance().launchNavigator(T, list, 1, true, new RoutePlanListener(sNode));
        }
    }

    public class RoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public RoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {

            Intent intent = new Intent(T, BNGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            T.startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            Toast.makeText(T, "路线规划失败", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private String getSdcardDir() {
//        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
//            return Environment.getExternalStorageDirectory().toString();
//        }
        String path = mContext.getFilesDir().getAbsolutePath();
        return path;
    }
}
