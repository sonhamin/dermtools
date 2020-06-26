package org.techtown.mnist_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;
import com.theartofdev.edmodo.cropper.CropImage;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    ImageButton imageButton;
//    ImageView imageView;
    ImageView imageView1, imageView2, imageView3;
    Button cropButton, analysisButton, addButton;

    private final int GET_GALLERY_IMAGE = 200;
    private final int EFFICIENT_NET_IMAGE = 100;
    private static final String TAG = "MainActivity";
    Boolean isSelected = false;

    int image_num = 0;
    int crop_num = 0;

    Uri imageUri1, imageUri2, imageUri3;
    Uri cropUri1, cropUri2, cropUri3;

//    Bitmap croppedBitmap = null;
    Bitmap croppedBitmap1 = null, croppedBitmap2 = null, croppedBitmap3 = null;
    File croppedFile1, croppedFile2, croppedFile3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 상태바를 안보이도록 합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 화면 켜진 상태를 유지합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        imageButton = findViewById(R.id.image_select_btn);
//        imageView = findViewById(R.id.image_view);
        imageView1 = findViewById(R.id.image_1);
        imageView2 = findViewById(R.id.image_2);
        imageView3 = findViewById(R.id.image_3);
        cropButton = findViewById(R.id.crop_btn);
        analysisButton = findViewById(R.id.classify_btn);
        addButton = findViewById(R.id.add_btn);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(image_num<=2){
                    image_num ++;
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, GET_GALLERY_IMAGE);
                }else{
                    Toast.makeText(getApplicationContext(), "no more image", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        imageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//                startActivityForResult(intent, GET_GALLERY_IMAGE);
//            }
//        });
//
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//                startActivityForResult(intent, GET_GALLERY_IMAGE);
//            }
//        });

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                Log.d("asdfasf", "`");
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                Log.d("asdfasf", "-1");
                startActivityForResult(intent, EFFICIENT_NET_IMAGE);
            }
        });

        analysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(croppedBitmap1 != null && croppedBitmap2 != null && croppedBitmap3 != null){
                    int[] bitWidth = new int[3];
                    int[] bitHeight = new int[3];
                    bitWidth[0] = croppedBitmap1.getWidth();
                    bitWidth[1] = croppedBitmap2.getWidth();
                    bitWidth[2] = croppedBitmap3.getWidth();
                    bitHeight[0] = croppedBitmap1.getHeight();
                    bitHeight[1] = croppedBitmap2.getHeight();
                    bitHeight[2] = croppedBitmap3.getHeight();
                    int[] coverImageIntArray1D1 = new int[bitWidth[0] * bitHeight[0]];
                    int[] coverImageIntArray1D2 = new int[bitWidth[1] * bitHeight[1]];
                    int[] coverImageIntArray1D3 = new int[bitWidth[2] * bitHeight[2]];
                    croppedBitmap1.getPixels(coverImageIntArray1D1, 0, bitWidth[0], 0, 0, bitWidth[0], bitHeight[0]);
                    croppedBitmap2.getPixels(coverImageIntArray1D2, 0, bitWidth[1], 0, 0, bitWidth[1], bitHeight[1]);
                    croppedBitmap3.getPixels(coverImageIntArray1D3, 0, bitWidth[2], 0, 0, bitWidth[2], bitHeight[2]);

                    Log.d("asdasdasdasd", bitWidth[0] + ", "+bitHeight[0]+" and "+bitWidth[1] + ", "+bitHeight[1]+" and "+bitWidth[2] + ", "+bitHeight[2]);

                    float[][][][][] input = new float[3][1][304][304][3];
                    Map<Integer, Object> outputs = new HashMap<>();

                    float[][][][] output1 = new float[1][304][304][1];
                    outputs.put(0,output1);

                    if(bitHeight[0] == 304 && bitWidth[0] == 304 && bitHeight[1] == 304 && bitWidth[1] == 304 && bitHeight[2] == 304 &&bitWidth[2] == 304){
                        for(int i=0;i<304;i++){
                            for(int j=0;j<304;j++){
                                int rgb1 = coverImageIntArray1D1[i*304+j];
                                int rgb2 = coverImageIntArray1D2[i*304+j];
                                int rgb3 = coverImageIntArray1D3[i*304+j];
                                int R1 = Color.red(rgb1);
                                int G1 = Color.green(rgb1);
                                int B1 = Color.blue(rgb1);
                                int R2 = Color.red(rgb2);
                                int G2 = Color.green(rgb2);
                                int B2 = Color.blue(rgb2);
                                int R3 = Color.red(rgb3);
                                int G3 = Color.green(rgb3);
                                int B3 = Color.blue(rgb3);
                                input[0][0][i][j][0] = (float) (R1 / 255.0);
                                input[0][0][i][j][1] = (float) (G1 / 255.0);
                                input[0][0][i][j][2] = (float) (B1 / 255.0);
                                input[1][0][i][j][0] = (float) (R2 / 255.0);
                                input[1][0][i][j][1] = (float) (G2 / 255.0);
                                input[1][0][i][j][2] = (float) (B2 / 255.0);
                                input[2][0][i][j][0] = (float) (R3 / 255.0);
                                input[2][0][i][j][1] = (float) (G3 / 255.0);
                                input[2][0][i][j][2] = (float) (B3 / 255.0);

                            }
                        }
                        Interpreter tflite = getTfliteInterpreter("real_unet.tflite");
                        // 모델 구동.
                        // 정확하게는 from_session 함수의 output_tensors 매개변수에 전달된 연산 호출
//                        tflite.run(input, output);
                        tflite.runForMultipleInputsOutputs(input, outputs);
                        Log.d("masdaasdasd", "완료");

                        Bitmap bitmap = Bitmap.createBitmap(304, 304, Bitmap.Config.ARGB_8888);
                        int one = 0, zero = 0;
                        for(int i=0;i<304;i++){
                            for(int j=0;j<304;j++){
                                Log.d("asdasdasd", i+", "+j+": "+output1[0][i][j][0]);
                                if(output1[0][i][j][0]>=0.15){
                                    one ++;
                                    bitmap.setPixel(j, i, Color.WHITE);
                                }else{
                                    zero++;
                                    bitmap.setPixel(j, i, Color.BLACK);
                                }
                            }
                        }
                        Log.d("asdasdasd", "one: "+ one+" & zero: "+zero);
                        imageView3.setImageBitmap(bitmap);
                    } else{
                        Toast.makeText(getApplicationContext(), "image is small", Toast.LENGTH_SHORT).show();
                    }
//                    Log.d("croppedBitmap", bitHeight + ", " + bitWidth);
//                    }
                }
            }
        });
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
        // 이미지 파일 이름 ( blackJin_{시간}_ )
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
                        break;
                    case 2:
                        croppedBitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri2);
                        break;
                    case 3:
                        croppedBitmap3 = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri3);
                        break;

                }
            } catch (Exception e){

            }
        }
        if (requestCode == EFFICIENT_NET_IMAGE && resultCode == RESULT_OK){
            Log.d("asdfasf", "-1");
            Uri enUri = data.getData();
            Log.d("asdfasf", "0");
            try{
                Log.d("asdfasf", "1");
                Bitmap enBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), enUri);
                Bitmap enResizeBitmap = Bitmap.createScaledBitmap(enBitmap, 224, 224, true);
                Log.d("asdfasdf", enResizeBitmap.getWidth() + ", " + enResizeBitmap.getHeight());
                Log.d("asdfasf", "2");
                float [][][][] input = new float[1][224][224][3];

                Map<Integer, Object> outputs = new HashMap<>();
                float[][] output1 = new float[1][18];
                outputs.put(0,output1);
                Log.d("asdfasf", "3");
                if(enResizeBitmap.getWidth()==224 && enResizeBitmap.getHeight()==224){
                    int[] enBitmapIntArray = new int[224*224];
                    Log.d("asdfasf", "4");
                    enResizeBitmap.getPixels(enBitmapIntArray, 0, 224, 0, 0, 224,224);
                    for(int i=0;i<224;i++){
                        for(int j=0;j<224;j++){
                            int rgb = enBitmapIntArray[i*224+j];
                            int R = Color.red(rgb);
                            int G = Color.green(rgb);
                            int B = Color.blue(rgb);
                            input[0][i][j][0] = R;
                            input[0][i][j][1] = G;
                            input[0][i][j][2] = B;
                        }
                    }
                    Log.d("asdfasf", "5");
                    Interpreter tflite = getTfliteInterpreter("real_efficientnet.tflite");
                    // 모델 구동.
                    // 정확하게는 from_session 함수의 output_tensors 매개변수에 전달된 연산 호출
                    Log.d("asdfasf", "6");
//                    tflite.runForMultipleInputsOutputs(input, outputs);
                    tflite.run(input, output1);
                    Log.d("asdfasf", "7");

                    ImageView imageView = findViewById(R.id.image_view);
                    imageView.setImageBitmap(enResizeBitmap);
                    imageView.setVisibility(View.VISIBLE);
                    imageButton.setVisibility(View.GONE);
                    String outputString = new String();
                    for(int i=0;i<17;i++) {
                        outputString = outputString + Float.toString(output1[0][i]) +", ";
                    }
                    outputString = outputString + Float.toString(output1[0][17]);
                    TextView textView = findViewById(R.id.textView);
                    textView.setText(outputString);
                }
            } catch (Exception e){
                Log.d("asdfasd", e.toString());
            }
        }
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}