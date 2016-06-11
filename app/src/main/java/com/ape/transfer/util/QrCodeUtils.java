package com.ape.transfer.util;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Random;

/**
 * 二维码工具类
 *
 * @author FireAnt（http://my.oschina.net/LittleDY）
 * @version 创建时间：2014年12月5日 下午5:15:47
 */

public class QrCodeUtils {
    public static final int STYLE_SIMPLE = 0;
    public static final int STYLE_COLORS = 1;
    public static final int STYLE_FLOWER = 2;

    private static final int IMAGE_HALFWIDTH = 20;
    // ---二维码的颜色
    private static final int COLOR_BLUE = 0xFF3366CC;
    private static final int COLOR_RED = 0xFFFD3C3C;
    private static final int COLOR_PURPLE = 0xFF68228B;
    private static final int COLOR_BLACK = 0xEE000000;
    private static final int[] COLORS = {COLOR_BLUE, COLOR_RED, COLOR_PURPLE,
            COLOR_BLACK};

    /**
     * 传入字符串生成二维码
     *
     * @param str
     * @return
     * @throws WriterException
     */
    public static Bitmap create2DCode(String str) throws WriterException {
        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(str,
                BarcodeFormat.QR_CODE, 300, 300);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }

            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap createQRCode(String content, int style) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, 300, 300);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int halfW = width / 2;
        int halfH = height / 2;
        int[] pixels = new int[width * height];

        int a = -150;
        int b = 150;
        int c = 150;
        int d = 450;
        Random random = new Random();
        int leftTop = COLORS[random.nextInt(4)];
        int leftBottom = COLORS[random.nextInt(4)];
        int rightTop = COLORS[random.nextInt(4)];
        int rightBottom = COLORS[random.nextInt(4)];
        int center = COLORS[random.nextInt(4)];
        Log.i(leftTop + ":" + leftBottom + ":" + rightTop + ":" + rightBottom
                + ":" + center);
        int defaultQRColor = 0xff000000;
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {

                // 将图片绘制到指定区域中
                // 就是将图片像素的颜色值写入到相应下标的数组中
                if (w > halfW - IMAGE_HALFWIDTH && w < halfW + IMAGE_HALFWIDTH
                        && h > halfH - IMAGE_HALFWIDTH
                        && h < halfH + IMAGE_HALFWIDTH && /*mIsShowLogo*/ false) {
//                    pixels[h * width + w] = mLogoInsideBitmap.getPixel(w
//                            - halfW + IMAGE_HALFWIDTH, h - halfH
//                            + IMAGE_HALFWIDTH);
                } else {
                    // 判断当前位置在二维矩阵中存储的boolean值
                    if (bitMatrix.get(w, h)) {
                        if (style == STYLE_COLORS) {
                            // ---左下角
                            if (h >= 150 && w <= a) {
                                defaultQRColor = leftBottom;

                                // ---右上角
                            } else if (h <= 150 && w >= b) {
                                defaultQRColor = rightTop;

                                // ---左上角
                            } else if (h <= 150 && w <= c) {
                                defaultQRColor = leftTop;

                                // ---右下角
                            } else if (h >= 150 && w >= d) {
                                defaultQRColor = rightBottom;

                            } else {
                                defaultQRColor = center;
                            }
                        } else if (style == STYLE_FLOWER) {
                            defaultQRColor = COLORS[random.nextInt(4)];
                        } else {
                            defaultQRColor = 0xff000000;
                        }

                        pixels[h * width + w] = defaultQRColor;

                    } else {
                        pixels[h * width + w] = 0xffffffff;

                    }
                }

            }
            a++;
            b++;
            c--;
            d--;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
