package com.example.helloandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showGameOverScreen();
    }

    private void showGameOverScreen() {

        FrameLayout layout = new FrameLayout(this);

        layout.setBackgroundColor(0xFF000000);

        // ゲームオーバー画像
        ImageView background = new ImageView(this);

        background.setImageResource(R.drawable.gameover);

        background.setScaleType(ImageView.ScaleType.FIT_XY);

        layout.addView(
                background,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        // 画像をタップするとタイトルに戻る
        background.setOnClickListener(v -> returnToTitle());

        layout.setOnClickListener(v -> returnToTitle());

        setContentView(layout);
    }

    private void returnToTitle() {

        setResult(RESULT_OK);

        finish();
    }

    @Override
    public void onBackPressed() {
        returnToTitle();
    }
}