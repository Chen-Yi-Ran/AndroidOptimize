package com.example.androidoptimize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class BigView extends View implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private static final String TAG = "BigView";
    private Rect mRect;
    private BitmapFactory.Options mOptions;
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private int mImageWidth;
    private int mImageHeight;
    private BitmapRegionDecoder mDecoder;
    private int mViewWidth;
    private int mViewHeight;
    private Bitmap mBitmap;
    private float mScaleX;
    private float mScaleY;

    public BigView(Context context) {
        this(context,null);
    }

    public BigView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BigView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public BigView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //第一步 设置BigView需要的成员变量
        //设置一个矩形区域(矩形区域框定)
        mRect = new Rect();
        //用于内存复用(设置编码格式)
        mOptions = new BitmapFactory.Options();
        //手势支持
        mGestureDetector = new GestureDetector(context, this);
        //滚动类
        mScroller = new Scroller(context);
        //触摸时触发事件
        setOnTouchListener(this);
    }


    //第二步设置图片
    public void setImage(InputStream is){
        //获取图片的宽和高
        //此时不能将整张图片加载进来，这样内存复用无意义，需要使用inJustDecodeBounds方法，只加载部分区域来获取图片宽高
        mOptions.inJustDecodeBounds=true;
        //将is传进去解码就能获取到图片的宽和高
        BitmapFactory.decodeStream(is,null,mOptions);

        mImageWidth = mOptions.outWidth;
        mImageHeight = mOptions.outHeight;
        Log.d(TAG, "mImageWidth: "+mImageWidth);
        Log.d(TAG, "mImageHeight: "+mImageHeight);
        //开启内存复用
        mOptions.inMutable=true;

        //设置图片格式:rgb565
        mOptions.inPreferredConfig= Bitmap.Config.RGB_565;

        //用完需要关闭
        mOptions.inJustDecodeBounds=false;

        //区域解码器
        try {
            mDecoder = BitmapRegionDecoder.newInstance(is, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestLayout();
    }

    //第三步 加载图片
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();
        Log.d(TAG, "mViewWidth: "+mViewWidth);
        Log.d(TAG, "mViewHeight: "+mViewHeight);
        //绑定图片加载区域
        //上边界为0
        mRect.top=0;
        //左边界为0
        mRect.left=0;
        //右边界为图片的宽度
        mRect.right=mImageWidth;
        //下边界为view的高度
        mRect.bottom=mViewHeight;
    }

    //第四步 画图
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mDecoder==null){
            Log.d(TAG, "null: ---->");
            return;
        }
        //内存复用
        //复用inBitmap这块的内存
        mOptions.inBitmap=mBitmap;
        Log.d(TAG, "mOptions.inBitmap--->"+mOptions.inBitmap);
        mBitmap=mDecoder.decodeRegion(mRect,mOptions);

        //计算缩放因子
        mScaleX = mViewWidth / (float) mImageWidth;
        Log.d(TAG, "mScaleX: "+mScaleX);
        mScaleY = mViewHeight / (float) mImageHeight;
        Log.d(TAG, "mScaleY: "+mScaleY);
        //得到矩阵缩放
        Matrix matrix = new Matrix();
        matrix.setScale(mScaleX, mScaleX);//如果matrix.setScale(mScaleX, mScaleY)则图片会在充满在当前的view的x和y轴
        canvas.drawBitmap(mBitmap,matrix,null);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
