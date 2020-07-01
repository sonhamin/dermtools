package org.techtown.mnist_sample;

public class EfficientOuput implements Comparable<EfficientOuput>{
    private int num;
    private float data;

    public EfficientOuput(int num, float data){
        this.num = num;
        this.data = data;
    }

    public int getNum(){return num;}
    public float getData(){return data;}


    @Override
    public int compareTo(EfficientOuput o) {
        if(this.data>o.data) return -1;
        else if(this.data == o.data) return 0;
        else return 1;
    }
}
