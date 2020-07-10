package org.techtown.mnist_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {
    TextView textView;
    ImageView imageView;

    Bitmap bmp;
    String data;
    String numb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        //UI 객체생성
        textView = (TextView)findViewById(R.id.infoText);
        imageView = (ImageView)findViewById(R.id.infoImage);

        //데이터 가져오기
        Intent intent = getIntent();
        data = intent.getStringExtra("data");
        numb = intent.getStringExtra("numb");
        bmp = intent.getParcelableExtra("image");

        textView.setText(data);
        imageView.setImageBitmap(bmp);
    }
}
