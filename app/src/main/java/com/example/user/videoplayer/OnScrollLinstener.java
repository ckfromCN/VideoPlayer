package com.example.user.videoplayer;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public  class OnScrollLinstener implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private boolean isCancel;

    public OnScrollLinstener(Context context, View targetView, OnScrolled onScrolled) {
        this.context = context;
        viewWidth = targetView.getWidth();
        viewHeight = targetView.getHeight();
        this.targetView = targetView;
        targetView.setOnTouchListener(this);
        compat = new GestureDetectorCompat(context, this);
        this.onScrolled = onScrolled;
    }
    public interface OnScrolled {
        void horizontalScroll(float distanceX);

        void rightVerticalScroll(float distanceY);

        void leftVerticalScroll(float distanceY);
        
        void onTouch(View v, MotionEvent event);
    }

    private OnScrolled onScrolled;
    private Context context;

    private boolean firstScroll;

    private int GESTURE_FLAG;

    private View targetView;

    private int viewWidth, viewHeight;

    public static final int RIHGT_SCROLL = 0x12345678;

    public static final int LEFT_SCROLL = 0x22345687;

    public static final int BOTTOM_SCROLL = 0x32345458;

    private GestureDetectorCompat compat ;


    @Override
    public boolean onDown(MotionEvent e) {
        firstScroll = true;
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public void cancelScroll(){
        isCancel=true;
    }
    
    public void reconverScroll(){
        isCancel=false;
    }
    
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isCancel) return false;
        float mOldX = e1.getX();
        Log.e("onScroll","fristScroll : "+firstScroll+"----GESTURE_FLAG : "+GESTURE_FLAG+"----viewWidth : "+viewWidth);
        if (firstScroll) {
            if (Math.abs(distanceY) <= Math.abs(distanceX)) {
                GESTURE_FLAG = BOTTOM_SCROLL;
            } else {
                if (mOldX > viewWidth * 2.5 / 5) {
                    GESTURE_FLAG = RIHGT_SCROLL;
                } else if (mOldX < viewWidth * 2.5 / 5) {
                    GESTURE_FLAG = LEFT_SCROLL;
                }
            }
        }
        
        switch (GESTURE_FLAG) {
            case BOTTOM_SCROLL:
                if (Math.abs(distanceY) <= Math.abs(distanceX)) {
                    onScrolled.horizontalScroll(distanceX);
                }
                break;
            case RIHGT_SCROLL:
                if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                    onScrolled.rightVerticalScroll(distanceY);
                }
                break;
            case LEFT_SCROLL:
                if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                    onScrolled.leftVerticalScroll(distanceY);
                }
                break;

        }
        firstScroll=false;
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
    public  boolean  onTouch(View v, MotionEvent event) {
        onScrolled.onTouch(v,event);
        return compat.onTouchEvent(event);
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}
