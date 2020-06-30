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
    Boolean isOutputExist = false;

    public EfficientNet(Bitmap bitmap, Context context){
        this.bitmap = bitmap;
        this.context = context;
        input = new float[1][224][224][3];
        output = new float[1][18];
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
            MappedByteBuffer tfliteModel = null;
            try{
                tfliteModel = FileUtil.loadMappedFile(context.getApplicationContext(), "real_efficientnet.tflite");

                Interpreter.Options tfliteOptions = new Interpreter.Options();
                tfliteOptions.setNumThreads(3);
                Interpreter tflite = new Interpreter(tfliteModel, tfliteOptions);

                tflite.run(input, output);

                isOutputExist = true;
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public float[][] getOutput(){
        if(isOutputExist) return output;
        return null;
    }
}
