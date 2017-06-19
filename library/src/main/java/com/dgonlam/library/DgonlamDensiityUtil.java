package com.dgonlam.library;

import android.app.Activity;
import android.content.Context;

/**
 * Created by DgonLam on 2017/6/16.
 */
public class DgonlamDensiityUtil {
    private int screenWidth;
    private int screenHeight;

    public DgonlamDensiityUtil(Activity activity){
        screenWidth = activity.getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
