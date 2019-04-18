package org.blackant.wifirobotappandroid.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.blackant.wifirobotappandroid.R;

/**
 * author: jiojio
 */
public class RockerView extends View implements Runnable{

    private Canvas canvas;

    private Paint mPaint;

    private Context mContext;

    private boolean mFlag = true;

    public double mRad, mAngle;

    private int mRockCentX, mRockCentY, mRockRadius;

    private int mBaseCentX, mBaseCentY, mBaseRadius;

    public int mLogicType = 0, mLogicStatus;

    private final int LOGIC_STOP   = 0x00;

    private final int LOGIC_UP     = 0x01;

    private final int LOGIC_DOWN   = 0x02;

    private final int LOGIC_LEFT  = 0x03;

    private final int LOGIC_RIGHT = 0x04;

    public RockerView(Context context) {

        super(context);

    }

    public RockerView(Context context, AttributeSet attrs) {

        super(context, attrs);

        mContext = context;

        initPaint();

    }

    private void initPaint() {

        mRockCentX  = mBaseCentX = 300;

        mRockCentY  = mBaseCentY = 300;

        mRockRadius = 100;

        mBaseRadius = 200;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setColor(Color.rgb(102,145, 235));

        mPaint.setAlpha(0x7A);

        mPaint.setStrokeWidth(10);

        new Thread(this).start();

    }

    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawCircle(mBaseCentX, mBaseCentY, mBaseRadius, mPaint);

        canvas.drawCircle(mRockCentX, mRockCentY, mRockRadius, mPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /* Reset rocker when touch up */
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mRockCentX = mBaseCentX;
            mRockCentY = mBaseCentY;
            mLogicType = LOGIC_STOP;
        } else {
//			setRockPosition(event.getX(), event.getY());
            float pointDx = event.getX() - mBaseCentX;
            float pointDy = event.getY() - mBaseCentY;
            Log.i("RockView","X: " + event.getX() + " Y: " + event.getY());
            double pointR = Math.sqrt(pointDx * pointDx + pointDy * pointDy);
            Log.i("RockView","pointR: " + pointR);
            if ( pointR <= mBaseRadius ) {
                mRockCentX = (int) event.getX();
                mRockCentY = (int) event.getY();
            }
            else {
                mRockCentX = mBaseCentX + (int) (mBaseRadius * pointDx / pointR);
                mRockCentY = mBaseCentY + (int) (mBaseRadius * pointDy / pointR);
                Log.i("RockView","mRockCentX: " + mRockCentX + "mRockCentY: " + mRockCentY);
                mRad = Math.acos(pointDx / pointR);
                Log.i("RockView","mRad: " + mRad);
                if ( event.getY() > mRockCentY ) {
                    mRad = -mRad;
                }
                Log.i("RockView","after mRad: " + mRad);
                mAngle = Math.toDegrees(mRad);
                mLogicType = setLogicType(mAngle);
            }
            if (pointR <= mRockRadius) {
                mLogicType = LOGIC_STOP;
                Log.i("RockerView", "STOP++++");
            }


        }
        return true;
    }

    @Override
    public void run() {
        // TODOAuto-generated method stub
        while(!Thread.currentThread().isInterrupted()) {
            try{
                Thread.sleep(50);
            } catch(InterruptedException e) {
                // TODO: handle exception
                Thread.currentThread().interrupt();
            }
            //ʹ��postInvalidate ����ֱ�����߳��и��½���
            postInvalidate();
        }
    }

    /**
     * Calculate logic action type
     * @param angle - rocker angle -180~180
     * @return type 0~5 on success, -1 on failure
     */
    int setLogicType(double angle) {
        Log.i("RockerView", "angle" + angle);
        if ((angle > 0 && angle <= 60) || (angle > -60 && angle <= 0)) {
            mLogicType = LOGIC_RIGHT;
            Log.i("RockerView", "RIGHT++++");
        }
        else if (angle > 60 && angle <= 120) {
            mLogicType = LOGIC_UP;
            Log.i("RockerView", "UP++++");
        }

        else if (angle > -120 && angle <= -60) {
            mLogicType = LOGIC_DOWN;
            Log.i("RockerView", "DOWN++++");
        }
        else if ((angle > 120 && angle <= 180) || (angle > -180 && angle <= -120)) {
            mLogicType = LOGIC_LEFT;
            Log.i("RockerView", "LEFT++++");
        }
        return mLogicType;
    }

}
