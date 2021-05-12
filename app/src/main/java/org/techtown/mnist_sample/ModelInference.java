package org.techtown.mnist_sample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.techtown.mnist_sample.MainActivity.bmpOutputs;
import static org.techtown.mnist_sample.MainActivity.croppedBitmaps;
import static org.techtown.mnist_sample.MainActivity.ranges;

public class ModelInference {
    OptionsInput options_input;
    Preprocessor preprocessor = new Preprocessor();
    Bitmap mask_bitmap;
    Bitmap resizedBitmap;
    Context context;

    Mat orig_changed;

    List<MatOfPoint> contours;
    Bitmap[] bmps;

    Uri imageUri;
    Uri cropUri;
    int save;

    TextView textView;
    public ModelInference(Context context, Bitmap resizedBitmap, Uri imageUri, Uri cropUri, int save)
    {
        this.context = context;
        this.resizedBitmap = resizedBitmap;
        textView = ((Activity) context).findViewById(R.id.textView);
        this.imageUri = imageUri;
        this.cropUri = cropUri;
        this.save = save;


    }

    public FirebaseModelInterpreterOptions initializeUnet(FirebaseCustomLocalModel localModel_unet)
    {
        FirebaseModelInterpreterOptions options2;
        options2 = new FirebaseModelInterpreterOptions.Builder(localModel_unet).build();
        return options2;

    }
    public void segment_and_classify(float[][][][] input1, float[][][][] input2, float[][][][] input3, FirebaseModelInterpreterOptions options2, final FirebaseCustomLocalModel localModel_effnet) {
        try {
            FirebaseModelInterpreter interpreter = FirebaseModelInterpreter.getInstance(options2);
            options_input = new OptionsInput();
            FirebaseModelInputOutputOptions inputOutputOptions = options_input.getUnetOptions();
            FirebaseModelInputs inputs = options_input.getUnetInputs(input1, input2, input3);

            interpreter.run(inputs, inputOutputOptions)
                    .addOnSuccessListener(
                            new OnSuccessListener<FirebaseModelOutputs>() {
                                @Override
                                public void onSuccess(FirebaseModelOutputs result) {
                                    Log.e("asdf", "done masking");
                                    Log.e("asdf", "THis is save: "+save);
                                    float[][][][] output2 = result.getOutput(0);
                                    mask_bitmap = preprocessor.maskBitmap(output2);
                                    init_efficientnet(localModel_effnet);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {        e.printStackTrace();                               }
                            });
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }

    private void init_efficientnet(FirebaseCustomLocalModel localModel_effnet) {

        FirebaseModelInterpreterOptions options2 = new FirebaseModelInterpreterOptions.Builder(localModel_effnet).build();
        FirebaseModelInterpreter effnet_interpreter = null;
        try {
            effnet_interpreter = FirebaseModelInterpreter.getInstance(options2);
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
        FirebaseModelInputOutputOptions inputOutputOptions = options_input.getEffnetOptions();
        classify(effnet_interpreter, inputOutputOptions);

    }
    int max;
    private void classify(FirebaseModelInterpreter effnet_interpreter, FirebaseModelInputOutputOptions inputOutputOptions) {
        contours = preprocessor.get_contours(mask_bitmap);
        orig_changed = preprocessor.getResizedMat(resizedBitmap);
        bmpOutputs = preprocessor.initBmpOutputs(contours);
        bmps = preprocessor.initBmps(contours);
        croppedBitmaps.removeAll(croppedBitmaps);

        for (int i=0; i<contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            rect = preprocessor.adjustRectangles(rect, orig_changed);
            final int height = rect.height;
            final int width = rect.width;
            if(height > 50 && width > 50 && height < 250){         max=i;            }
        }

        for (int i=0; i<contours.size(); i++) {

            final List<MatOfPoint> contour = new ArrayList<>();
            contour.add(contours.get(i));

            Rect rect = Imgproc.boundingRect(contours.get(i));
            int [] originalDims = preprocessor.getOriginalDims(rect);
            rect = preprocessor.adjustRectangles(rect, orig_changed);

            final int x = rect.x;
            final int y = rect.y;
            final int height = rect.height;
            final int width = rect.width;

            float ratio = (float)height / (float)width;

            if(height > 50 && width > 50 && height < 250)
            {

                Log.e("asdf", "x: " + x + " y: " + y + " height: " + height + " width: " + width);
                preprocessor.drawContourRects(orig_changed, contour, rect, false);

                final RectangleRange rectangleRange = preprocessor.getRectangleRange(originalDims);
                ranges.add(rectangleRange);

                bmps[i] = preprocessor.crop_segments(context.getContentResolver(), cropUri, rect, width, height);
                croppedBitmaps.add(bmps[i]);
                Log.d("croppedBitmap", i+": "+width+", "+height+" || "+rect);
                Log.d("crooppedBitmap Size", i+": "+bmps[i].getWidth()+", "+bmps[i].getHeight());
                //Log.d("crooppedBitmap Size", i+": "+preprocessor.crop_segments(context.getContentResolver(), imageUri, rect, width, height).getWidth()+", "+preprocessor.crop_segments(context.getContentResolver(), imageUri, rect, width, height).getHeight());

                float [][][][] effnet_input = preprocessor.make_inputs_effnet(bmps[i]);
                FirebaseModelInputs inputs = options_input.getEffnetInputs(effnet_input);
                final Rect finalRect = rect;

                final int finalI = i;
                effnet_interpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {
                                        Point pnt = new Point(x + width/2 - 10, y + height/2 + 10);
                                        set_output_parameters(result, pnt, rectangleRange, orig_changed, contour, finalRect, false);

                                        if(finalI == max)
                                        {
                                            Log.e("asdfasdfasdf", "This is i and max:"+finalI+"  "+(contours.size()-1));
                                            Log.e("asdfasdfasdf", res_all);
                                            Log.e("asdfasdfasdf", "DONE");
                                            Log.e("asdf", "This is save: "+save);

                                            ((Activity) context).findViewById(R.id.analyzing_view_image).setVisibility(View.INVISIBLE);
                                            ((Activity) context).findViewById(R.id.analyzing_view_text).setVisibility(View.INVISIBLE);
                                            ((Activity) context).findViewById(R.id.view).setVisibility(View.INVISIBLE);



                                            String text_all = textView.getText().toString() + "\n\n\nIMPORTANT: \nOur models are still undergoing training. \n If you are experiencing serious skin discomfort, please contact a medical professional.";
                                            textView.setText(text_all);


                                        }

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {    e.printStackTrace();     }
                                });
            }


        }


    }



    int counter;
    String res_all = "";
    Bitmap new_bit;
    ImageView iv;

    private void set_output_parameters(FirebaseModelOutputs result, Point pnt, RectangleRange rectangleRange, Mat orig_changed, List<MatOfPoint> contour, Rect finalRect, boolean b) {
        bmpOutputs[counter] = result.getOutput(0);

        Character numb = (char)('A'+counter);
        EfficientOuput efficientOuput = new EfficientOuput();
        String a = efficientOuput.outputToString(bmpOutputs[counter]);


        ranges.add(rectangleRange);
        preprocessor.drawContourRects(orig_changed, contour, finalRect, b);


        res_all = res_all + numb + ": "+a + "\n";
        String text = "" + numb;

        int fontFace = 2;
        double fontScale = 1.0;
        Imgproc.putText(this.orig_changed, text, pnt, fontFace, fontScale, Scalar.all(255));

        new_bit = Bitmap.createBitmap(304, 304, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(this.orig_changed, new_bit);



        //String text_all = textView.getText().toString();
        //String text_all2 = text_all.substring(0, text_all.length() - 34);

        textView.setText(res_all);
        textView.setMovementMethod(new ScrollingMovementMethod());

        Log.d("efficientNet", res_all);

        ImageView select_btn, analyze_btn;
        ImageView iv;

        Log.e("asdf", "THis is save: "+save);

        iv = ((MainActivity) context).findViewById(R.id.image_view);
        select_btn = ((MainActivity) context).findViewById(R.id.imageView);

        iv.setImageBitmap(new_bit);
        iv.setVisibility(View.VISIBLE);


        MainActivity mainActivity;
        mainActivity = (MainActivity) context;

        if(iv!=null){
            iv.setOnTouchListener(mainActivity);
        }


        counter++;
    }

}
