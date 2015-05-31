package org.jain.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class StyleUtil {

    public static int getPixelByDP(Context context, float dpNum) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpNum, metrics) + 0.5);
    }
}
