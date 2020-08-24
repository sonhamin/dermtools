package org.techtown.mnist_sample;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class OriginalInfo {
    public OriginalInfo(){

    }

    public Map<String, Object> postInfo(String user, String time){
        Map<String, Object> postValues = new HashMap<>();
        Map<String, Object> childUpdates = new HashMap<>();

        String key = "/"+user+"/diseaseInfo/"+time+"/original";

        String ref = "image/"+user+"/"+time+"/original.jpg";
        postValues.put("ref", ref);

        childUpdates.put(key, postValues);
        return childUpdates;
    }

}
