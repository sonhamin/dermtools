package org.techtown.mnist_sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    ImageView select_btn, analyze_btn;
    ImageView imageView, iv;
    TextView select_text;
    TextView textView;
    ImageView an_img;
    TextView an_text;
    FrameLayout frameLayout;
    View view;
    int counter;
    ArrayList<RectangleRange> ranges = new ArrayList<>();

    private final int GET_GALLERY_IMAGE = 200;
    private final int GET_CAMERA_IMAGE = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 672;

    Preprocessor preprocessor;
    float[][][] bmpOutputs;
    float[][][][] input1;
    float[][][][] input2;
    float[][][][] input3;

    Uri imageUri;
    Uri cropUri;
    Uri cameraUri;
    String cameraFilePath;
    private MediaScanner mMediaScanner;

    File file;

    Bitmap mask_bitmap;
    Bitmap resizedBitmap;

    Bitmap croppedBitmap1 = null, croppedBitmap2 = null, croppedBitmap3 = null;
    File croppedFile;

    ArrayList<Bitmap> croppedBitmaps = new ArrayList<>();
    MainActivity mainActivity;

    FileManager fileManager;
    DeviceInfo deviceInfo;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();
    OptionsInput options_input;
    // Create a child reference
    // imagesRef now points to "images"
    StorageReference imagesRef = storageRef.child("images");
    private DatabaseReference mDatabase;
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private static final String TAG = "PhoneState";

    Boolean isAnalyzed = false;
    Boolean fromGall = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_menu:
                Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_per:
                Toast.makeText(this, "2", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainActivity = this;
        fileManager = new FileManager(getApplication());

        // 화면 켜진 상태를 유지합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMediaScanner = MediaScanner.getInstance(getApplicationContext());
        mDatabase = FirebaseDatabase.getInstance().getReference();
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

        FirebaseModelManager.getInstance().download(remoteModel_unet, conditions)
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
                });


        select_text = findViewById(R.id.text_select);
        imageView = findViewById(R.id.image_view);
        select_btn = findViewById(R.id.imageView);
        analyze_btn = findViewById(R.id.imageView2);
        textView = findViewById(R.id.textView);
        an_img = findViewById(R.id.analyzing_view_image);
        an_text = findViewById(R.id.analyzing_view_text);
        frameLayout = findViewById(R.id.analyzing_view);
        view = findViewById(R.id.view);

        deviceInfo = new DeviceInfo(Build.BOARD, Build.BRAND, Build.CPU_ABI, Build.DEVICE, Build.DISPLAY,
                Build.FINGERPRINT, Build.HOST, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), Build.MANUFACTURER, Build.MODEL, Build.PRODUCT,
                Build.TAGS, Build.TYPE, Build.USER, Build.VERSION.RELEASE);


        deviceInfo.logging();

        select_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity, AddPhotoPopup.class);
                startActivityForResult(intent, 2);
            }
        });

        analyze_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAnalyzed){
                    Toast.makeText(getApplication(), "이미 실행되었습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(croppedBitmap1 != null){
                    isAnalyzed = true;
                    int bithw = 304;
                    resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap1, 304, 304, true);

                    preprocessor = new Preprocessor();
                    Bitmap[] combs = preprocessor.decompose(resizedBitmap);
                    croppedBitmap2 = combs[0];
                    croppedBitmap3 = combs[1];

                    float [][][][][] combs_input = preprocessor.make_inputs_unet(bithw, resizedBitmap, croppedBitmap2, croppedBitmap3);
                    input1 = combs_input[0];
                    input2 = combs_input[1];
                    input3 = combs_input[2];

                    frameLayout.bringToFront();
                    view.setVisibility(View.VISIBLE);
                    an_img.setVisibility(View.VISIBLE);
                    an_text.setVisibility(View.VISIBLE);
                    init_Unet();
                }
            }

            private void init_Unet() {
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
                                            mask_bitmap = preprocessor.maskBitmap(output2);

//                                            imageView3.setImageBitmap(mask_bitmap);
                                            init_efficientnet();
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

            private void init_efficientnet() {
                FirebaseModelManager.getInstance().isModelDownloaded(remoteModel_effnet)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean isDownloaded) {
                                FirebaseModelInterpreterOptions options2;
                                if (isDownloaded) {               options2 = new FirebaseModelInterpreterOptions.Builder(remoteModel_effnet).build();                  }
                                else {                            options2 = new FirebaseModelInterpreterOptions.Builder(localModel_effnet).build();                   }

                                FirebaseModelInterpreter effnet_interpreter = null;
                                try {
                                    effnet_interpreter = FirebaseModelInterpreter.getInstance(options2);
                                } catch (FirebaseMLException e) {
                                    e.printStackTrace();
                                }
                                FirebaseModelInputOutputOptions inputOutputOptions = options_input.getEffnetOptions();
                                classify(effnet_interpreter, inputOutputOptions);
                            }
                        });
            }
        });
    }

    String res_all = "";
    Mat orig_changed;

    List<MatOfPoint> contours;
    Bitmap[] bmps;

    Bitmap new_bit;

    private void classify(FirebaseModelInterpreter effnet_interpreter, FirebaseModelInputOutputOptions inputOutputOptions) {
        contours = preprocessor.get_contours(mask_bitmap);
        orig_changed = preprocessor.getResizedMat(resizedBitmap);
        bmpOutputs = preprocessor.initBmpOutputs(contours);
        bmps = preprocessor.initBmps(contours);


        for (int i=0; i<contours.size(); i++) {

            List<MatOfPoint> contour = new ArrayList<>();
            contour.add(contours.get(i));

            Rect rect = Imgproc.boundingRect(contours.get(i));
            int [] originalDims = preprocessor.getOriginalDims(rect);
//            rect = preprocessor.adjustRectangles(rect, orig_changed);

            final int x = rect.x;
            final int y = rect.y;
            final int height = rect.height;
            final int width = rect.width;

            if(height > 25 && width > 25 && height < 250)
            {

                Log.e("asdf", "x: " + x + " y: " + y + " height: " + height + " width: " + width);
                preprocessor.drawContourRects(orig_changed, contour, rect);

                RectangleRange rectangleRange = preprocessor.getRectangleRange(originalDims);
                ranges.add(rectangleRange);

                bmps[i] = preprocessor.crop_segments(getContentResolver(), imageUri, rect, width, height);
                croppedBitmaps.add(bmps[i]);
                Log.d("croppedBitmap", i+": "+width+", "+height+" || "+rect);
                Log.d("crooppedBitmap Size", i+": "+bmps[i].getWidth()+", "+bmps[i].getHeight());
                Log.d("crooppedBitmap Size", i+": "+preprocessor.crop_segments(getContentResolver(), imageUri, rect, width, height).getWidth()+", "+preprocessor.crop_segments(getContentResolver(), imageUri, rect, width, height).getHeight());

                float [][][][] effnet_input = preprocessor.make_inputs_effnet(bmps[i]);
                FirebaseModelInputs inputs = options_input.getEffnetInputs(effnet_input);
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
            }
        }

//        imageView1.setVisibility(View.INVISIBLE);
//        imageView2.setVisibility(View.INVISIBLE);
//        imageView3.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                an_img.setVisibility(View.INVISIBLE);
                an_text.setVisibility(View.INVISIBLE);
                view.setVisibility(View.INVISIBLE);
                update_firebase();

            }
        }, 1000);

    }

    private void set_output_parameters(FirebaseModelOutputs result, Point pnt) {
        bmpOutputs[counter] = result.getOutput(0);

        Character numb = (char)('A'+counter);
        EfficientOuput efficientOuput = new EfficientOuput();
        String a = efficientOuput.outputToString(bmpOutputs[counter]);

        res_all = res_all + numb + ": "+a + "\n";
        String text = "" + numb;

        int fontFace = 2;
        double fontScale = 1.0;
        Imgproc.putText(orig_changed, text, pnt, fontFace, fontScale, Scalar.all(255));

        new_bit = Bitmap.createBitmap(304, 304, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(orig_changed, new_bit);

//        imageView2.setImageBitmap(new_bit);
        textView.setText(res_all);
        textView.setMovementMethod(new ScrollingMovementMethod());
        Log.d("efficientNet", res_all);

        imageView.setImageBitmap(new_bit);
        imageView.setVisibility(View.VISIBLE);
        iv = (ImageView)findViewById(R.id.image_view);
        if(iv!=null){
            iv.setOnTouchListener(mainActivity);
        }

//        ImageView iv = (ImageView)findViewById(R.id.image_view);
//        if(iv!=null){
//            iv.setOnTouchListener(mainActivity);
//        }

        counter++;
    }

    private void update_firebase(){
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");
        String time = sdf.format(dt).toString();
        OriginalInfo originalInfo = new OriginalInfo();
        mDatabase.updateChildren(originalInfo.postInfo(deviceInfo.getId(), time));
        fileManager.uploadImageOrigin(croppedBitmap1, "original", deviceInfo.getId(), time);
        for(int i=0;i<croppedBitmaps.size();i++){
            double area = croppedBitmaps.get(i).getWidth() * croppedBitmaps.get(i).getHeight();
            int[] pixels = new int[(int)area];
            croppedBitmaps.get(i).getPixels(pixels, 0, croppedBitmaps.get(i).getWidth(), 0, 0, croppedBitmaps.get(i).getWidth(), croppedBitmaps.get(i).getHeight());
            double R = 0;
            double G = 0;
            double B = 0;
            for(int j=0;j<(int)area;j++){
                R += Color.red(pixels[j]);
                G += Color.green(pixels[j]);
                B += Color.blue(pixels[j]);
            }
            R /= area;
            G /= area;
            B /= area;
            CropInfo cropInfo = new CropInfo(area, R, G, B);
            String parameter = (char)('A'+i)+"";
            mDatabase.updateChildren(cropInfo.postInfo(deviceInfo.getId(), time, parameter));
            fileManager.uploadImageOrigin(croppedBitmaps.get(i), parameter, deviceInfo.getId(), time);
        }
        mDatabase.updateChildren(deviceInfo.postInfo());

    }
    private void cropImage(Uri photoUri) {
        /**
         *  갤러리에서 선택한 경우에는 tempFile 이 없으므로 새로 생성해줍니다.
         */
        try {
            croppedFile = fileManager.createImageFile("croppedFile");
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        //크롭 후 저장할 Uri
        cropUri = Uri.fromFile(croppedFile);
        Crop.of(photoUri, cropUri).withMaxSize(304, 304).start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK){
            int result = data.getIntExtra("result", 1);
            switch (result){
                case 2:
                    camera();
                    break;
                case 3:
                    //Clear previous inputs
                    counter=0;
//                    imageView.setImageDrawable(null);
//                    textView.setText("");
                    res_all="";
                    //~Clear previous inputs

                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, GET_GALLERY_IMAGE);
                    break;
            }
        }
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
            select_text.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            fromGall = true;
            cropImage(imageUri);
        }
        if (requestCode == GET_CAMERA_IMAGE && resultCode == RESULT_OK){
//            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageUri = Uri.fromFile(file);
            imageView.setImageURI(imageUri);
            select_text.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            cropImage(imageUri);
        }
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            //imageView1.setImageURI(cropUri);
            Log.d("imageString", cropUri.toString());
            isAnalyzed = false;
            try{
                croppedBitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri);
                if(fromGall){
                    ExifInterface exif = null;

                    try{
                        exif = new ExifInterface(cropUri.getPath());
                    } catch (IOException e){
                        e.printStackTrace();
                    }

                    int exifOrientation;
                    int exifDegree;

                    if(exif != null){
                        exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        exifDegree = exifOrientationToDegress(exifOrientation);
                    } else{
                        exifDegree = 0;
                    }
                    croppedBitmap1 = rotate(croppedBitmap1, exifDegree);
                    imageView.setImageBitmap(croppedBitmap1);
                }


                fileManager.uploadImage(croppedBitmap1, "img1");
            } catch (Exception e){
                Log.e("except", String.valueOf(e));
            }
        }
        if (requestCode==1){
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bitmap bitmap = BitmapFactory.decodeFile(cameraFilePath);
            ExifInterface exif = null;

            try{
                exif = new ExifInterface(cameraFilePath);
            } catch (IOException e){
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if(exif != null){
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegress(exifOrientation);
            } else{
                exifDegree = 0;
            }

            imageView.setImageBitmap(rotate(bitmap,exifDegree));
            imageView.setVisibility(View.VISIBLE);
            select_text.setVisibility(View.GONE);
            imageUri = fileManager.getImageUri(getApplicationContext(), rotate(bitmap,exifDegree));
            cropImage(imageUri);
        }
    }

    private int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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
                Log.d("ranges", ranges.size()+"");
                Log.d("ranges_ev", evX+", "+evY);
                Log.d("ranges_imSize1", imSize+"");
                Log.d("ranges_imSize2", imageView.getLayoutParams().width+"");
                Log.d("ranges_change", changeX+", "+changeY);
                for(int i=0;i<ranges.size();i++){
                    if(ranges.get(i).isIn(changeX, changeY)){
                        Log.d("ranges", i+"");
                        inNum = i;
                        break;
                    }
                }
                Log.d("ranges", inNum+"");
                if(inNum==-1){
                    Log.d("ranges", "not inside contour   : " + ranges.size());
                }
                else{
                    Character numb = (char)('A'+inNum);
                    EfficientOuput efficientOuput = new EfficientOuput();
                    Intent intent = new Intent(this, PopupActivity.class);
                    intent.putExtra("image", croppedBitmaps.get(inNum));
                    intent.putExtra("numb", numb+"");
                    intent.putExtra("data", efficientOuput.outputToData(bmpOutputs[inNum]));
                    startActivityForResult(intent, 1);
                }
                break;
        }
        return true;
    }

    public void camera(){
        fromGall = false;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            File cameraFile = null;
            try{
                cameraFile = fileManager.createImageFile2();
                cameraFilePath = cameraFile.getAbsolutePath();
            } catch(IOException e){

            }
            if(cameraFile != null){
                cameraUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), cameraFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
}