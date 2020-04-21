package demo.viewdraw.yima.Seademo.cameratest;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import com.ftr.video.ftrsdk.fBroad;


public class AppContext extends Application {
    private fBroad m_broad = new fBroad();
    private HandlerThread m_ServerHandlerThread = new HandlerThread("Server");
    private Handler m_handler;
    private static AppContext m_instance;

    @Override
    public void onCreate() {
        super.onCreate();
        m_instance = this;
        m_ServerHandlerThread.start();
        m_handler = new Handler(m_ServerHandlerThread.getLooper());
    }

    public static AppContext getInstance(){
        return m_instance;
    }

    public fBroad getBroad(){
        return m_broad;
    }

}
