package com.dgonlam.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;

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

    public static Rect calculateTapArea(Camera camera,float x, float y, float coefficient) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int centerX = (int) (x / getResolution(camera).width * 2000 - 1000);
        int centerY = (int) (y / getResolution(camera).height * 2000 - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int right = clamp(left + areaSize, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        int bottom = clamp(top + areaSize, -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static Camera.Size getResolution(Camera mCamera) {
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size s = params.getPreviewSize();
        return s;
    }
}
