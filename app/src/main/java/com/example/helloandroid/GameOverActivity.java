package com.example.helloandroid;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

        TextView message = new TextView(this);
        message.setText("画面をタップしてタイトルに戻る");
        message.setTextColor(0xFFFFFFFF);
        message.setTextSize(18);
        message.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = 80;

        layout.addView(message, params);

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