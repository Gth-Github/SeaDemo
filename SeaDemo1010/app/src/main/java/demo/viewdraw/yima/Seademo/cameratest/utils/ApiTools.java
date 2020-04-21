package demo.viewdraw.yima.Seademo.cameratest.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import demo.viewdraw.yima.Seademo.cameratest.AppContext;
import com.ftr.video.ftrsdk.CameraBroadCtrl;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;


public class ApiTools {
    public static final String deviceIP     = "192.168.3.1";    //板子的 IP地址 10.10.10.1
    public static final int devTcpPort      = 2002;
    public static final String devHttpPort  = "8080";
    public static final String imgDirName   = "mjpeg";          //手机SD卡保存截图的文件夹
    public static String imgDirPath = "";

    //Toast Show
    public static void showMsg(Context ctx, String msg, boolean isShort) {
        Toast.makeText(ctx, msg, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    //TextView Show
    public static void showTextView(TextView tv, String msg) {
        tv.append(msg + "\n");

        int lineHeight  = tv.getLineHeight();
        int lineCount   = tv.getLineCount();
        int tvHeight    = tv.getHeight();
        int offset      = lineHeight * lineCount;

        if (offset > tvHeight) {
            tv.scrollTo(0, offset - tvHeight);
        }else{
            tv.scrollTo(0, 0);
        }
    }

    //检查SD卡，创建文件夹
    public static boolean checkSdCard(Context ctx) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            showMsg(ctx, "请插入SD卡", true);
            return false;
        }

        if (TextUtils.isEmpty(imgDirPath)) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ imgDirName);
            if (!(file.exists() && file.isDirectory())) {
                file.mkdir();
            }
            imgDirPath = file.getAbsolutePath() + "/";
        }
        return !TextUtils.isEmpty(imgDirPath);
    }

    //获取Url
    public static String getUrl(String ip){
        if (ip.split("\\.").length == 4) {
            return "http://" + ip + ":" + devHttpPort + "/?action=stream";
        }
        return null;
    }

    //获取当前时间
    public static String getSysNowTime() {
        Time localTime = new Time();
        localTime.setToNow();
        return localTime.format("%Y-%m-%d-%H-%M-%S");
    }

    //切换全屏模式 或 标准模式
    public static boolean isSwitchFullScreen(MjpegView mjpegView) {
        int mode = mjpegView.getDisplayMode();

        if (mode == MjpegView.FULLSCREEN_MODE) {
            mjpegView.setDisplayMode(MjpegView.KEEP_SCALE_MODE);
        } else {
            mjpegView.setDisplayMode(MjpegView.FULLSCREEN_MODE);
        }
        return mode == MjpegView.FULLSCREEN_MODE;
    }

    //截图
    public static boolean shotSnap(Context ctx, MjpegView mjpegView) {
        if (checkSdCard(ctx)) {
            File file = new File(imgDirPath + getSysNowTime() + ".jpg");
            try {
                Bitmap bmp = mjpegView.getBitmap();
                if (bmp != null) {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);  //压缩
                    bos.flush();
                    bos.close();
                    showMsg(ctx, "图片已保存至" + imgDirPath, false);
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //获取照片的缩略图
    public static Bitmap getThumbnailImg(File file, int width, int height) {
        Bitmap smallBitmap;

        // 直接通过图片路径将图片转化为bitmap,并将bitmap压缩，避免内存溢出
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 10;                              // 图片宽高都为原来的十分之一
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;    // 每个像素占用2byte内存
        options.inPurgeable = true;                             // 如果 inPurgeable
        options.inInputShareable = true;    // 设为True的话表示使用BitmapFactory创建的Bitmap, 用于存储Pixel的内存空间在系统内存不足时可以被回收
        FileInputStream fInputStream;
        try {
            fInputStream = new FileInputStream(file);
            // 建议使用BitmapFactory.decodeStream
            Bitmap bitmap = BitmapFactory.decodeStream(fInputStream, null, options);    // 直接根据图片路径转化为bitmap
            smallBitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);       // 创建所需尺寸居中缩放的位图
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return smallBitmap;
    }

    //删除照片
    public static boolean delFiles(File fileDel) {
        File[] files = new File(imgDirPath).listFiles();
        if(files != null){
            for(File file : files){
                if(file == fileDel){
                    return file.delete();
                }
            }
        }
        return false;
    }

    //获取所有连接到本wifi热点设备IP
    public static ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" ");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connectedIP;
    }

    //获取wifi信息
    public static WifiInfo getWifiInfo(Context ctx){
        WifiManager wifiMgr = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        return wifiInfo;
    }


    //排序 - Integer值越大，则排在前面
    public static class descendSortByIndex implements Comparator<Integer> {
        /**
         * @return 负数：object2<object1，正数：object2>object1，0：相等
         */
        @Override
        public int compare(Integer object1, Integer object2) {

            return object2.compareTo(object1);
        }

    }

    //排序 - File的最后修改时间值越大，则排在前面
    public static class descendSortByTime implements Comparator<File> {
        /**
         * @return 负数：object2<object1，正数：object2>object1，0：相等
         */
        @Override
        public int compare(File object1, File object2) {

            return (int) (object2.lastModified() - object1.lastModified());
        }

    }

    //api - 设置数据来源
    public static void setDataSrc(MjpegView mjpegView) {

        MjpegInputStream mis = MjpegInputStream.getInstance();

        //设置数据来源
        mjpegView.setSource(mis);

        //设置mjpegview的显示模式
        mjpegView.setDisplayMode(mjpegView.getDisplayMode());

        //setFps和getFps方法是为了在屏幕的右上角动态显示当前的帧率,如果我们只需观看画面，下面这句完全可以省去
        //mjpegView.setFps(mjpegView.getFps());

        //调用mjpegView中的线程的run方法，开始显示画面
        mjpegView.startPlay();

    }

    //api - http 连接
    public static class ConnectTask extends AsyncTask<String, Integer, String> {
        private Context mCtx = null;
        private MjpegView mjpegView;
        private InputStream mInputStream;

        public ConnectTask(Context ctx, MjpegView mjpegView) {
            this.mCtx = ctx;
            this.mjpegView = mjpegView;
        }

        @Override
        protected String doInBackground(String... params) {
            for (int i = 0; i < params.length; i++) {
                String url = getUrl(params[i]);
                if(!TextUtils.isEmpty(url)){
                    mInputStream = http(url);
                    if (mInputStream != null) {
                        MjpegInputStream.initInstance(mInputStream);
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mInputStream != null) {
                setDataSrc(mjpegView);
            } else {
                showMsg(mCtx, "连接失败", true);
            }
            super.onPostExecute(result);
        }

        private InputStream http(String url) {
            HttpResponse res;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 500);
            try {
                HttpGet hg = new HttpGet(url);
                res = httpclient.execute(hg);
                return res.getEntity().getContent(); // 从响应中获取消息实体内容
            }
            catch (IOException e) {
                Log.e(">>>http", "err=" + e);
            }
            return null;
        }
    }


    //api - 设置不同生命周期的事件
    public static void ApiOnResume(final Context ctx, final MjpegView mjpegView, TcpTools.OnTcpCallBackListener callBack){
        AppContext.getInstance().getBroad().setDeviceIP(deviceIP);
        AppContext.getInstance().getBroad().init(ctx, new Runnable() {
            @Override
            public void run() {
                Log.d(">>>Broad", "init Ok");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] arrIp = new String[]{deviceIP};
                        new ConnectTask(ctx, mjpegView).execute(arrIp);
                    }
                }, 500);
            }
        });
        //AppContext.getInstance().getBroad().setWIFIChannel("11");

        CameraBroadCtrl.deinit();
        CameraBroadCtrl.create(ctx, deviceIP);
        CameraBroadCtrl.getInstance().TakePhotoNotifyStart();
        CameraBroadCtrl.getInstance().setMsgCallback(new CameraBroadCtrl.CameraBroadCtrlCallback() {
            @Override
            public int process(int what, int param1, int parma2) {
                if (what == CameraBroadCtrl.MSG_CAMERABROADCTRL_TAKEPHOTOS) {
                    Log.d(">>>Camera", "Click");
                }
                return 0;
            }
        });
        TcpTools.getInstance().setServer(ctx, deviceIP, devTcpPort, 60*1000, "gb2312", 3);//callbackmode = 3
        TcpTools.getInstance().recive(callBack);
    }
    public static void ApiOnStop(Context ctx){
        CameraBroadCtrl.getInstance().setMsgCallback(null);
    }
    public static void ApiOnDestroy(Context ctx, MjpegView mjpegView){
        if (mjpegView != null)
            mjpegView.stopPlay();
        TcpTools.getInstance().close();
    }
}
