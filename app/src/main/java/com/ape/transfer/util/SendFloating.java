package com.ape.transfer.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.view.animation.AccelerateInterpolator;

import com.ufreedom.floatingview.transition.BaseFloatingPathTransition;
import com.ufreedom.floatingview.transition.FloatingPath;
import com.ufreedom.floatingview.transition.PathPosition;
import com.ufreedom.floatingview.transition.YumFloating;

/**
 * Created by android on 16-11-7.
 */

public class SendFloating extends BaseFloatingPathTransition {
    @Override
    public FloatingPath getFloatingPath() {
        Path path = new Path();
        path.rLineTo(0, -2000);
        return FloatingPath.create(path, false);
    }

    @Override
    public void applyFloating(final YumFloating yumFloating) {
/*        SpringHelper.createWithBouncinessAndSpeed(0.0f, 1.0f, 10, 15)
                .reboundListener(new SimpleReboundListener() {
                    @Override
                    public void onReboundUpdate(double currentValue) {
                        yumFloating.setScaleX((float) currentValue);
                        yumFloating.setScaleY((float) currentValue);
                    }
                }).start(yumFloating);*/


/*        ValueAnimator rotateAnimator = ObjectAnimator.ofFloat(0, 360);
        rotateAnimator.setDuration(200L);
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                yumFloating.setRotation((float) valueAnimator.getAnimatedValue());
            }
        });*/

        ValueAnimator translateAnimator = ObjectAnimator.ofFloat(getStartPathPosition(), getEndPathPosition());
        translateAnimator.setDuration(1000L);
        translateAnimator.setInterpolator(new AccelerateInterpolator());
        translateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                PathPosition floatingPosition = getFloatingPosition(value);
                yumFloating.setTranslationX(floatingPosition.x);
                yumFloating.setTranslationY(floatingPosition.y);
            }
        });
        translateAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                yumFloating.setTranslationX(0);
                yumFloating.setTranslationY(0);
                yumFloating.setAlpha(0f);
                yumFloating.clear();
            }
        });
        //rotateAnimator.start();
        translateAnimator.start();
    }
}
