package org.techtown.mnist_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class PopupActivity extends Activity {

    TextView txtText;
    ImageView imageView;
    TextView noticeText;

    Bitmap bmp;
    String data;
    String numb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup);

        //UI 객체생성
        txtText = (TextView)findViewById(R.id.txtText);
        imageView = (ImageView)findViewById(R.id.popup_imageView);
        noticeText = (TextView)findViewById(R.id.notice);

        //데이터 가져오기
        Intent intent = getIntent();
        data = intent.getStringExtra("data");
        numb = intent.getStringExtra("numb");
        bmp = intent.getParcelableExtra("image");

        noticeText.setText(numb);
        txtText.setText(data);
        imageView.setImageBitmap(bmp);
    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", 1);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    public void mOnMoreInfo(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", 2);
        intent.putExtra("image", bmp);
        intent.putExtra("numb", numb);
        intent.putExtra("data", data);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}
