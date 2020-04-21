package demo.viewdraw.yima.Seademo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import demo.viewdraw.yima.Seademo.cameratest.VideoActivity;
import demo.viewdraw.yima.Seademo.floatvideo.FloatingVideoService;
import yimamapapi.skia.*;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;
import java.util.TooManyListenersException;

import demo.viewdraw.yima.Seademo.cameratest.VideoMainActivity;

// YimaApi.jar 帮助文档位置：http://www.yimasoftdemo.cc:800/YimaEncSDK_HelpFile_Android/classyimamapapi_1_1skia_1_1_yima_lib.html

public class MainActivity extends AppCompatActivity {
    private SkiaDrawView fMainView;

    //本机GPS
    private LocationManager lm;
    private static final String TAG = "GpsActivity";

    private double latitude = 0;
    private double longitude = 0;

    // Used to load the 'native-lib' library on application startup.
    static {
        YimaLib.LoadLib();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashReport.initCrashReport(getApplicationContext(), "c27d4c114d", true);
        String strfile = getApplicationContext().getFilesDir().getAbsolutePath();
        long startTime = System.currentTimeMillis();
        YimaLib.CopyWorkDir(getApplicationContext(), strfile);
        long endTime = System.currentTimeMillis(); //获取结束时间
        Toast.makeText(MainActivity.this, "文件拷贝" + String.valueOf(endTime - startTime), Toast.LENGTH_SHORT).show();
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
        fMainView = (SkiaDrawView) findViewById(R.id.skiaView);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                String[] showStatic = getResources().getStringArray(R.array.showStatic);
                switch (pos) {
                    case 0: {
                        fMainView.mYimaLib.SetDisplayCategory(0);
                        fMainView.postInvalidate();
                        break;
                    }
                    case 1: {
                        fMainView.mYimaLib.SetDisplayCategory(1);
                        fMainView.postInvalidate();
                        break;
                    }
                    case 2: {
                        fMainView.mYimaLib.SetDisplayCategory(2);
                        fMainView.postInvalidate();
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        openGPSSettings();
        // 为获取地理位置信息时设置查询条件  
        String bestProvider = lm.getBestProvider(getCriteria(), true);
        // 获取位置信息  
        // 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER  
        Location location = lm.getLastKnownLocation(bestProvider);
        updateData(location);
        // 监听状态  
        lm.addGpsStatusListener(listener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    private void updateData(Location location) {
        if (location != null) {
            Log.i(TAG,"test2");
            Toast.makeText(MainActivity.this, "数据改变", Toast.LENGTH_SHORT).show();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
    }

    private void openGPSSettings() {
        LocationManager alm = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, " GPS模块正常 ", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, " 请开启GPS！ ", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateData(location);
            Log.i(TAG,"test3");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            switch (i) {
                case LocationProvider
                        .AVAILABLE:
                    Log.i(TAG, "当前GPS可见");
                    break;
            }
            Log.i(TAG,"test4");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.i(TAG,"test1");
            Location location = lm.getLastKnownLocation(s);
            updateData(location);
        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(MainActivity.this,"GPS已禁用",Toast.LENGTH_SHORT).show();
        }
    };

    GpsStatus.Listener listener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int i) {
            switch (i){
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG,"第一次定位");
                    break;
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG,"定位启动");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG,"定位结束");
            }
        }
    };

    String tag = null;

    //放大缩小按钮
    public void ZoomInClick_Event(View view) {
        if(fMainView.bOpeningMap)  return;
        Log.i(tag, "放大");
        fMainView.mYimaLib.SetCurrentScale(fMainView.mYimaLib.GetCurrentScale() / 2);
        fMainView.postInvalidate();//刷新fMainView
    }

    public void ZoomOutClick_Event(View view) {
        if(fMainView.bOpeningMap)  return;
        Log.i(tag, "缩小");
        fMainView.mYimaLib.SetCurrentScale(fMainView.mYimaLib.GetCurrentScale() * 2);
        fMainView.postInvalidate();//刷新fMainView
    }

    //定位本船位置
    public void OwnCenterClick_Event(View view){
        if(fMainView.bOpeningMap)  return;
        //if(m_curLon == 0.0 && m_curLat == 0.0)  return;
        //从本机请求授权定位
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }else{
            Log.i(TAG,"test6");
            if(latitude!=0&&longitude!=0){
                fMainView.mYimaLib.CenterMap((int)(longitude*10000000), (int)(latitude*10000000));
                fMainView.postInvalidate();//刷新fMainView
            }else{
                Toast.makeText(MainActivity.this, "GPS 未收到数据",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //请求权限的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200://刚才的识别码
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意权限,执行我们的操作
                    //本机GPS
                    Toast.makeText(MainActivity.this,"已开启权限",Toast.LENGTH_SHORT).show();
                }else{//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
                    Toast.makeText(MainActivity.this,"未开启定位权限,请手动到设置去开启权限",Toast.LENGTH_LONG).show();
                }
                break;
            default:break;
        }
    }

    //设置留白贴图模式
    public void buttonClick_Event(View view){
        fMainView.bNormalDragMapMode = !fMainView.bNormalDragMapMode;
    }

    public void AddShipsClick_Event(View view) {

        try{
            String name1 = new String("航点1".getBytes("gbk"),"gbk");
            String name2 = new String("航点1".getBytes("gbk"),"gbk");
            String name3 = new String("航点1".getBytes("gbk"),"gbk");

            int wpId1 = fMainView.mYimaLib.AddWayPoint(1207500000, 407500000, name1, 20, "");
            fMainView.mYimaLib.SetWayPointShowSymbol(wpId1, 1);
            int wpId2 = fMainView.mYimaLib.AddWayPoint(1217500000, 407500000, name2, 20, "");
            fMainView.mYimaLib.SetWayPointShowSymbol(wpId2, 3);
            int wpId3 = fMainView.mYimaLib.AddWayPoint(1197500000, 397500000, name3, 20, "");
            fMainView.mYimaLib.SetWayPointShowSymbol(wpId3, 4);
            int[] wpids = {wpId1, wpId2, wpId3};
            int routeID = fMainView.mYimaLib.AddRoute("航线", wpids, 3, true);
            fMainView.mYimaLib.SetRouteBePlannedOrAlternated(routeID, true);
        }
        catch(Exception e) {
        }
        fMainView.postInvalidate();
    }
    //添加、删除船舶
//    public void AddShipsClick_Event(View view){
//        int m_AisType_red = fMainView.mYimaLib.AddAisType("TypeA");
//        int m_AisType_green = fMainView.mYimaLib.AddAisType("TypeB");
//        int m_AisType_yellow_red = fMainView.mYimaLib.AddAisType("TypeC");
//        int m_AisType_unKnown = fMainView.mYimaLib.AddAisType("TypeD");
//        fMainView.mYimaLib.SetAisTypeInfo(m_AisType_red,"",false,49,0,false,0,48,200000,false,false,0,true,0);
//        fMainView.mYimaLib.SetAisTypeInfo(m_AisType_green,"",false,50,0,false,0,48,200000,false,false,0,true,0);
//        fMainView.mYimaLib.SetAisTypeInfo(m_AisType_yellow_red,"",false,51,0,false,0,48,200000,false,false,0,true,0);
//        fMainView.mYimaLib.SetAisTypeInfo(m_AisType_unKnown,"",false, 5 ,0,false,0, 5,200000,false,false,0,true,0);
//        for(int iship = 0; iship < 200; iship++){
//            int shipId = fMainView.mYimaLib.AddOtherVessel(false, 1210000000 + iship * 100000,  320000000, 135 + iship,
//                    135 + iship, 135 + iship, 120 + iship, 120 + iship);
//
////            public void SetDrawShipOptions(boolean bSetOwnshipOrOtherVessel, boolean bShowCourseAndSpeedVector ,
////            boolean bShowTimeMarksOnVector , boolean bShowHeadingLine , float vectorLenTimePer,
////            int vectorStable, int vectorTimeMarkIntvl, int memStoreTrackPointsLength, int  showTrackPointsLength)
//
//            fMainView.mYimaLib.SetDrawShipOptions(false, true, false, false, 10, 1, 5, 30, 30);
//            fMainView.mYimaLib.SetOtherVesselBasicInfo(iship, 21, 112, "", 412000000 + iship, "");
////            int shipId = fMainView.mYimaLib.AddOtherVessel(false, 1210000000,  320000000, 135, 135, 135, 120, 120);
////            int shipPos = fMainView.mYimaLib.GetOtherVesselPosOfID(shipId);
//            fMainView.mYimaLib.SetAisTargetType(iship, m_AisType_unKnown);
//        }
//        fMainView.postInvalidate();
//    }
    //Video按钮点击事件，跳转悬浮窗
    public void StartVideoClick_Event(View view) {
        if (FloatingVideoService.isStarted) {
            return;
        }else {
            startService(new Intent(this, FloatingVideoService.class));
        }

    }
    //跳转视频
    public void windowClick_Event(View view){
        startActivity(new Intent(this,VideoActivity.class));
    }
    @Override
    public void onStart() { super.onStart(); }
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, FloatingVideoService.class));
            }
        }
    }
}
