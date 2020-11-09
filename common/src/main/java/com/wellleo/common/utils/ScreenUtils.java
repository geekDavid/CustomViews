package com.wellleo.common.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.wellleo.common.Common;

public class ScreenUtils {

    /**
     * dip to px value
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dip2px(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int result = (int) ((displayMetrics.density * dp) + 0.5f);
        return result;
    }

    /**
     * get Screen Width
     *
     * @return
     */
    public static int getScreenWidth() {
        Context context = Common.sAppContext;
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * get Screen Height
     *
     * @return
     */
    public static int getScreenHeight() {
        Context context = Common.sAppContext;
        return context.getResources().getDisplayMetrics().heightPixels;
    }

}
