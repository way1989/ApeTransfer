package com.ape.transfer.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.cardview.widget.CardView;

/**
 * Created by way
 * on 16/7/15.
 * 正方形控件
 */
public class SquareLayout extends CardView {

    public SquareLayout(Context context) {
        super(context);
    }

    public SquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /*
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightSize == 0) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
            return;
        }

        if (widthSize == 0) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
            return;
        }

        if (widthSize > heightSize)
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        else
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        */
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
