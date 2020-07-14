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

    public FirebaseModelInputs getEffnetInputs(float[][][][] effnet_input) {
        FirebaseModelInputs inputs = null;
        try {
            inputs = new FirebaseModelInputs.Builder()
                    .add(effnet_input)
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
        return inputs;

    }
}
