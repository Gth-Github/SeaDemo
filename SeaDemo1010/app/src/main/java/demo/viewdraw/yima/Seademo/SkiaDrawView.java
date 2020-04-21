package demo.viewdraw.yima.Seademo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;

import java.util.List;
import java.util.Timer;

import yimamapapi.skia.*;

public class SkiaDrawView extends View {
    public YimaLib mYimaLib;

    public Bitmap fSkiaBitmap;
    private int curDisplayCategory;
    private int mLastX, mLastY;
    private int mCurrX, mCurrY;

    private int mLastX0, mLastY0, mLastX1, mLastY1;
    private int mCurrX0, mCurrY0, mCurrX1, mCurrY1;

    private Context mContext;

    public boolean bNormalDragMapMode; //是否使用移动贴图模式
    private boolean bDragingMap;//是否真在进行拽图
    private int dragStartPointX, dragStartPointY;//拽动的起始位置
    private int dragMapOffsetPointX, dragMapOffsetPointY; //移动拽图的X/Y偏移量

    private double pinchScaleFactor;//MotionEvent.ACTION_DOWN;比例尺变化因子
    private int pasteWidth, pasteHeight; //pinch时贴图宽度和高度
    private M_POINT scrnCenterPointGeo;

    public  boolean bOpeningMap;

    //test
    private Timer timer;
    private M_POINT ownShipPo;

    private List<S52LayerDisplayInfo> m_arrS52LayerDisplayInfo;

    public SkiaDrawView(Context ctx, AttributeSet attr) {
        super(ctx, attr);
        mYimaLib = new YimaLib();

        //mYimaLib.SetIfYmcFileNeedEncrypt(true);
       // mYimaLib.SetYMCFileEncryptKey(-1);
        //String  strWorkDirPath =  "/data/data/demo.viewdraw.yima.yimasimpledemo/files/WorkDir";
        String strWorkDirPath = "/data/data/"+ ctx.getPackageName()+ "/files/WorkDir";

        mYimaLib.Create(strWorkDirPath);
        String DeviceId = mYimaLib.GetDeviceIDForLicSvr();
        mYimaLib.Init();//初始化，传入WorkDir初始化目录地址
        mYimaLib.SetIfUseS52LayerDisplaySet(false);
        mYimaLib.SetLoadMapScaleFactor(20);
        mContext = ctx;
        bNormalDragMapMode = true;
        curDisplayCategory = 2;//monal;
        bOpeningMap = false;

        ownShipPo = new M_POINT();
        ownShipPo.x = 1210000000;
        ownShipPo.y = 320000000;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        fSkiaBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //String  strTtfFilePath =  "/data/data/demo.viewdraw.yima.yimasimpledemo/files/WorkDir/DroidSansFallback.ttf";
        String strTtfFilePath = "/data/data/"+ mContext.getPackageName()+ "/files/WorkDir/DroidSansFallback.ttf";
        mYimaLib.RefreshDrawer(fSkiaBitmap, strTtfFilePath);//刷新绘制器，需要传入字体文件地址，用户可以自己修改为别的字体
        mYimaLib.OverViewLibMap(0);//概览第一幅图
        mYimaLib.SetDisplayCategory(2);
//        mYimaLib.SetIfShowSoundingAndMinMaxSound(true, 0, 20);
    }

    @Override
    public void onDraw(Canvas canvas) {
        float curScale = mYimaLib.GetCurrentScale();
//        if (curScale <= 10000) {
//            if (curDisplayCategory != 3) {
//                curDisplayCategory = 3;
//                mYimaLib.SetDisplayCategory(2);
//            }
//        } else if (curScale <= 100000) {
//            if (curDisplayCategory != 2) {
//                curDisplayCategory = 2;
//                mYimaLib.SetDisplayCategory(1);
//            }
//        } else {
//            if (curDisplayCategory != 1) {
//                curDisplayCategory = 1;
//                mYimaLib.SetDisplayCategory(0);
//            }
//        }
        long startTime = System.currentTimeMillis();
        mYimaLib.ViewDraw(fSkiaBitmap, null, null);

        canvas.drawBitmap(fSkiaBitmap, 0, 0, null);

        //地理坐标转屏幕坐标 接口mYimaLib.getScrnPoFromGeoPo()
        long endTime = System.currentTimeMillis();
        Paint paint = new Paint();
        paint.setTextSize(28);
        paint.setARGB(255, 255, 0, 0);
        canvas.drawText(String.valueOf(endTime - startTime), 50, 50, paint);


    }

    String tag = null;
    @Override
    //手势滑动
    public boolean onTouchEvent(MotionEvent event) {
        if(bOpeningMap)  return true;
        mCurrX = mLastX;
        mCurrY= mLastY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(tag, "down");
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();

                if(bNormalDragMapMode) {
                    bDragingMap = true;//拽动起始
                    dragStartPointX = mLastX;
                    dragStartPointY = mLastY;
                    pasteWidth = fSkiaBitmap.getWidth();
                    pasteHeight = fSkiaBitmap.getHeight();
                    pinchScaleFactor = 1;
                    scrnCenterPointGeo = mYimaLib.getGeoPoFromScrnPo(fSkiaBitmap.getWidth() / 2, fSkiaBitmap.getHeight() / 2);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(tag, "move");
                mLastX =  (int) event.getX();
                mLastY =  (int) event.getY();
                int pointCount = event.getPointerCount();

                if(pointCount >= 2)//pinch-->随手指放大缩小
                {
                    Log.i("Pinch", "into");
                    if(bNormalDragMapMode) {
                        bDragingMap = false;
                        dragMapOffsetPointX = dragMapOffsetPointY = 0;
                    }
                    int painterIndex0 = event.findPointerIndex(0);
                    int painterIndex1 = event.findPointerIndex(1);
                    if(painterIndex0 == -1 || painterIndex1 == -1){
                        break;
                    }
                    Log.i("Pinch", "painterIndex0:" + String.valueOf(painterIndex0) + ",painterIndex1:" + String.valueOf(painterIndex1));
                    if((mLastX0 == 0) || (mLastY0 == 0) || ( mLastX1 == 0) || (  mLastY1 == 0) )
                    {
                        mLastX0 = (int)event.getX(painterIndex0);
                        mLastY0 = (int)event.getY(painterIndex0);
                        mLastX1 = (int)event.getX(painterIndex1);
                        mLastY1 = (int)event.getY(painterIndex1);
                        invalidate();
                        break;
                    }
                    mCurrX0 = (int)event.getX(painterIndex0);
                    mCurrY0 = (int)event.getY(painterIndex0);
                    mCurrX1 = (int)event.getX(painterIndex1);
                    mCurrY1 = (int)event.getY(painterIndex1);
                    double d1 = Math.sqrt(Math.pow(mLastX0 - mLastX1, 2) + Math.pow(mLastY0 - mLastY1, 2));
                    double d2 = Math.sqrt(Math.pow(mCurrX0 - mCurrX1, 2) + Math.pow(mCurrY0 - mCurrY1, 2));
                    double currentScaleFactor = d2 / d1;
                    if(currentScaleFactor == 1.0)
                        break;
                    if(bNormalDragMapMode) {
                        pasteWidth = (int) (pasteWidth * currentScaleFactor);
                        pasteHeight = (int) (pasteHeight * currentScaleFactor);
                        pinchScaleFactor = pinchScaleFactor * currentScaleFactor;
                        int dstOffsetX = (fSkiaBitmap.getWidth() - pasteWidth) / 2;
                        int dstOffsetY = (fSkiaBitmap.getHeight() - pasteHeight) / 2;
                        Log.i("Pinch", "pasteWidth:" + String.valueOf(pasteWidth) + ",pasteHeight:" + String.valueOf(pasteHeight) + ",dstOffsetX:" + String.valueOf(dstOffsetX) + ",dstOffsetY:" + String.valueOf(dstOffsetY));
                        mYimaLib.DrawScaledMap(dstOffsetX, dstOffsetY, pasteWidth, pasteHeight);
                    }
                    else {
                        mYimaLib.SetCurrentScale(mYimaLib.GetCurrentScale() / (float) currentScaleFactor);//设置比例尺
                    }
                    mLastX0 = (int)event.getX(painterIndex0);
                    mLastY0 = (int)event.getY(painterIndex0);
                    mLastX1 = (int)event.getX(painterIndex1);
                    mLastY1 = (int)event.getY(painterIndex1);
                    invalidate();
                    break;
                }

                int iDragX = mLastX - mCurrX;
                int iDragY = mLastY - mCurrY;
                if((iDragX==0)&&(iDragY==0))
                {
                    break;
                }
                if(bNormalDragMapMode && bDragingMap)  {
                    dragMapOffsetPointX = iDragX;//mLastX - dragStartPointX;
                    dragMapOffsetPointY = iDragY;//mLastY - dragStartPointY;//curMouseScrnPo - dragStartPoint;
                    mYimaLib.PasteToScrn(dragMapOffsetPointX, dragMapOffsetPointY);
                }
                else  mYimaLib.SetMapMoreOffset(iDragX, iDragY);//移动设置偏移
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mLastX0 = mLastY0 =  mLastX1 =  mLastY1 = 0;

                if(bNormalDragMapMode){//留白模式
                    if( bDragingMap)//留白拖动结束
                        mYimaLib.SetMapMoreOffset((int)event.getX() - dragStartPointX, (int)event.getY() - dragStartPointY);
                    else{//pinch拖动结束
                        mYimaLib.CenterMap(scrnCenterPointGeo.x, scrnCenterPointGeo.y);
                        mYimaLib.SetCurrentScale( mYimaLib.GetCurrentScale() / (float) pinchScaleFactor);
                    }
                    bDragingMap = false;//拽动结束
                }
                invalidate();
                Log.i(tag, "up");
                break;
            default:
                break;
        }
        return true;
    }

}

