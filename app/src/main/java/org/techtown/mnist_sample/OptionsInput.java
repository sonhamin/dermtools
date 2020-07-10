package org.techtown.mnist_sample;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;

public class OptionsInput {

    public OptionsInput()
    {

    }

    public FirebaseModelInputOutputOptions getUnetOptions()
    {
        FirebaseModelInputOutputOptions inputOutputOptions = null;
        try {
            inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 304, 304, 3})
                            .setInputFormat(1, FirebaseModelDataType.FLOAT32, new int[]{1, 304, 304, 3})
                            .setInputFormat(2, FirebaseModelDataType.FLOAT32, new int[]{1, 304, 304, 3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 304, 304, 1})
                            .build();
            Log.e("asdf", "yes: 2");
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        return inputOutputOptions;
    }


    public FirebaseModelInputs getUnetInputs(float [][][][] input1, float [][][][] input2, float [][][][] input3){
        FirebaseModelInputs inputs = null;
        try {
            inputs = new FirebaseModelInputs.Builder()
                    .add(input1)  // add() as many input arrays as your model requires
                    .add(input2)
                    .add(input3)
                    .build();
            Log.e("asdf", "yes: 3");
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        return inputs;
    }


    public FirebaseModelInputOutputOptions getEffnetOptions()
    {
        FirebaseModelInputOutputOptions inputOutputOptions = null;
        try {
            inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 18})
                            .build();
            Log.e("asdf", "yes22: 2");
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
        return inputOutputOptions;
    }

    public FirebaseModelInputs getEffnetInputs(Bitmap bmp)
    {
        int[] temp_eff = new int[224*224];
        bmp.getPixels(temp_eff, 0, 224, 0, 0, 224, 224);
        float [][][][] effin = new float[1][224][224][3];
        for(int a=0; a<224; a++)
            for(int b=0; b<224; b++)
            {
                int rgb1 = temp_eff[a*224+b];
                int R1 = Color.red(rgb1);
                int G1 = Color.green(rgb1);
                int B1 = Color.blue(rgb1);
                effin[0][a][b][0] = (float) (R1);
                effin[0][a][b][1] = (float) (G1);
                effin[0][a][b][2] = (float) (B1);

            }

        FirebaseModelInputs inputs = null;
        try {
            inputs = new FirebaseModelInputs.Builder()
                    .add(effin)
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
        return inputs;

    }


}
