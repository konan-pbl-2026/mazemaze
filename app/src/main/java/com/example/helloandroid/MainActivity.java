
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

    private GameView gameView;
    private ImageView mapView;
    private AreaChange areaChange;
    private static final int REQUEST_PUZZLE = 1;
    private boolean isGameScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        areaChange = new AreaChange();

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

        container.post(() -> updateArrowPosition());

    }

    private void updateArrowPosition() {
        if(arrow == null || container == null){
            return;
        }

        float width = container.getWidth();
        float height = container.getHeight();

        float[] updateArrowPosition = {
                0.520f,0.59f,0.66f,0.73f
        };

        float arrowX = width * 0.35f;
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
        android.util.Log.d("INPUT_TEST","MainActivity received:" + event.getKeyCode());

        if (isGameScreen && gameView != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (gameView.onKeyDown(event.getKeyCode(), event)) {
                    return true;
                }
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (gameView.onKeyUp(event.getKeyCode(), event)) {
                    return true;
                }
            }
        }

        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {

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

                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:

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

        mapView = new ImageView(this);
        mapView.setImageResource(R.drawable.map);
        mapView.setScaleType(ImageView.ScaleType.FIT_XY);

        gameContainer.addView(
                mapView,new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        gameView = new GameView(this);
        gameView.setOnPuzzleRequestListener(() -> openPuzzle());

        gameContainer.addView(gameView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        isGameScreen = true;

        setContentView(gameContainer);

        gameView.setFocusable(true);
        gameView.setFocusableInTouchMode(true);

        gameView.post(() -> {
            gameView.requestFocus();
        });
    }

    private void openPuzzle() {
        gameView.setPuzzleActive(true);

        // ★ PuzzleActivity2 を使っている場合はここを PuzzleActivity2.class に合わせる
        Intent intent = new Intent(MainActivity.this, PuzzleActivity2.class);
        startActivityForResult(intent, REQUEST_PUZZLE);
    }

    public void changeToPuzzleReturnMap() {

        areaChange.changeArea(
                mapView,
                R.drawable.map_tobiranasi
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PUZZLE) {

            gameView.setPuzzleActive(false);
            gameView.restorePuzzlePosition();

            if (resultCode == RESULT_OK && mapView != null) {
                mapView.post(() -> {
                    areaChange.changeArea(
                            mapView,
                            R.drawable.map_tobiranasi
                    );
                    mapView.invalidate();
                });
                android.widget.Toast.makeText(this, "扉が開放された！", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

}
