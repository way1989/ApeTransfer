/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ape.qrcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.ape.qrcode.camera.CameraManager;
import com.google.zxing.ResultPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;
    /**
     * 中间滑动线的最底端位置
     */
    private static boolean mIsFirst;
    private final Bitmap mLineBitmap;
    private final int SPEEN_DISTANCE;// 中间那条线每次刷新移动的距离
    private final int LINE_WIDTH;// 四个边角对应的宽度
    private final int LINE_PADDING;
    private final Paint paint;
    private final int maskColor;
    private final int resultColor;
    private final int laserColor;
    private final int resultPointColor;
    private int LINE_LENGTH;// 四个边角对应的长度
    /**
     * 中间滑动线的最顶端位置
     */
    private int mSlideTop;
    private CameraManager cameraManager;
    private Bitmap resultBitmap;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        laserColor = resources.getColor(R.color.viewfinder_laser);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;


        mLineBitmap = BitmapFactory.decodeResource(resources, R.drawable.qb_scan_light);

        float density = context.getResources().getDisplayMetrics().density;
        // 将像素转换成dp
        Log.i("liweiping", "density = " + density);
        SPEEN_DISTANCE = (int) (2 * density);
        LINE_LENGTH = (int) (20 * density);
        LINE_WIDTH = (int) (5 * density);
        LINE_PADDING = -LINE_WIDTH;

        mIsFirst = true;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            // 初始化中间线滑动的最上边和最下边
            if (mIsFirst) {
                mIsFirst = false;
                mSlideTop = frame.top;
            }
            paint.setColor(Color.WHITE);
            // 画边框的四条细线
            canvas.drawLine(frame.left, frame.top - 1, frame.right,
                    frame.top - 1, paint);// 上，向上偏移一个像素
            canvas.drawLine(frame.right, frame.top, frame.right, frame.bottom,
                    paint);// 右，右边不用偏移
            canvas.drawLine(frame.left, frame.bottom, frame.right,
                    frame.bottom, paint);// 下，下面不用偏移，
            canvas.drawLine(frame.left - 1, frame.top, frame.left - 1,
                    frame.bottom, paint);// 左，左边偏移一个像素

            // draw rect //画扫描框边上的角，总共8个部分
            paint.setColor(laserColor);
            canvas.drawRect(LINE_PADDING + frame.left,
                    LINE_PADDING + frame.top, LINE_PADDING
                            + (LINE_WIDTH + frame.left), LINE_PADDING
                            + (LINE_LENGTH + frame.top), paint);// 左-上
            canvas.drawRect(LINE_PADDING + frame.left,
                    LINE_PADDING + frame.top, LINE_PADDING
                            + (LINE_LENGTH + frame.left), LINE_PADDING
                            + (LINE_WIDTH + frame.top), paint);// 上-左
            canvas.drawRect(-LINE_PADDING + ((-1 - LINE_WIDTH) + frame.right),
                    LINE_PADDING + frame.top, -LINE_PADDING + frame.right - 1,
                    LINE_PADDING + (LINE_LENGTH + frame.top), paint);// 右-上
            canvas.drawRect(-LINE_PADDING + (-LINE_LENGTH + frame.right - 1),
                    LINE_PADDING + frame.top, -LINE_PADDING + frame.right - 1,
                    LINE_PADDING + (LINE_WIDTH + frame.top), paint);// 上-右
            canvas.drawRect(LINE_PADDING + frame.left, -LINE_PADDING
                            + (-(LINE_LENGTH - 1) - 1 + frame.bottom), LINE_PADDING
                            + (LINE_WIDTH + frame.left), -LINE_PADDING + frame.bottom,
                    paint);// 左-下
            canvas.drawRect(LINE_PADDING + frame.left, -LINE_PADDING
                            + ((0 - LINE_WIDTH) + frame.bottom), LINE_PADDING
                            + (LINE_LENGTH + frame.left), -LINE_PADDING + frame.bottom,
                    paint);// 下-左
            canvas.drawRect(-LINE_PADDING + ((-1 - LINE_WIDTH) + frame.right),
                    -LINE_PADDING + (-(LINE_LENGTH - 1) + frame.bottom),
                    -LINE_PADDING + frame.right - 1, -LINE_PADDING
                            + frame.bottom, paint);// 右-下
            canvas.drawRect(-LINE_PADDING + (-LINE_LENGTH + frame.right - 1),
                    -LINE_PADDING + ((0 - LINE_WIDTH) + frame.bottom),
                    -LINE_PADDING + frame.right - 1, -LINE_PADDING
                            + (LINE_WIDTH - LINE_WIDTH + frame.bottom), paint);// 下-右
            // 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            mSlideTop += SPEEN_DISTANCE;
            if (mSlideTop >= frame.bottom - SPEEN_DISTANCE - 18) {
                mSlideTop = frame.top + SPEEN_DISTANCE;
            }
            Rect lineRect = new Rect(frame.left, mSlideTop, frame.right,
                    mSlideTop + 18);
            canvas.drawBitmap(mLineBitmap, null, lineRect, paint);

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY,
                    frame.left - POINT_SIZE,
                    frame.top - POINT_SIZE,
                    frame.right + POINT_SIZE,
                    frame.bottom + POINT_SIZE);
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

}
