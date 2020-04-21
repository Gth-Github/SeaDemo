package demo.viewdraw.yima.Seademo.cameratest;

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
import demo.viewdraw.yima.Seademo.cameratest.utils.MjpegView;
import demo.viewdraw.yima.Seademo.cameratest.utils.TcpTools;

import demo.viewdraw.yima.Seademo.R;


public class VideoActivity extends Activity implements View.OnClickListener {//RadioGroup.OnCheckedChangeListener {
    private String TAG = ">>> " + this.getClass().getSimpleName();
    private Context mContext = this;
    private MjpegView mjpegView;

    private Button btnMenu1, btnMenu2, btnMenu3, btnMenu4;
    private LinearLayout laySender;
    private TextView tvData;
    private EditText etSend;
    private Button btnClear, btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        mjpegView = (MjpegView) findViewById(R.id.mjpegview);

        btnMenu1 = (Button) findViewById(R.id.btnMenu1);
        btnMenu2 = (Button) findViewById(R.id.btnMenu2);
        btnMenu3 = (Button) findViewById(R.id.btnMenu3);
        btnMenu4 = (Button) findViewById(R.id.btnMenu4);

        laySender = (LinearLayout) findViewById(R.id.laySender);
        tvData = (TextView) findViewById(R.id.tvData);
        etSend = (EditText) findViewById(R.id.etSend);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnClear = (Button) findViewById(R.id.btnClear);

        btnMenu1.setOnClickListener(this);
        btnMenu2.setOnClickListener(this);
        btnMenu3.setOnClickListener(this);
        btnMenu4.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        tvData.setMovementMethod(ScrollingMovementMethod.getInstance());

        etSend.setText("测试ABCdef1268");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApiTools.ApiOnResume(this, mjpegView, onTcpCallBackListener);
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



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMenu1:        //屏幕快照
                ApiTools.shotSnap(mContext, mjpegView);
                break;

            case R.id.btnMenu2:        //串口收发
                laySender.setVisibility(laySender.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;

            case R.id.btnMenu3:        //照片列表
                startActivity(new Intent(this, FileListActivity.class));
                this.finish();
                break;

            case R.id.btnMenu4:        //全屏切换
                if (ApiTools.isSwitchFullScreen(mjpegView)) {
                    btnMenu4.setText(R.string.fullscreen);
                    ((LinearLayout) btnMenu4.getParent()).setBackgroundResource(R.drawable.maintab_toolbar_bg);
                } else {
                    btnMenu4.setText(R.string.standard);
                    ((LinearLayout) btnMenu4.getParent()).setBackgroundColor(Color.TRANSPARENT);
                    laySender.setVisibility(View.GONE);
                }
                break;

            case R.id.btnClear:        //清空接收日志
                tvData.setText("");
                break;

            case R.id.btnSend:         //发送串口数据
                String strData = etSend.getText().toString();
                if (!TextUtils.isEmpty(strData)) {
                    TcpTools.getInstance().send(strData, onTcpCallBackListener);
                }
                break;

        }
    }


    //串口数据接收与发送回调
    private TcpTools.OnTcpCallBackListener onTcpCallBackListener = new TcpTools.OnTcpCallBackListener() {
        @Override
        public void onTcpCallBack(int ret, Object obj) {
            Log.d(TAG, "ret=" + ret + ", data=" + obj);
            if (ret == TcpTools.retSendOk) {   //串口数据发送结果
                ApiTools.showTextView(tvData, "发送结果>>" + obj);
                return;
            }
            if (ret == TcpTools.retRevOk) {    //串口数据接收
                ApiTools.showTextView(tvData, (String) obj);
            }
        }
    };

}
