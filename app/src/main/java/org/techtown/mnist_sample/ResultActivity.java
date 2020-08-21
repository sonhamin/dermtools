package org.techtown.mnist_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.ml.custom.FirebaseModelOutputs;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
    ImageView imageView;
    ArrayList<RectangleRange> ranges = new ArrayList<>();
    float[][][] bmpOutputs;
    ArrayList<Bitmap> croppedBitmaps = new ArrayList<>();
    String res_all;
    TextView textView;
    Bitmap new_bit;
    ResultActivity resultActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
//        ranges = intent.getParcelableArrayListExtra("ranges");
        croppedBitmaps = intent.getParcelableArrayListExtra("bitmaps");
        int a = intent.getIntExtra("a", 1);
        int b = intent.getIntExtra("b", 1);
        int c = intent.getIntExtra("c", 1);
        bmpOutputs = new float[a][b][c];
        for(int i=0;i<a;i++){
            for(int j=0;j<b;j++){
                String name = "bmp"+a+"_"+b;
                bmpOutputs[i][j] = intent.getFloatArrayExtra(name);
            }
        }
        res_all = intent.getStringExtra("res");
        new_bit = (Bitmap)intent.getParcelableExtra("image");

        resultActivity = this;

        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.textView);
        imageView.setImageBitmap(new_bit);
        textView.setText(res_all);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                boolean handledHere = false;

                final int action = ev.getAction();

                final int evX = (int)ev.getX();
                final int evY = (int)ev.getY();

                ImageView imageView = (ImageView)v.findViewById(R.id.image_view);

                final int imSize = imageView.getLayoutParams().width;
                final int changeSize = 304;
                if(imageView == null) return false;

                switch(action){
                    case MotionEvent.ACTION_UP:
                        int changeX = evX * changeSize / imSize;
                        int changeY = evY * changeSize / imSize;
                        int inNum = -1;
                        for(int i=0;i<ranges.size();i++){
                            if(ranges.get(i).isIn(changeX, changeY)){
                                inNum = i;
                                break;
                            }
                        }
                        if(inNum==-1){
                            Log.d("asdfasdf", "not inside contour   : " + ranges.size());
                        }
                        else{
                            Character numb = (char)('A'+inNum);
                            EfficientOuput efficientOuput = new EfficientOuput();
                            Intent intent = new Intent(resultActivity, PopupActivity.class);
                            intent.putExtra("image", croppedBitmaps.get(inNum));
                            intent.putExtra("numb", numb+"");
                            intent.putExtra("data", efficientOuput.outputToData(bmpOutputs[inNum]));
                            startActivityForResult(intent, 1);
                        }
                        break;
                }
                return true;
            }
        });
    }

}
