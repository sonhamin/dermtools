package org.techtown.mnist_sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class EfficientNet {
    float[][][][] input;
    float[][] output;
    Bitmap bitmap;
    Context context;
    Boolean isOutputExist;
    Interpreter tflite;

    public EfficientNet(Bitmap bitmap, Context context, Interpreter tflite){
        this.bitmap = bitmap;
        this.context = context;
        this.tflite = tflite;
        input = new float[1][224][224][3];
        output = new float[1][18];
        isOutputExist = false;
    }

    public void RunModel(){
        if(bitmap.getHeight() == 224 && bitmap.getHeight() == 224){
            int[] bitmapIntArr = new int[224*224];
            bitmap.getPixels(bitmapIntArr, 0, 224, 0, 0, 224, 224);
            for(int i=0;i<224;i++){
                for(int j=0;j<224;j++){
                    int rgb = bitmapIntArr[i*224+j];
                    int R = Color.red(rgb);
                    int G = Color.green(rgb);
                    int B = Color.blue(rgb);
                    input[0][i][j][0] = R;
                    input[0][i][j][1] = G;
                    input[0][i][j][2] = B;
                }
            }
                tflite.run(input, output);

                isOutputExist = true;

        }
    }

    public float[][] getOutput(){
        if(isOutputExist) return output;
        return null;
    }
}
