package com.example.helloandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout container;
    private TextView arrow;
    private int selectedItem = 0;

    private boolean isGameScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //ここから下は変更
        showHomeMenu();
    }

    private void showHomeMenu(){

        container = new FrameLayout(this);

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.homemenu);
        background.setScaleType(ImageView.ScaleType.FIT_XY);

        container.addView(background,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        arrow = new TextView(this);
        arrow.setText("→");
        arrow.setTextColor(Color.WHITE);
        arrow.setTextSize(24);
        arrow.setGravity(Gravity.CENTER);

        container.addView(arrow);

        setContentView(container);

        container.post(() ->updateArrowPosition());

    }

    private void updateArrowPosition() {
        if(arrow == null || container == null){
            return;
        }

        float width = container.getWidth();
        float height = container.getHeight();

        float[] updateArrowPosition = {
                0.52f,0.59f,0.66f,0.73f
        };

        float arrowX = width * 0.36f;
        float arrowY = height * updateArrowPosition[selectedItem];

        arrow.setX(arrowX);
        arrow.setY(arrowY);

        arrow.setLayoutParams(
                new FrameLayout.LayoutParams(
                        (int)(width * 0.07f),(int)(height * 0.09f)
                )
        );
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        if(isGameScreen){
            return super.dispatchKeyEvent(event);
        }
        if(event.getAction() == KeyEvent.ACTION_DOWN){

            switch (event.getKeyCode()){

                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_W:

                    selectedItem = (selectedItem + 3) % 4;
                    updateArrowPosition();
                    return true;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_S:

                    selectedItem = (selectedItem + 1) % 4;
                    updateArrowPosition();
                    return true;

                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:

                    selectMenuItem();
                    return true;

            }

        }
        return super.dispatchKeyEvent(event);
    }

    private void selectMenuItem(){

        switch (selectedItem){

            case 0:

                showGame();
                break;

            case 1://続きから

                break;

            case 2://設定

                break;

            case 3://やめる

                finish();
                break;
        }
    }

    private void showGame(){

        FrameLayout gameContainer = new FrameLayout(this);

        ImageView map = new ImageView(this);
        map.setImageResource(R.drawable.map);
        map.setScaleType(ImageView.ScaleType.FIT_XY);

        gameContainer.addView(
                map,new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        GameView gameView = new GameView(this);

        gameContainer.addView(gameView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        isGameScreen = true;

        setContentView(gameContainer);

        gameView.setFocusableInTouchMode(true);
        gameView.requestFocus();
    }

}