package org.techtown.mnist_sample;

public class EfficientOuput implements Comparable<EfficientOuput>{
    private String name;
    private float data;

    public EfficientOuput(String num, float data){
        this.name = num;
        this.data = data;
    }

    public String getName(){return name;}
    public float getData(){return data;}


    @Override
    public int compareTo(EfficientOuput o) {
        if(this.data>o.data) return -1;
        else if(this.data == o.data) return 0;
        else return 1;
    }
}
