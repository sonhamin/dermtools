package org.techtown.mnist_sample;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class DeviceInfo {
    String board;
    String brand;
    String cpu_abi;
    String device;
    String display;
    String fingerprint;
    String host;
    String id;
    String manufacturer;
    String model;
    String product;
    String tags;
    String type;
    String user;
    String vers_rel;

    private static final String TAG = "PhoneState_DeviceInfo";

    public DeviceInfo(String board, String brand, String cpu_abi, String device, String display, String fingerprint,
                      String host, String id, String manufacturer, String model, String product, String tags,
                      String type, String user, String vers_rel){
        this.board = board;
        this.brand = brand;
        this.cpu_abi = cpu_abi;
        this.device = device;
        this.display = display;
        this.fingerprint = fingerprint;
        this.host = host;
        this.id = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.product = product;
        this.tags = tags;
        this.type = type;
        this.user = user;
        this.vers_rel = vers_rel;
    }

    public void logging(){
        Log.d(TAG, "BOARD = " + Build.BOARD);
        Log.d(TAG, "BRAND = " + Build.BRAND);
        Log.d(TAG, "CPU_ABI = " + Build.CPU_ABI);
        Log.d(TAG, "DEVICE = " + Build.DEVICE);
        Log.d(TAG, "DISPLAY = " + Build.DISPLAY);
        Log.d(TAG, "FINGERPRINT = " + Build.FINGERPRINT);
        Log.d(TAG, "HOST = " + Build.HOST);
        Log.d(TAG, "ID = " + Build.ID);
        Log.d(TAG, "MANUFACTURER = " + Build.MANUFACTURER); //제조사
        Log.d(TAG, "MODEL = " + Build.MODEL); //모델명
        Log.d(TAG, "PRODUCT = " + Build.PRODUCT);
        Log.d(TAG, "TAGS = " + Build.TAGS);
        Log.d(TAG, "TYPE = " + Build.TYPE);
        Log.d(TAG, "USER = " + Build.USER);
        Log.d(TAG, "VERSION.RELEASE = " + Build.VERSION.RELEASE);
    }

    public String getId(){
        return id;
    }

    public Map<String, Object> postInfo(){
        Map<String, Object> postValues = toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        String key = "/"+id+"/deviceInfo";
        childUpdates.put(key, postValues);
        return childUpdates;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("board", board);
        result.put("brand", brand);
        result.put("cpu_abi", cpu_abi);
        result.put("device", device);
        result.put("display", display);
        result.put("fingerprint", fingerprint);
        result.put("host", host);
        result.put("manufacturer", manufacturer);
        result.put("model", model);
        result.put("product", product);
        result.put("tags", tags);
        result.put("type", type);
        result.put("user", user);
        result.put("version_release", vers_rel);
        return result;
    }

}
