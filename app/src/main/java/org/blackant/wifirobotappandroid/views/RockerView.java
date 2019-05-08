package org.blackant.wifirobotappandroid.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static java.lang.String.valueOf;


public class RockerView extends View implements Runnable {

    private int DEFAULT_VIEW_SIZE = 200;
    private int DEFAULT_ROCKER_RADIUS = 100;
    private int DEFAULT_BASE_RADIUS = 200;

    private Paint mPaint;
    private Point mRockerPosition;
    private Point mCenterPoint;
    private int mAreaRadius, mRockerRadius;

    private OnAngleChangeListener mOnAngleChangeListener;
    private OnShakeListener mOnShakeListener;

    private Direction tempDirection = Direction.DIRECTION_CENTER;


    // direction
    public enum Direction {
        DIRECTION_UP, // 上
        DIRECTION_DOWN, // 下
        DIRECTION_LEFT, // 左
        DIRECTION_RIGHT, // 右
        DIRECTION_CENTER // 中间
    }


    public RockerView(Context context) {
        super(context);

        // 中心点
        mCenterPoint = new Point();
        // 摇杆位置
        mRockerPosition = new Point();
    }

    public RockerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 中心点
        mCenterPoint = new Point();
        // 摇杆位置
        mRockerPosition = new Point();

        initAttribute();
    }


    private void initAttribute() {
        // 摇杆半径
        mRockerRadius = DEFAULT_ROCKER_RADIUS;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.rgb(102,145, 235));
        mPaint.setAlpha(0x7A);
        mPaint.setStrokeWidth(10);
        new Thread(this).start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth, measureHeight;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            // 具体的值和match_parent
            measureWidth = widthSize;
        } else {
            // wrap_content
            measureWidth = DEFAULT_VIEW_SIZE;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            measureHeight = heightSize;
        } else {
            measureHeight = DEFAULT_VIEW_SIZE;
        }
        Log.i("rockerview", "onMeasure: --------------------------------------");
        Log.i("rockerview", "onMeasure: widthMeasureSpec = " + widthMeasureSpec + " heightMeasureSpec = " + heightMeasureSpec);
        Log.i("rockerview", "onMeasure: widthMode = " + widthMode + "  measureWidth = " + widthSize);
        Log.i("rockerview", "onMeasure: heightMode = " + heightMode + "  measureHeight = " + widthSize);
        Log.i("rockerview", "onMeasure: measureWidth = " + measureWidth + " measureHeight = " + measureHeight);
        setMeasuredDimension(measureWidth, measureHeight);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        int cx = measuredWidth / 2;
        int cy = measuredHeight / 2;
        // 中心点
        mCenterPoint.set(cx, cy);
        // 可移动区域的半径
        mAreaRadius = (measuredWidth <= measuredHeight) ? cx : cy;
        // 摇杆位置
        if (0 == mRockerPosition.x || 0 == mRockerPosition.y) {
            mRockerPosition.set(mCenterPoint.x, mCenterPoint.y);
        }

        // the base circle
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, DEFAULT_BASE_RADIUS, mPaint);
        // the rocker circle
        canvas.drawCircle(mRockerPosition.x, mRockerPosition.y, mRockerRadius, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 按下
                // 回调 开始
                callBackStart();
            case MotionEvent.ACTION_MOVE:// 移动
                float moveX = event.getX();
                float moveY = event.getY();
                mRockerPosition = getRockerPositionPoint(mCenterPoint, new Point((int) moveX, (int) moveY), mAreaRadius, mRockerRadius);
                moveRocker(mRockerPosition.x, mRockerPosition.y);
                break;
            case MotionEvent.ACTION_UP:// 抬起
            case MotionEvent.ACTION_CANCEL:// 移出区域
                // 回调 结束
                callBackFinish();
                moveRocker(mCenterPoint.x, mCenterPoint.y);
                break;
        }
        return true;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while(!Thread.currentThread().isInterrupted()) {
            try{
                Thread.sleep(50);
            } catch(InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            // 异步重绘
            postInvalidate();
        }
    }


    /**
     * 获取摇杆实际要显示的位置（点）
     *
     * @param centerPoint  中心点
     * @param touchPoint   触摸点
     * @param regionRadius 摇杆可活动区域半径
     * @param rockerRadius 摇杆半径
     * @return 摇杆实际显示的位置（点）
     */
    private Point getRockerPositionPoint(Point centerPoint, Point touchPoint, float regionRadius, float rockerRadius) {
        // 两点在X轴的距离
        float lenX = (float) (touchPoint.x - centerPoint.x);
        // 两点在Y轴距离
        float lenY = (float) (touchPoint.y - centerPoint.y);
        // 两点距离
        float lenXY = (float) Math.sqrt((double) (lenX * lenX + lenY * lenY));
        // 计算弧度
        double radian = Math.acos(lenX / lenXY) * (touchPoint.y < centerPoint.y ? -1 : 1);
        // 计算角度
        double angle = radian2Angle(radian);

        // 回调 返回参数
        callBack(angle);

        if (lenXY + rockerRadius <= regionRadius) { // 触摸位置在可活动范围内
            return touchPoint;
        } else { // 触摸位置在可活动范围以外
            // 计算要显示的位置
            int showPointX = (int) (centerPoint.x + (regionRadius - rockerRadius) * Math.cos(radian));
            int showPointY = (int) (centerPoint.y + (regionRadius - rockerRadius) * Math.sin(radian));
            return new Point(showPointX, showPointY);
        }
    }

    /**
     * 移动摇杆到指定位置
     */
    private void moveRocker(float x, float y) {
        mRockerPosition.set((int) x, (int) y);
        invalidate();
    }

    /**
     * 弧度转角度
     *
     * @param radian 弧度
     * @return 角度[0, 360)
     */
    private double radian2Angle(double radian) {
        double tmp = Math.round(radian / Math.PI * 180);
        return tmp >= 0 ? tmp : 360 + tmp;
    }

    /**
     * 回调开始
     */
    private void callBackStart() {
        tempDirection = Direction.DIRECTION_CENTER;
        if (null != mOnShakeListener) {
            mOnShakeListener.onStart();
        }
    }

    /**
     * 回调返回参数
     *
     * @param angle 摇动角度
     */
    private void callBack(double angle) {
        Log.i("rockerview", "angel:" + valueOf(angle));
        if (null != mOnAngleChangeListener) {
            mOnAngleChangeListener.angle(angle);
        }
        if (null != mOnShakeListener) {
            if (((angle > 315 && angle <= 360) || (angle > 0 && angle <= 45)) && (tempDirection != Direction.DIRECTION_RIGHT)) {
                // 右
                tempDirection = Direction.DIRECTION_RIGHT;
                mOnShakeListener.direction(Direction.DIRECTION_RIGHT);
            } else if ((angle > 225 && angle <= 315) && (tempDirection != Direction.DIRECTION_UP)) {
                // 上
                tempDirection = Direction.DIRECTION_UP;
                mOnShakeListener.direction(Direction.DIRECTION_UP);
            } else if ((angle > 45 && angle <= 135) && (tempDirection != Direction.DIRECTION_DOWN)) {
                // 下
                tempDirection = Direction.DIRECTION_DOWN;
                mOnShakeListener.direction(Direction.DIRECTION_DOWN);
            } else if ((angle > 135 && angle <= 225) && (tempDirection != Direction.DIRECTION_LEFT)) {
                // 左
                tempDirection = Direction.DIRECTION_LEFT;
                mOnShakeListener.direction(Direction.DIRECTION_LEFT);
            }
        }
    }

    /**
     * 回调结束
     */
    private void callBackFinish() {
        tempDirection = Direction.DIRECTION_CENTER;
        mOnShakeListener.direction(Direction.DIRECTION_CENTER);
        if (null != mOnAngleChangeListener) {
            mOnAngleChangeListener.onFinish();
        }
        if (null != mOnShakeListener) {
            mOnShakeListener.onFinish();
        }
    }




    /**
     * 添加摇杆摇动角度的监听
     *
     * @param listener 回调接口
     */
    public void setOnAngleChangeListener(OnAngleChangeListener listener) {
        mOnAngleChangeListener = listener;
    }

    /**
     * 添加摇动的监听
     *
     * @param listener      回调
     */
    public void setOnShakeListener(OnShakeListener listener) {
        mOnShakeListener = listener;
    }

    /**
     * 摇动方向监听接口
     */
    public interface OnShakeListener {
        // 开始
        void onStart();

        /**
         * 摇动方向
         *
         * @param direction 方向
         */
        void direction(Direction direction);

        // 结束
        void onFinish();
    }


    /**
     * 摇动角度的监听接口
     */
    public interface OnAngleChangeListener {
        // 开始
        void onStart();

        /**
         * 摇杆角度变化
         *
         * @param angle 角度[0,360)
         */
        void angle(double angle);

        // 结束
        void onFinish();
    }

}
