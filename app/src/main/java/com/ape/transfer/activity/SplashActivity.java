package com.ape.transfer.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ImageView;

import com.ape.transfer.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {
    private static final ArgbEvaluator ARGB_EVALUATOR = new ArgbEvaluator();
    private static final int HANDLER_MESSAGE_ANIMATION = 0;
    private static final int HANDLER_MESSAGE_NEXT_ACTIVITY = 1;
    @BindView(R.id.splash_bg)
    ImageView splashBg;

    private ColorDrawable mColorDrawable;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == HANDLER_MESSAGE_ANIMATION) {
                playColorAnimator();
            } else if (msg.what == HANDLER_MESSAGE_NEXT_ACTIVITY) {
                next();
            }
        }
    };

    private void next() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.scroll_in, R.anim.scroll_out);
        finish();
    }

    private void playColorAnimator() {
        final List<Animator> animList = new ArrayList<>();
        final int toColor = getResources().getColor(R.color.primary_dark);
        //final int toColor = Color.parseColor("#1F1F1F");
        final Window window = getWindow();
        ObjectAnimator statusBarColor = ObjectAnimator.ofInt(window,
                "statusBarColor", window.getStatusBarColor(), toColor);
        statusBarColor.setEvaluator(ARGB_EVALUATOR);
        animList.add(statusBarColor);

        ObjectAnimator navigationBarColor = ObjectAnimator.ofInt(window,
                "navigationBarColor", window.getNavigationBarColor(), toColor);
        navigationBarColor.setEvaluator(ARGB_EVALUATOR);
        animList.add(navigationBarColor);

        ObjectAnimator backgroundColor = ObjectAnimator.ofObject(mColorDrawable, "color", ARGB_EVALUATOR, toColor);
        animList.add(backgroundColor);

        final AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(1500L);
        animSet.playTogether(animList);
        animSet.start();

        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_NEXT_ACTIVITY, 500L);
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initViews() {
        mColorDrawable = new ColorDrawable(Color.BLACK);
        splashBg.setBackground(mColorDrawable);
        mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_ANIMATION, 500L);
    }
}
