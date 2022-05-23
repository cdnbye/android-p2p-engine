package com.cdnbye.demo

import android.app.Activity
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * @author imgod
 * 自定义一个类来实现OnGestureListener 手势监听
 */
class BackGestureListener
    (private val mc: Activity) : GestureDetector.OnGestureListener {
    override fun onDown(arg0: MotionEvent): Boolean {
        return false
    }

    override fun onFling(
        arg0: MotionEvent, arg1: MotionEvent, arg2: Float,
        arg3: Float
    ): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    override fun onLongPress(arg0: MotionEvent) {
        // TODO Auto-generated method stub
    }

    override fun onScroll(
        arg0: MotionEvent, arg1: MotionEvent, arg2: Float,
        arg3: Float
    ): Boolean {
        // 已经往右滑动超过100px并且上下滑动不超过60px
        if (arg1.x - arg0.x > 100 && Math.abs(arg0.y - arg1.y) < 60) {
            mc.onBackPressed()
        }
        return false
    }

    override fun onShowPress(arg0: MotionEvent) {
        // TODO Auto-generated method stub
    }

    override fun onSingleTapUp(arg0: MotionEvent): Boolean {
        // TODO Auto-generated method stub
        return false
    }
}