package org.techtown.mnist_sample;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceInfo {
    TelephonyManager tm;
    String modelnumb;
    public DeviceInfo(Application application){
        tm = (TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
        modelnumb = Build.BOARD;
    }

    public String getModelnumb() {
        return modelnumb;
    }
}
