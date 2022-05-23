package com.cdnbye.demo

import android.app.Activity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import com.cdnbye.demo.BackGestureListener

open class BaseActivity : Activity() {
    /** 手势监听  */
    var mGestureDetector: GestureDetector? = null

    /** 是否需要监听手势关闭功能  */
    private var mNeedBackGesture = false
    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState)
        initGestureDetector()
    }

    private fun initGestureDetector() {
        if (mGestureDetector == null) {
            mGestureDetector = GestureDetector(
                applicationContext,
                BackGestureListener(this)
            )
        }
    }

    /* (non-Javadoc)
     * touch手势分发
     * @see android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // TODO Auto-generated method stub
        return if (mNeedBackGesture) {
            (mGestureDetector!!.onTouchEvent(ev)
                    || super.dispatchTouchEvent(ev))
        } else super.dispatchTouchEvent(ev)
    }

    /*
     * 设置是否进行手势监听
     */
    fun setNeedBackGesture(mNeedBackGesture: Boolean) {
        this.mNeedBackGesture = mNeedBackGesture
    }
}