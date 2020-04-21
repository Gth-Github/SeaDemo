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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.graphics.Color;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import demo.viewdraw.yima.Seademo.cameratest.utils.ApiTools;
import demo.viewdraw.yima.Seademo.cameratest.utils.MjpegInputStream;
import demo.viewdraw.yima.Seademo.cameratest.utils.MjpegView;
import demo.viewdraw.yima.Seademo.cameratest.utils.TcpTools;

import demo.viewdraw.yima.Seademo.R;

import com.tencent.bugly.crashreport.CrashReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import demo.viewdraw.yima.Seademo.cameratest.VideoActivity;
import demo.viewdraw.yima.Seademo.floatvideo.FloatingVideoService;
import yimamapapi.skia.M_POINT;
import yimamapapi.skia.YimaLib;

// YimaApi.jar 帮助文档位置：http://www.yimasoftdemo.cc:800/YimaEncSDK_HelpFile_Android/classyimamapapi_1_1skia_1_1_yima_lib.html

public class Main2Activity extends Activity  {
    private SkiaDrawView fMainView;
    private String TAG = ">>> " + this.getClass().getSimpleName();
    private Context mContext = this;
    private MjpegView mjpegView;
    //private TextView tvData;
    private TextView textView4;
    //异步线程
    private ApiTools.ConnectTask task;
    //定位相关
    Location location;
    Handler handler = new Handler();
    private GPSUtils gpsUtils;
    //视频显示设置
    private int videoWidth,videoHeight;

    //本船绘制相关
    private M_POINT m_point = new M_POINT();

    //串口数据
    public double pre_leida,//前方雷达
            ship_sd ,//船速
            stopship_dis,//停船距离
            shipfrist_ele,//船首控制器电源电压
            cock_ele, //驾驶舱控制器电源电压
            log_flag;//提示信息标志
    //本机GPS
    /*private LocationManager lm;
    private static final String TAG2 = "GpsActivity";

    private double latitude = 0;
    private double longitude = 0;*/

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
        Toast.makeText(Main2Activity.this, "文件拷贝" + String.valueOf(endTime - startTime), Toast.LENGTH_SHORT).show();
        //getSupportActionBar().hide();
        //海图显示
        setContentView(R.layout.layout_mix);
        fMainView = (SkiaDrawView) findViewById(R.id.skiaView);
        //视频显示
        mjpegView = (MjpegView) findViewById(R.id.mjpegview);
        //tvData = (TextView) findViewById(R.id.tvData);
        //tvData.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView4 = findViewById(R.id.textView4);

        //原生Android GPSapi
        gpsUtils = new GPSUtils(Main2Activity.this);//初始化gps
        handler.postDelayed(runnable,0);
        /*final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    TextView tv = (TextView)findViewById(R.id.tvData);//根据id获取TextView组件
                    tv.setText("前方雷达"+pre_leida);
                }
            }
        });
        thread.start();*/
        m_point.x = 1210000000;
        m_point.y = 320000000;
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            location = gpsUtils.getLocation();//获取位置信息

            if (location != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { updateView(location);
                    }
                });
                handler.removeCallbacks(runnable);
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };
    //空的定义方法
    private void updateView(Location location) {
        if (location != null) {
            Toast ts1=Toast.makeText(Main2Activity.this,"未接受到GPS信号",Toast.LENGTH_SHORT    );
            ts1.show();
        } else {
            Toast ts2=Toast.makeText(Main2Activity.this,"Toast提示消息",Toast.LENGTH_SHORT    );
            ts2.show();
        }
    }



    //定位本船位置
    public void OwnCenterClick_Event(View view){
        if(fMainView.bOpeningMap)  return;
        //if(m_curLon == 0.0 && m_curLat == 0.0)  return;
        if(location == null){
            Toast ts=Toast.makeText(Main2Activity.this,"未接受到GPS信号,请确认GPS是否打开或移至开阔地带！",Toast.LENGTH_LONG   );
            ts.show();
        }else {
            fMainView.mYimaLib.CenterMap((int)(location.getLongitude()*10000000), (int)(location.getLatitude()*10000000));
            m_point.x = (int) (location.getLongitude()*10000000);
            m_point.y = (int) (location.getLatitude()*10000000);
            SetOwnShip("", m_point);
            //fMainView.postInvalidate();//刷新fMainView
        }

    }

    //显示本船
    public void SetOwnShip(String shipName, M_POINT m_point) {

        fMainView.mYimaLib.SetIfShowOwnship(true);
        try {
            String testArray = new String("当前船只".getBytes("gbk"), "gbk");
            fMainView.mYimaLib.SetOwnShipBasicInfo(testArray, "123456789", 90, 16);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //位置刷新模块 坐标 航向 速度角 对水速度，对地速度
        fMainView.mYimaLib.SetOwnShipCurrentInfo(m_point.x, m_point.y, 60, 30,
                30, 20, 30);
        //可以设置 船只 是否显示 航向 以及航迹点
        fMainView.mYimaLib.SetDrawShipOptions(true, false,
                false, false, 10, 0,
                5, 30, 30);


        //设置船只的
//        skiaDrawView.mYimaLib.SetOtherVesselCurrentInfo(shipId,mPoint.x,mPoint.y,60,30,30,20,30);


        //设置本船显示样式
        fMainView.mYimaLib.SetOwnShipShowSymbol(false, 1, true,
                16, 500000000);
        fMainView.postInvalidate();

//        Toast.makeText(Main2Activity.this, "测试设置本船", Toast.LENGTH_SHORT).show();
    }

        /*Spinner spinner = (Spinner) findViewById(R.id.spinner);
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
        });*/

       /* lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        openGPSSettings();
        // 为获取地理位置信息时设置查询条件  
        String bestProvider = lm.getBestProvider(getCriteria(), true);
        // 获取位置信息  
        // 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER  
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
            Log.i(TAG, "test2");
            Toast.makeText(Main2Activity.this, "数据改变", Toast.LENGTH_SHORT).show();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
    }

    private void openGPSSettings() {
        LocationManager alm = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
            Log.i(TAG, "test3");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            switch (i) {
                case LocationProvider
                        .AVAILABLE:
                    Log.i(TAG, "当前GPS可见");
                    break;
            }
            Log.i(TAG, "test4");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.i(TAG, "test1");
            Location location = lm.getLastKnownLocation(s);
            updateData(location);
        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(Main2Activity.this,"GPS已禁用",Toast.LENGTH_SHORT).show();
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
    }*/

    //定位本船位置
   /* public void OwnCenterClick_Event(View view){
        if(fMainView.bOpeningMap)  return;
        //if(m_curLon == 0.0 && m_curLat == 0.0)  return;
        //从本机请求授权定位
        if(ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions(Main2Activity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }else{
            Log.i(TAG,"test6");
            if(latitude!=0&&longitude!=0){
                fMainView.mYimaLib.CenterMap((int)(longitude*10000000), (int)(latitude*10000000));
                fMainView.postInvalidate();//刷新fMainView
            }else{
                Toast.makeText(Main2Activity.this, "GPS 未收到数据",Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    //请求权限的回调
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200://刚才的识别码
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意权限,执行我们的操作
                    //本机GPS
                    Toast.makeText(Main2Activity.this,"已开启权限",Toast.LENGTH_SHORT).show();
                }else{//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
                    Toast.makeText(Main2Activity.this,"未开启定位权限,请手动到设置去开启权限",Toast.LENGTH_LONG).show();
                }
                break;
            default:break;
        }
    }*/

    //设置留白贴图模式
   /* public void buttonClick_Event(View view){
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
//    }*/
    //Video按钮点击事件，跳转悬浮窗
    /*public void StartVideoClick_Event(View view) {
        if (FloatingVideoService.isStarted) {
            return;
        }else {
            startService(new Intent(this, FloatingVideoService.class));
        }

    }*/
    //显示隐藏视频
   /*public void AddvideoClick_Event(View view){
       if(mjpegView.getVisibility() == View.VISIBLE){
           mjpegView.setVisibility(View.INVISIBLE);
       }else {
           mjpegView.setVisibility(View.VISIBLE);
       }
    }*/
    //显示隐藏数据
    /*
    public void ShowAll_Event(View view){
        if(tvData.getVisibility() == View.VISIBLE){
            tvData.setVisibility(View.INVISIBLE);
        }else {
            tvData.setVisibility(View.VISIBLE);
        }
    }*/
    //视频全屏显示FullScr_Event
    public void FullScr_Event(View view){
        if(mjpegView.getWidth()== fMainView.getWidth()){
            videoWidth = 870;
            videoHeight = 700;
            mjpegView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight));
        }else {
            videoWidth = fMainView.getWidth();
            videoHeight = fMainView.getHeight();
            mjpegView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mjpegView.startPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApiTools.ApiOnResume(this, mjpegView, onTcpCallBackListener);

    }
   @Override
    protected void onPause() {
        super.onPause();
        MjpegInputStream.closeInstance();
    }
    @Override
    protected void onStop() {
        super.onStop();
        ApiTools.ApiOnStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApiTools.ApiOnDestroy(this, mjpegView);
    }

    public byte[] toByteArray (Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }
    //串口数据接收与发送回调
    public  TcpTools.OnTcpCallBackListener onTcpCallBackListener = new TcpTools.OnTcpCallBackListener() {
        @Override
        public void onTcpCallBack(int ret, Object obj) {   //Object obj
            Log.d(TAG, "ret=" + ret + ", dataAAAAAAAAAAAAAAAAAA=" + obj);
            //添加
            if (ret == TcpTools.retSendOk) {   //串口数据发送结果
                //ApiTools.showTextView(tvData, "发送结果>>" + obj);
                return;
            }
            if (ret == TcpTools.retRevOk) {    //串口数据接收
                String strobj =  obj.toString();
                System.out.println(""+strobj);
                /*strobj = strobj.replace(" ","");
                if (strobj.startsWith("AA55")){
                    int len = Integer.parseInt(strobj.substring(4,6),16);

                    System.out .println(strobj.substring(6,len*2+2));
                    //System.out.println("lenlenlenlenlenlenlen++++"+len);
                }*/
               String[] oldstrobj = strobj.split(" ");
                //方法二
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA+++++++++++++"+oldstrobj[0]);
                System.out.println("55555555555555555555555555555555555555++++++++++++++++++"+oldstrobj[1]);
                //strobj.startsWith("AA55");
                if (oldstrobj[0].equals("AA")&&oldstrobj[1].equals("55")){
                    //Integer.parseInt()
                        int pre_leida1, pre_leida2;
                        pre_leida1 = Integer.valueOf(oldstrobj[3],16)*256;
                        pre_leida2 = Integer.valueOf(oldstrobj[4],16);
                        pre_leida = (pre_leida1+pre_leida2)/10.0;
                        ship_sd = Integer.valueOf(oldstrobj[5],16)/100.0;
                        //setShip_sd(ship_sd);
                        int stopship_dis1, stopship_dis2;
                        stopship_dis1 = Integer.valueOf(oldstrobj[6],16)*256;
                        stopship_dis2 = Integer.valueOf(oldstrobj[7],16);
                        stopship_dis = (stopship_dis1+stopship_dis2)/10.0;

                        int shipfrist_ele1, shipfrist_ele2;
                        shipfrist_ele1 = Integer.valueOf(oldstrobj[8],16)*256;
                        shipfrist_ele2 = Integer.valueOf(oldstrobj[9],16);
                        shipfrist_ele = (shipfrist_ele1+shipfrist_ele2)/10.0;

                        int cock_ele1, cock_ele2;
                        cock_ele1 = Integer.valueOf(oldstrobj[10],16)*256;
                        cock_ele2 = Integer.valueOf(oldstrobj[11],16);
                        cock_ele = (cock_ele1+cock_ele2)/10.0;
                }
                //串口数据的显示，目前不需要全部显示
                /*
                ApiTools.showTextView(tvData,"前方障碍物:"+pre_leida+" m");
                ApiTools.showTextView(tvData,"当前船速:"+ship_sd+" m/s");
                ApiTools.showTextView(tvData,"停船距离:"+stopship_dis+" m");
                ApiTools.showTextView(tvData,"船首控制器电压:"+shipfrist_ele+" V");
                ApiTools.showTextView(tvData,"驾驶舱控制器电压:"+cock_ele+" V");
                */
                //将船速等数据传入视频绘制类，实现数据在视频框内的叠加显示
                mjpegView.getshipsd(ship_sd,pre_leida,stopship_dis);
                //预警色块显示
                if (pre_leida>=20){
                    textView4.setBackgroundColor(Color.GREEN);
                    textView4.setText("前方障碍物距离："+ pre_leida +"m");
                }else if(pre_leida>=5&pre_leida<20){
                    textView4.setBackgroundColor(Color.YELLOW);
                    textView4.setText("前方障碍物距离："+ pre_leida +"m");
                }else if(pre_leida<5){
                    textView4.setBackgroundColor(Color.RED);
                    textView4.setText("前方障碍物距离："+ pre_leida +"m");
                }

                /*
                if ("01".equals(oldstrobj[12])){
                    ApiTools.showTextView(tvData,"提示信息:"+"船首请充电！");
                }else {
                    ApiTools.showTextView(tvData,"提示信息:"+"船首电量正常！");
                }*/
            }
        }
    };

    //@Override
   /*protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                startService(new Intent(Main2Activity.this, FloatingVideoService.class));
            }
        }
    }*/

}
