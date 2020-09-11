package com.arrowsmith.llv1.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

public class SemiSwipeableViewPager extends ViewPager {

    private boolean swipingUnlocked;

    public SemiSwipeableViewPager(Context context) {
        super(context);
        swipingUnlocked = false;
    }

    public SemiSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        swipingUnlocked = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipingUnlocked && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return swipingUnlocked && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return swipingUnlocked && super.canScrollHorizontally(direction);
    }

    public void setSwipingUnlocked(boolean swipingUnlocked){
        this.swipingUnlocked = swipingUnlocked;
    }

    public boolean isSwipingUnlocked() {
        return swipingUnlocked;
    }
}