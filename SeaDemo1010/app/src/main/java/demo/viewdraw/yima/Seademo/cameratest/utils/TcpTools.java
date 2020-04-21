package demo.viewdraw.yima.Seademo.cameratest.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



@SuppressWarnings("unused")
public class TcpTools {
    public interface OnTcpCallBackListener {
        void onTcpCallBack(int ret, Object obj);
    }
    public static final int retSendOk = 0;
    public static final int retRevOk = 1;
    public static final int retErr = 2;

    private static final String TAG = ">>>TcpTools";
    private String charset;
    private String serIp;
    private int serPort;
    private int timeOut;
    private int callbackMode;


    private Context mCtx;
    private ExecutorService mExecutorService;
    public Socket mSocket;
    public DataOutputStream mOutStrem;
    public DataInputStream mInStrem;
    private String strRecv;

    private OnTcpCallBackListener mCallBack;
    private void setCallBack(final int ret, final Object obj) {
        if (this.mCallBack != null) {
            ((Activity) mCtx).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        mCallBack.onTcpCallBack(ret, obj);
                }
            });
        }
    }

    private static TcpTools mSlgxTcp;
    public static TcpTools getInstance() {
        if (mSlgxTcp == null) {
            mSlgxTcp = new TcpTools();
        }
        return mSlgxTcp;
    }

    public TcpTools() {
    }

    public void setServer(Context ctx, String host, int port, int timeOut, String charset, int callbackMode) {
        this.mCtx = ctx;
        this.serIp = host;
        this.serPort = port;
        this.timeOut = timeOut;
        this.charset = charset;
        this.callbackMode = callbackMode;
        this.mExecutorService = Executors.newCachedThreadPool();
    }
    public void close() {
        try {
            if (mSocket != null) mSocket.close();
            if (mOutStrem != null) mOutStrem.close();
            if (mInStrem != null) mInStrem.close();
            Log.d(TAG, "Close Tcp... OK");
        } catch (IOException e) {
            Log.e(TAG, "Close Tcp ... Err=" + e);
        }
    }
    public void open(OnTcpCallBackListener callBack) {
        this.mCallBack = callBack;
        try {
            mSocket = new Socket(serIp, serPort);
            mSocket.setSoTimeout(timeOut);
            mOutStrem = new DataOutputStream(mSocket.getOutputStream());
            mInStrem = new DataInputStream(mSocket.getInputStream());
        } catch (Exception e) {
            Log.e(TAG, "Conn Tcp Err=" + e);
        }
    }
    public void recive(final OnTcpCallBackListener callBack) {
        this.mCallBack = callBack;
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                        open(callBack);
                    if (mSocket == null) {
                        setCallBack(retErr, "尚未建立连接");
                        return;
                    }
                    if (mSocket.isClosed()) {
                        setCallBack(retErr, "连接已关闭");
                        return;
                    }
                    if (!mSocket.isConnected()) {
                        setCallBack(retErr, "连接不正常");
                        return;
                    }
                    if (mSocket.isInputShutdown()) {
                        setCallBack(retErr, "输入流已断开");
                        return;
                    }
                    byte[] bytesRev = new byte[512];
                    int len;
                    String recvString = "";
                    while ((len = mInStrem.read(bytesRev, 0, bytesRev.length)) != -1) {
                        recvString = byteToHexString(bytesRev,len);
                        setCallBack(retRevOk,recvString);
                        Log.e(TAG, "Recive Data ErrSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS=" + recvString);
                    }
                    //mInStrem.close();
                } catch (Exception e) {
                    Log.e(TAG, "Recive Data Err=" + e);
                }
            }
        });
    }
    public String byteToHexString(byte[] bytes,int length){
        String out = "";
        if (bytes!=null){
            for (int i=0;i<length;i++){
                out =out
                        +(NumToHex(bytes[i]<0?bytes[i]+256:bytes[i]).length()==1?"0"
                        +NumToHex(bytes[i]<0?bytes[i]+256:bytes[i])
                        :NumToHex(bytes[i]<0?bytes[i]+256:bytes[i])
                )+" ";
            }
        }
        return out;
    }
    public String NumToHex(int num){
        String hexString = Integer.toHexString(num);
        hexString = hexString.length()%2!=0?BuLing(hexString,1,true):hexString;
        return hexString.toUpperCase();
    }
    public String BuLing(String src,int num,Boolean isboolean){
        String outsrc = src;
        for (int i=0;i<num;i++){
            outsrc = isboolean?"0"+outsrc:outsrc+"0";
        }
        return outsrc;
    }
    public char[] getChars (byte[] bytes) {
        Charset cs = Charset.forName ("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate (bytes.length);
        bb.put (bytes);
        bb.flip ();
        CharBuffer cb = cs.decode (bb);
        return cb.array();
    }
    public void send(String strSend, OnTcpCallBackListener callBack) {
        try {
            send(strSend.getBytes(charset), callBack);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Send Data Err=" + e);
        }
    }

    public void send(final byte[] byteSend, OnTcpCallBackListener callBack) {
        this.mCallBack = callBack;
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSocket == null) {
                        setCallBack(retErr, "尚未建立连接");
                        return;
                    }
                    if (!mSocket.isConnected()) {
                        setCallBack(retErr, "连接已关闭");
                        return;
                    }
                    if (mSocket.isOutputShutdown()) {
                        setCallBack(retErr, "输出流已断开");
                        return;
                    }
                    mOutStrem.write(byteSend);
                    mOutStrem.flush();
                    //mOutStrem.close();
                    setCallBack(retSendOk, "发送成功");
                }
                catch (IOException e) {
                    Log.e(TAG, "Send Data Err=" + e);
                }
            }
        });
    }

    private String strToHexStr(String strSrc) {
        try {
            char[] chars = "0123456789ABCDEF".toCharArray();
            StringBuilder sb = new StringBuilder("");
            byte[] Bytes = strSrc.getBytes(this.charset);
            int bit;

            for (byte Byte : Bytes) {
                bit = (Byte & 0x0f0) >> 4;
                sb.append(chars[bit]);

                bit = Byte & 0x0f;
                sb.append(chars[bit]);
            }sb.append(' ');
            return sb.toString().trim();
        } catch (Exception e) {
            Log.e(TAG, "ExErr=" + e);
        }
        return " ";
    }
}
