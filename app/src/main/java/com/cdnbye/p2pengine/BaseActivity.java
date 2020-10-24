package com.cdnbye.p2pengine;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class BaseActivity extends Activity {
    /** 手势监听 */
    GestureDetector mGestureDetector;
    /** 是否需要监听手势关闭功能 */
    private boolean mNeedBackGesture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initGestureDetector();
    }

    private void initGestureDetector() {
        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(getApplicationContext(),
                    new BackGestureListener(this));
        }
    }


    /* (non-Javadoc)
     * touch手势分发
     * @see android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        if (mNeedBackGesture) {
            return mGestureDetector.onTouchEvent(ev)
                    || super.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    /*
     * 设置是否进行手势监听
     */
    public final void setNeedBackGesture(boolean mNeedBackGesture) {
        this.mNeedBackGesture = mNeedBackGesture;
    }

}