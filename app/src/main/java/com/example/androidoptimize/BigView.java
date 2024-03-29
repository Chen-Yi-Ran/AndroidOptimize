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
      //  Log.d(TAG, "mImageWidth: --->"+mImageWidth);
      //  Log.d(TAG, "mImageHeight: --->"+mImageHeight);
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
        Log.d(TAG, "执行onMeasure: ");
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
        Log.d(TAG, "执行onDraw: ");
        if(mDecoder==null){
         //   Log.d(TAG, "null: ---->");
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
      //  Log.d(TAG, "mScaleX: "+mScaleX);
        mScaleY = mViewHeight / (float) mImageHeight;
      //  Log.d(TAG, "mScaleY: "+mScaleY);
        //得到矩阵缩放
        Matrix matrix = new Matrix();
        matrix.setScale(mScaleX, mScaleX);//如果matrix.setScale(mScaleX, mScaleY)则图片会在充满在当前的view的x和y轴
        canvas.drawBitmap(mBitmap,matrix,null);
    }

    //第五步 处理点击事件
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "执行onTouch: ");
        //将Touch事件传递给手势
        return mGestureDetector.onTouchEvent(event);
    }

    //第六步 处理手势按下事件

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG, "执行onDown: ");
        //如果滑动没有停止就 强制停止
        if(!mScroller.isFinished()){
            mScroller.forceFinished(true);
        }
        //将事件进行传递，接收后续事件
        //因为在GestureDetector中，onDown方法是用于监听手指按下事件的，如果不返回true消费该事件，
        // GestureDetector就不会将后续的事件传递给其他的方法进行处理，
        // 包括滑动事件。因此，如果要实现按下手指后进行滑动图片的效果，需要在onDown方法中返回true进行消费。
        return true;
    }

    //第七步 处理滑动事件(手势)指手势的拖动
    //e1 开始事件
    //e2 即时事件也就是滑动时
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "执行onScroll: ");

        //上下滑动时，直接改变Rect的显示区域
        mRect.offset(0,(int) distanceY);//上下滑动只需要改变Y轴
        Log.d(TAG, "onScroll----->distanceY--->: "+distanceY);
        Log.d(TAG, "onScroll----->mRect.bottom--->: "+mRect.bottom);
        //判断到顶和到底的情况
        if(mRect.bottom>mImageHeight){//滑到最底
            Log.d(TAG, "onScroll: 经来了");
            mRect.bottom=mImageHeight;
            mRect.top=mImageHeight-mViewHeight;
        }
      //  Log.d(TAG, "mRect.top: "+mRect.top);
        if(mRect.top<0){//滑到最顶
            mRect.top=0;
            mRect.bottom=mViewHeight;
        }
        invalidate();
        return true;
    }


    //第八步 处理惯性问题(手势)指手势的滑动
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
       Log.d(TAG, "执行onFling: ");
        //velocityY表示Y轴的惯性值，startX和startY为滑动的开始位置,minY和maxY为滑动距离的最小值和最大值
      //  Log.d(TAG, "onFling: mRect.top -->"+mRect.top);
        mScroller.fling(0,mRect.top,0,(int) -velocityY,0,0,0,mImageHeight-mViewHeight);
     //   Log.d(TAG, "velocityY: "+velocityY);
     //   Log.d(TAG, "mRect.top");
        return false;
    }

    //该方法可以获取当前的滚动值
    @Override
    public void computeScroll() {
        super.computeScroll();
       Log.d(TAG, "执行computeScroll: ");
        //如果没有滚动，直接返回即可
        if(mScroller.isFinished()){
            return;
        }
        //如果已经滚动到新位置返回true
        if(mScroller.computeScrollOffset()){
            mRect.top=mScroller.getCurrY();
            Log.d(TAG, "computeScroll:mRect.top "+mRect.top);
            mRect.bottom=mRect.top+mViewHeight;//底部边框等于更新的top位置加上
            Log.d(TAG, "computeScroll:mRect.bottom "+mRect.bottom);
        }
        invalidate();
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

}
