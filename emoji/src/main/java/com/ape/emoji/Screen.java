package com.ape.emoji;

import android.content.Context;
import android.content.res.Resources;

public class Screen {

    private static float density;
    private static float scaledDensity;

    public static int dp(Context context, float dp) {
        if (density == 0f)
            density = context.getResources().getDisplayMetrics().density;

        return (int) (dp * density + .5f);
    }

    public static int sp(Context context, float sp) {
        if (scaledDensity == 0f)
            scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;

        return (int) (sp * scaledDensity + .5f);
    }

    public static int getWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getStatusBarHeight(Context context) {

        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavbarHeight(Context context) {
        if (hasNavigationBar(context)) {
            int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    public static boolean hasNavigationBar(Context context) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return (id > 0) && resources.getBoolean(id);
    }

    public static float getDensity() {
        return density;
    }
}