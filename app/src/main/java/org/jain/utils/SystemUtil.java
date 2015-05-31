package org.jain.utils;

import android.widget.EditText;

public class SystemUtil {

    public static void removeEditTextError(final EditText targetEditText,
                                           Long delayMills) {

        if (targetEditText == null) {
            return;
        }

        if (delayMills == null) {
            delayMills = 2000L; // default delayTime;
        }

        targetEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                targetEditText.setError(null);
            }
        }, delayMills);
    }
}
