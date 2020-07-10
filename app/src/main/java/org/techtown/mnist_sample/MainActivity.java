package org.techtown.mnist_sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.techtown.mnist_sample.EfficientOuput;
import org.techtown.mnist_sample.InfoActivity;
import org.techtown.mnist_sample.OptionsInput;
import org.techtown.mnist_sample.PopupActivity;
import org.techtown.mnist_sample.Preprocessor;
import org.techtown.mnist_sample.R;
import org.techtown.mnist_sample.RectangleRange;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{

    ImageButton imageButton;
    ImageView imageView1, imageView2, imageView3;
    ImageView iv;
    Button analysisButton, addButton;

    ArrayList<RectangleRange> ranges = new ArrayList<>();

    private final int GET_GALLERY_IMAGE = 200;

    int image_num = 0;
    int crop_num = 0;
    float[][][] bmpOutputs;
    float[][][][] input1;
    float[][][][] input2;
    float[][][][] input3;
    Uri imageUri1, imageUri2, imageUri3;
    Uri cropUri1, cropUri2, cropUri3;

    Bitmap mask_bitmap;
    Bitmap resizedBitmap;

    Bitmap croppedBitmap1 = null, croppedBitmap2 = null, croppedBitmap3 = null;
    File croppedFile1, croppedFile2, croppedFile3;

    ArrayList<Bitmap> croppedBitmaps = new ArrayList<>();
    MainActivity mainActivity;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();
    OptionsInput options_input;
    // Create a child reference
    // imagesRef now points to "images"
    StorageReference imagesRef = storageRef.child("images");
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 상태바를 안보이도록 합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mainActivity = this;

        // 화면 켜진 상태를 유지합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        setContentView(R.layout.activity_main);


        final FirebaseCustomRemoteModel remoteModel_unet =
                new FirebaseCustomRemoteModel.Builder("unet_basic_withopt66.tflite").build();

        final FirebaseCustomLocalModel localModel_unet = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("unet_basic_withopt66.tflite")
                .build();

        final FirebaseCustomRemoteModel remoteModel_effnet =
                new FirebaseCustomRemoteModel.Builder("eff_net_basic.tflite").build();

        final FirebaseCustomLocalModel localModel_effnet = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("eff_net_basic.tflite")
                .build();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        /*FirebaseModelManager.getInstance().download(remoteModel_unet, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e("asdf", "unet download success");
                    }
                });

        FirebaseModelManager.getInstance().download(remoteModel_effnet, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e("asdf", "effnet download success");
                    }
                });*/


        imageButton = findViewById(R.id.image_select_btn);
//        imageView = findViewById(R.id.image_view);
        imageView1 = findViewById(R.id.image_1);
        imageView2 = findViewById(R.id.image_2);
        imageView3 = findViewById(R.id.image_3);
        //cropButton = findViewById(R.id.crop_btn);
        analysisButton = findViewById(R.id.classify_btn);
        addButton = findViewById(R.id.add_btn);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CharSequence[] items = {"Take Photo", "Choose from Gallery", "Cancel"};
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Add Photo");
                alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:

                                break;
                            case 1:
                                if(image_num<=2){
                                    image_num ++;
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                    startActivityForResult(intent, GET_GALLERY_IMAGE);
                                }else{
                                    Toast.makeText(getApplicationContext(), "no more image", Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });


        analysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(croppedBitmap1 != null){



                    int bithw = 304;
                    resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap1, 304, 304, true);

                    Preprocessor preprocessor = new Preprocessor();
                    Bitmap[] combs = preprocessor.decompose(resizedBitmap);
                    croppedBitmap2 = combs[0];
                    croppedBitmap3 = combs[1];

                    imageView2.setImageBitmap(croppedBitmap2);
                    imageView2.setVisibility(View.VISIBLE);
                    imageView3.setImageBitmap(croppedBitmap3);
                    imageView3.setVisibility(View.VISIBLE);


                    float [][][][][] combs_input = preprocessor.make_inputs(bithw, resizedBitmap, croppedBitmap2, croppedBitmap3);
                    input1 = combs_input[0];
                    input2 = combs_input[1];
                    input3 = combs_input[2];



                    FirebaseModelManager.getInstance().isModelDownloaded(remoteModel_unet)
                            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                @Override
                                public void onSuccess(Boolean isDownloaded) {
                                    FirebaseModelInterpreterOptions options2;
                                    if (isDownloaded) {
                                        options2 = new FirebaseModelInterpreterOptions.Builder(remoteModel_unet).build();
                                    } else {
                                        options2 = new FirebaseModelInterpreterOptions.Builder(localModel_unet).build();
                                    }
                                    segment_and_classify(input1, input2, input3, options2);

                                }
                            });

                        /*long startTime = System.currentTimeMillis();
                        long difference = System.currentTimeMillis() - startTime;
                        Log.e("DIFFERENCE", "Diff: " + String.valueOf(difference));
                        Log.e("asdf", "DONE");*/



                }
            }


            private void segment_and_classify(float[][][][] input1,float[][][][] input2,float[][][][] input3,FirebaseModelInterpreterOptions options2) {
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
                                            float[][][][] output2 = result.getOutput(0);
                                            mask_bitmap = Bitmap.createBitmap(304, 304, Bitmap.Config.ARGB_8888);
                                            for(int i=0;i<304;i++){
                                                for(int j=0;j<304;j++){
                                                    if(output2[0][i][j][0]>=0.10){          mask_bitmap.setPixel(j, i, Color.WHITE);           }
                                                    else                         {          mask_bitmap.setPixel(j, i, Color.BLACK);           }
                                                }
                                            }
                                            imageView3.setImageBitmap(mask_bitmap);
                                            init_efficientnet();
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {        e.printStackTrace();                               }
                                    });
                } catch (FirebaseMLException e) {                                            e.printStackTrace();                               }

            }

            private void init_efficientnet() {
                int[] unet_result = new int[304 * 304];
                mask_bitmap.getPixels(unet_result, 0, 304, 0, 0, 304, 304);
                Mat contour_input = new Mat();
                Mat contour_input22 = new Mat();
                Utils.bitmapToMat(mask_bitmap, contour_input);

                Imgproc.cvtColor(contour_input, contour_input22, Imgproc.COLOR_BGR2GRAY);
                final List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();

                Imgproc.findContours(contour_input22, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
                final Mat orig_img = new Mat();
                Utils.bitmapToMat(resizedBitmap, orig_img);

                final Bitmap[] bmps = new Bitmap[contours.size()];
                bmpOutputs = new float[contours.size()][1][18];


                FirebaseModelManager.getInstance().isModelDownloaded(remoteModel_effnet)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean isDownloaded) {
                                FirebaseModelInterpreterOptions options2;
                                if (isDownloaded) {
                                    options2 = new FirebaseModelInterpreterOptions.Builder(remoteModel_effnet).build();
                                } else {
                                    options2 = new FirebaseModelInterpreterOptions.Builder(localModel_effnet).build();
                                }

                                FirebaseModelInterpreter effnet_interpreter = null;
                                try {
                                    effnet_interpreter = FirebaseModelInterpreter.getInstance(options2);
                                } catch (FirebaseMLException e) {
                                    e.printStackTrace();
                                }

                                FirebaseModelInputOutputOptions inputOutputOptions = options_input.getEffnetOptions();

                                classify(contours, orig_img, bmps, effnet_interpreter, inputOutputOptions);
                            }
                        });
            }
        });

    }

    String res_all = "";
    Mat orig_changed;
    int counter = 0;
    ArrayList good_nums = new ArrayList();

    private void classify(List<MatOfPoint> contours, Mat orig_img, Bitmap[] bmps, FirebaseModelInterpreter effnet_interpreter, FirebaseModelInputOutputOptions inputOutputOptions) {

        String result = "";
        orig_changed = orig_img;

        for (int i=0; i<contours.size(); i++) {

            List<MatOfPoint> contour = new ArrayList<>();
            contour.add(contours.get(i));

            Rect rect = Imgproc.boundingRect(contours.get(i));

            int xx = rect.x;
            int yy = rect.y;
            int hh = rect.height;
            int ww = rect.width;

            if(rect.x >= 10){ rect.x-=10; }
            if(rect.y >= 10){ rect.y-=10; }

            if(rect.x + rect.width + 20 < orig_img.cols()){ rect.width+=20; }
            else{rect.width=orig_img.cols()-rect.x;}

            if(rect.y + rect.height + 20 < orig_img.rows()){ rect.height+=20; }
            else{rect.height=orig_img.rows()-rect.y;}

            final int x = rect.x;
            final int y = rect.y;
            final int height = rect.height;
            final int width = rect.width;
            if(height > 25 && width > 25 && height < 250)
            {
                Imgproc.drawContours(orig_img, contour, 0, new Scalar(0,255,0), 1);
                RectangleRange rectangleRange = new RectangleRange(yy, yy+hh, xx, xx+ww);
                ranges.add(rectangleRange);
                Imgproc.rectangle(orig_img, rect.tl(), rect.br(), new Scalar(255,0,0), 2);
                try{
                    ///////////TODO: 1. Crop rectangle from original image
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri1);
                    Mat originImg = new Mat(originalBitmap.getWidth() ,originalBitmap.getHeight(), CvType.CV_8UC4);
                    Utils.bitmapToMat(originalBitmap, originImg);
                    Mat subImg = originImg.submat(rect);
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(subImg, bmp);
                    imageView3.setImageBitmap(bmp);

                    bmps[i] = Bitmap.createScaledBitmap(bmp, 224, 224, true);
                    croppedBitmaps.add(bmps[i]);




                    good_nums.add(i);

                    FirebaseModelInputs inputs = options_input.getEffnetInputs(bmps[i]);
                    effnet_interpreter.run(inputs, inputOutputOptions)
                            .addOnSuccessListener(
                                    new OnSuccessListener<FirebaseModelOutputs>() {
                                        @Override
                                        public void onSuccess(FirebaseModelOutputs result) {

                                            Point pnt = new Point(x + width/2 - 10, y + height/2 + 10);
                                            set_output_parameters(result, pnt);
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {    e.printStackTrace();     }
                                    });
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

        Log.e("asdf", "GOOD NUMBERS: " + good_nums.toString());

        imageView1.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
        imageView3.setVisibility(View.INVISIBLE);
    }

    private void set_output_parameters(FirebaseModelOutputs result, Point pnt)
    {
        bmpOutputs[counter] = result.getOutput(0);


        Character numb = (char)('A'+counter);
        String a = outputToString(bmpOutputs[counter]);

        res_all = res_all + numb + ": "+a + "\n";
        String text = "" + numb;

        int fontFace = 2;
        double fontScale = 1.0;
        Imgproc.putText(orig_changed, text, pnt, fontFace, fontScale, Scalar.all(255));

        Bitmap new_bit = Bitmap.createBitmap(304, 304, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(orig_changed, new_bit);


        imageView2.setImageBitmap(new_bit);
        TextView textView = findViewById(R.id.textView);
        textView.setText(res_all);
        Log.d("efficientNet", res_all);

        ImageView imageView = findViewById(R.id.image_view);
        imageView.setImageBitmap(new_bit);
        imageView.setVisibility(View.VISIBLE);

        iv = (ImageView)findViewById(R.id.image_view);
        if(iv!=null){
            iv.setOnTouchListener(mainActivity);
        }

        counter++;
    }

    private void cropImage(Uri photoUri) {
        /**
         *  갤러리에서 선택한 경우에는 tempFile 이 없으므로 새로 생성해줍니다.
         */
        try {
            switch (crop_num){
                case 1:
                    croppedFile1 = createImageFile();
                    break;
                case 2:
                    croppedFile2 = createImageFile();
                    break;
                case 3:
                    croppedFile3 = createImageFile();
                    break;
            }
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }

        //크롭 후 저장할 Uri
        switch (crop_num){
            case 1:
                cropUri1 = Uri.fromFile(croppedFile1);
                Crop.of(photoUri, cropUri1).withMaxSize(304, 304).start(this);
                break;
            case 2:
                cropUri2 = Uri.fromFile(croppedFile2);
                Crop.of(photoUri, cropUri2).withMaxSize(304, 304).start(this);
                break;
            case 3:
                cropUri3 = Uri.fromFile(croppedFile3);
                Crop.of(photoUri, cropUri3).withMaxSize(304, 304).start(this);
                break;
        }
//        Crop.of(photoUri, savingUri).withAspect(304, 304).start(this);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "croppedImage" + timeStamp + "_";

        File storageDir = new File(getFilesDir()+ "/sample_folder/");
        if (!storageDir.exists())
            try{
                storageDir.mkdirs();
            } catch (Exception e){
            }
        // 빈 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if(image_num==1){
                imageUri1 = data.getData();
                imageView1.setImageURI(imageUri1);
//                Cursor cursor = getContentResolver().query(imageUri1, null, null, null, null );
//                cursor.moveToNext();
//                filename = cursor.getString( cursor.getColumnIndex( "_data" ) );
//                cursor.close();
                imageButton.setVisibility(View.GONE);
                imageView1.setVisibility(View.VISIBLE);
                crop_num = 1;
                cropImage(imageUri1);
            }
            else if(image_num==2){
                imageUri2 = data.getData();
                imageView2.setImageURI(imageUri2);
                imageView2.setVisibility(View.VISIBLE);
                crop_num = 2;
                cropImage(imageUri2);
            }
            else if(image_num==3){
                imageUri3 = data.getData();
                imageView3.setImageURI(imageUri3);
                imageView3.setVisibility(View.VISIBLE);
                crop_num = 3;
                cropImage(imageUri3);
            }
        }
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            Log.e("asdfasdf", "22HERE WITH: " + crop_num);
            switch (crop_num){
                case 1:
                    imageView1.setImageURI(cropUri1);
                    Log.d("imageString", cropUri1.toString());
                    break;
                case 2:
                    imageView2.setImageURI(cropUri2);
                    Log.d("imageString", cropUri2.toString());
                    break;
                case 3:
                    imageView3.setImageURI(cropUri3);
                    Log.d("imageString", cropUri3.toString());
                    break;
            }
            try{
                switch (crop_num){
                    case 1:
                        croppedBitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri1);
                        uploadImage(croppedBitmap1, "img1");
                        break;
                    case 2:
                        croppedBitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri2);
                        uploadImage(croppedBitmap2, "img2");
                        break;
                    case 3:
                        croppedBitmap3 = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri3);
                        uploadImage(croppedBitmap3, "img3");
                        break;

                }
            } catch (Exception e){
                Log.e("except", String.valueOf(e));

            }
        }

        if(requestCode==1){
            if(resultCode==RESULT_OK){
                int result = data.getIntExtra("result", 1);
                if(result!=1) {
                    String data1 = data.getStringExtra("data");
                    String numb = data.getStringExtra("numb");
                    Bitmap bmp = data.getParcelableExtra("image");

                    Intent intent = new Intent(this, InfoActivity.class);
                    intent.putExtra("image", bmp);
                    intent.putExtra("numb", numb);
                    intent.putExtra("data", data1);
                    startActivity(intent);
                }
            }
        }
    }

    void uploadImage(Bitmap bitmap, String filename){
        StorageReference imageRef = storageRef.child("images/"+filename +".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            }
        });
    }

    String outputToString(float[][] op){
        ArrayList<EfficientOuput> eo = new ArrayList<>();

        String[] outputNames = {"Acne", "Actinic", "Atopic", "Bullous", "Cellulitis", "Eczema", "Exanthems", "Herpes",
                "Hives", "Light Disease", "Lupus", "Contact Dermititis", "Psoriasis", "Scabies",
                "Systemic", "Tinea", "Vasculitis", "Warts"};


        for(int i=0;i<18;i++){
            eo.add(new EfficientOuput(outputNames[i], op[0][i]));
        }

        Collections.sort(eo);



        String result = "";
        for(int i=0;i<4;i++){
            float data = eo.get(i).getData();
            data = data*100;
            String strData = new String();
            if(data>=1) strData = String.format("%.0f", data);
            else if(data>=0.1) strData = String.format("%.1f", data);
            else if(data>=0.01) strData = String.format("%.2f", data);
            else strData = String.format("%.3f", data);
            result = result + eo.get(i).getName() + ": " +strData+"%, ";
        }
        float data = eo.get(4).getData();
        data = data*100;
        String strData = new String();
        if(data>=1) strData = String.format("%.0f", data);
        else if(data>=0.1) strData = String.format("%.1f", data);
        else if(data>=0.01) strData = String.format("%.2f", data);
        else strData = String.format("%.3f", data);
        result = result + eo.get(4).getName() + ": " +strData+"%";

        return result;
    }

    String outputToData(float[][] op){
        ArrayList<EfficientOuput> eo = new ArrayList<>();

        String[] outputNames = {"Acne", "Actinic", "Atopic", "Bullous", "Cellulitis", "Eczema", "Exanthems", "Herpes",
                "Hives", "Light Disease", "Lupus", "Contact Dermititis", "Psoriasis", "Scabies",
                "Systemic", "Tinea", "Vasculitis", "Warts"};


        for(int i=0;i<18;i++){
            eo.add(new EfficientOuput(outputNames[i], op[0][i]));
        }

        String name= outputNames[0];
        float confidence = op[0][0];

        for(int i=1;i<18;i++){
            if(confidence < op[0][i]){
                confidence = op[0][i];
                name = outputNames[i];
            }
        }

        confidence *= 100;

        String result = "Name: "+name + "\nConfidence: ";
        if(confidence>=1) result = result + String.format("%.0f", confidence);
        else if(confidence>=0.1) result = result + String.format("%.1f", confidence);
        else result = result + String.format("%.2f", confidence);

        return result;
    }




    public boolean onTouch(View v, MotionEvent ev){
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
                    Toast.makeText(getApplicationContext(), numb+" ", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, PopupActivity.class);
                    intent.putExtra("image", croppedBitmaps.get(inNum));
                    intent.putExtra("numb", numb+"");
                    intent.putExtra("data", outputToData(bmpOutputs[inNum]));
                    startActivityForResult(intent, 1);
                }
                break;
        }
        return true;
    }


}