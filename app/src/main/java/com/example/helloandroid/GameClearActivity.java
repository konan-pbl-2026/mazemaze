package com.example.helloandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;

public class GameClearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 縦画面に固定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ImageView imageView = new ImageView(this);

        imageView.setImageResource(R.drawable.gameclear);

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        setContentView(imageView);
    }
}