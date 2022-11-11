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
        Log.d(TAG, "mImageWidth: --->"+mImageWidth);
        Log.d(TAG, "mImageHeight: --->"+mImageHeight);
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
        Log.d(TAG, "mViewWidth: --->"+mViewWidth);
        Log.d(TAG, "mViewHeight:---> "+mViewHeight);
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
        //复用inBitmap这块的内存(每次滚动重新绘制都会复用这块内存，达到内存复用)
        mOptions.inBitmap=mBitmap;
        //Log.d(TAG, "mOptions.inBitmap--->"+mOptions.inBitmap);
        mBitmap=mDecoder.decodeRegion(mRect,mOptions);
        //Log.d(TAG, "mBitmap--->"+mBitmap);
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

    //第五步 处理点击事件
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: ");
        //将Touch事件传递给手势
        return mGestureDetector.onTouchEvent(event);
    }

    //第六步 处理手势按下事件
    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG, "onDown: ");
        //如果滑动没有停止就 强制停止
        if(!mScroller.isFinished()){
            mScroller.forceFinished(true);
        }

        //将事件进行传递，接收后续事件
        return true;
    }

    //第七步 处理滑动事件
    //e1 开始事件
    //e2 即时事件也就是滑动时
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onScroll----->distanceY--->: "+distanceY);
        Log.d(TAG, "onScroll----->mImageHeight--->: "+mImageHeight);
        //上下滑动时，直接改变Rect的显示区域
        mRect.offset(0,(int) distanceY);//上下滑动只需要改变Y轴
        //判断到顶和到底的情况
        if(mRect.bottom>mImageHeight){//滑到最底
            mRect.bottom=mImageHeight;
            mRect.top=mImageHeight-mViewHeight;
        }
        Log.d(TAG, "mRect.top: "+mRect.top);
        if(mRect.top<0){//滑到最顶
            mRect.top=0;
            mRect.bottom=mViewHeight;
        }
        invalidate();
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
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }


}
