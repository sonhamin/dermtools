package org.techtown.mnist_sample;

public class RectangleRange {
    int up;
    int down;
    int left;
    int right;

    public RectangleRange(int up, int down, int left, int right){
        this.up = up;
        this.down = down;
        this.right = right;
        this.left = left;
    }

    public int getUp(){return up;}
    public int getDown(){return down;}
    public int getLeft(){return left;}
    public int getRight(){return right;}

    public String setString(){
        return "("+left+", "+up+"), ("+right+", "+down+")";
    }

    public boolean isIn(int x, int y){
        if(up<=y && down>=y && left<=x && right>=x){
            return true;
        }
        else return false;
    }

}
