# AndroidOptimize
## Android大图加载优化方案
### 1、自定义View加载图片
### 2、设置setImage方法设置图片，测量图片宽高
### 3、调用requestLayout()重新加载图片
### 4、在onMeasure(...)方法中对比图片宽高和View的宽高，
### 5、定义使用区域解码器、手势对象、缩放手势，缩放因子等对象
    //区域解码器
    private BitmapRegionDecoder mDecode;
	 //手势对象
    private GestureDetector mGestureDetector;
    //使用inJustDecodeBounds方法，只加载部分区域来获取图片宽高
     mOptions.inJustDecodeBounds=true;
    //缩放功能
    ScaleGestureDetector mScaleGestureDetector;
    //设置图片格式:rgb565
    mOptions.inPreferredConfig= Bitmap.Config.RGB_565;
    //滑动帮助类
    private Scroller mScroller;
    //需要显示的区域
    private Rect mRect;
	//图片缩放因子
    private float mScale;
### 6、在onDraw(...)做对应的缩放和区域滑动


![bigView](https://github.com/Chen-Yi-Ran/AndroidOptimize/assets/76609982/5340737e-98f9-471c-89ad-0fee6e8ee0ab)
