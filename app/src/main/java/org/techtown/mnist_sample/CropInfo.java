package org.techtown.mnist_sample;

import java.util.HashMap;
import java.util.Map;

public class CropInfo {
    public double area;
    public double R;
    public double G;
    public double B;

    public CropInfo(){

    }

    public CropInfo(double area, double R, double G, double B){
        this.area = area;
        this.R = R;
        this.G = G;
        this.B = B;
    }

    public double getArea()             {return area; }
    public double getR()                {return R;}
    public double getB()                {return B;}
    public double getG()                {return G;}
    public void setArea(double area)    {this.area = area;}
    public void setR(double R)          {this.R = R;}
    public void setG(double G)          {this.G = G;}
    public void setB(double B)          {this.B = B;}

    public Map<String, Object> postInfo(String user, String time, String parameter){
        Map<String, Object> postValues = toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        String key = "/"+user+"/"+time+"/"+parameter+"/Text";
        childUpdates.put(key, postValues);
        String key2 = "/"+user+"/"+time+"/"+parameter+"/Image";
        Map<String, Object> postValues2 = new HashMap<>();
        postValues2.put("ref", "image/"+user+"/"+time+"/"+parameter +".jpg");
        childUpdates.put(key2, postValues2);
        return childUpdates;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("area", area);
        result.put("R", R);
        result.put("G", G);
        result.put("B", B);
        return result;
    }
}
