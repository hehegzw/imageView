package gzw.cn.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by gzw on 2016/4/26.
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public static final int DIR_TOP = 1;
    public static final int DIR_RIGHT = 2;
    public static final int DIR_BOTTOM = 3;
    public static final int DIR_LEFT = 4;
    private SurfaceHolder holder;
    private MyThread myThread;
    private boolean isRun;
    private int oriCenterX;//图片原始中心坐标
    private int oriCenterY;//图片原始中心坐标
    private int indexdir = DIR_TOP;//移动方向
    private int speed;//移动速度
    private int imageId;//图片资源
    private Bitmap mBitmap;//图片

    public MySurfaceView(Context context) {
        this(context, null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        holder = getHolder();
        holder.addCallback(this);
        myThread = new MyThread(holder);
        speed = 1;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRun = true;
        myThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private class MyThread extends Thread {
        private SurfaceHolder holder;

        public MyThread(SurfaceHolder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            super.run();
            BitmapData bd = new BitmapData(imageId);
            ViewData vd = new ViewData();
            DisBounds db = new DisBounds(bd, vd);
            if (!db.canMove) isRun = false;
            initDraw(bd, vd);
            int offset = 0;//偏移量
            while (isRun) {
                Canvas c = holder.lockCanvas();
                synchronized (holder) {
                    switch (indexdir) {
                        case DIR_TOP:
                            if (db.top > 0 && offset < db.top) {
                                offset += speed;
                            } else {
                                indexdir = DIR_RIGHT;
                                offset = 0;
                            }
                            c.drawBitmap(bd.bitmap, oriCenterX, oriCenterY-offset, new Paint());
                            break;
                        case DIR_RIGHT:
                            if (db.right > 0 && offset < db.right) {
                                offset += 1;
                            } else {
                                indexdir = DIR_BOTTOM;
                                offset = 0;
                            }
                            c.drawBitmap(bd.bitmap, oriCenterX+offset, oriCenterY, new Paint());
                            break;
                        case DIR_BOTTOM:
                            if (db.bottom > 0 && offset < db.bottom) {
                                offset += 1;
                            } else {
                                indexdir = DIR_LEFT;
                                offset = 0;
                            }
                            c.drawBitmap(bd.bitmap, oriCenterX, oriCenterY+offset, new Paint());
                            break;
                        case DIR_LEFT:
                            if (db.left > 0 && offset < db.left) {
                                offset += 1;
                            } else {
                                indexdir = DIR_TOP;
                                offset = 0;
                            }
                            c.drawBitmap(bd.bitmap, oriCenterX-offset, oriCenterY, new Paint());
                            break;
                    }
                    holder.unlockCanvasAndPost(c);//结束锁定画图，并提交改变。
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    //第一次画bitmap
    private void initDraw(BitmapData bd, ViewData vd) {
        oriCenterX = (vd.center.x  - bd.center.x);
        oriCenterY = (vd.center.y  - bd.center.y);
        Canvas c = holder.lockCanvas();//锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了。
        c.drawBitmap(bd.bitmap, oriCenterX, oriCenterY, new Paint());
        Log.d("viewwh", getHeight() + " " + getWidth());
        holder.unlockCanvasAndPost(c);//结束锁定画图，并提交改变。
    }
    private class BitmapData {
        public int width;
        public int height;
        public Bitmap bitmap;
        public Point center;

        public BitmapData(int id) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = 1;
            bitmap = BitmapFactory.decodeResource(getResources(), id, opts);
            width = opts.outWidth;
            height = opts.outHeight;
            center = new Point(width / 2, height / 2);
        }
    }

    private class ViewData {
        public int width;
        public int height;
        public Point center;

        public ViewData() {
            width = getWidth();
            height = getHeight();
            center = new Point(width / 2, height / 2);
        }
    }
    //计算bitmap和view各边的距离差
    private class DisBounds{
        public int top;
        public int right;
        public int bottom;
        public int left;
        public boolean canMove;//是否所有边差都小于0

        public DisBounds(BitmapData bd, ViewData vd) {
            right = (bd.width - vd.width)/2;
            left = (bd.width - vd.width)/2;
            top = (bd.height - vd.height)/2;
            bottom = (bd.height - vd.height)/2;
            if(right<=0&&top<=0){
                canMove = false;
            }else{
                canMove = true;
            }
        }
    }
}
