package org.techtown.mnist_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class AddPhotoPopup extends Activity {

    ImageView imageView1;
    ImageView imageView2;
    ImageView close_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_photo_popup);
    }

    public void mOnClose(View v){
        Intent intent = new Intent();
        intent.putExtra("result", 1);
        setResult(RESULT_OK, intent);

        finish();
    }

    public void mOnCamera(View v){
        Intent intent = new Intent();
        intent.putExtra("result", 2);
        setResult(RESULT_OK, intent);

        finish();
    }

    public void mOnGallery(View v){
        Intent intent = new Intent();
        intent.putExtra("result", 3);
        setResult(RESULT_OK, intent);

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
