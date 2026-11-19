package com.example.helloandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // =================================
    // リクエストコード
    // =================================

    private static final int REQUEST_PUZZLE = 1;
    private static final int REQUEST_NUM = 2;
    private static final int REQUEST_MAGIC = 3;
    private static final int REQUEST_GAME_OVER = 4;

    // =================================
    // 画面管理
    // =================================

    private FrameLayout container;
    private TextView arrow;

    private GameView gameView;
    private ImageView mapView;

    private AreaChange areaChange;

    private boolean isGameScreen = false;

    // ゲームオーバー画面の二重起動防止
    private boolean gameOverOpened = false;

    // =================================
    // タイトル画面
    // =================================

    private int selectedItem = 0;

    // =================================
    // ゲーム状態
    // =================================

    private boolean puzzleCleared = false;
    private boolean numPuzzleCleared = false;
    private boolean magicPuzzleCleared = false;

    private boolean firstFloorMode = false;
    private boolean hasKey = false;

    private float mapReturnX = 0f;
    private float mapReturnY = 0f;

    // =================================
    // Activity開始
    // =================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        areaChange = new AreaChange();

        showHomeMenu();
    }

    // =================================
    // タイトル画面
    // =================================

    private void showHomeMenu() {

        isGameScreen = false;
        selectedItem = 0;

        container = new FrameLayout(this);

        ImageView background = new ImageView(this);

        background.setImageResource(R.drawable.homemenu);
        background.setScaleType(ImageView.ScaleType.FIT_XY);

        container.addView(
                background,
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

        FrameLayout.LayoutParams arrowParams =
                new FrameLayout.LayoutParams(
                        100,
                        100
                );

        container.addView(arrow, arrowParams);

        setContentView(container);

        container.post(this::updateArrowPosition);
    }

    // =================================
    // 矢印の位置
    // =================================

    private void updateArrowPosition() {

        if (arrow == null || container == null) {
            return;
        }

        float width = container.getWidth();
        float height = container.getHeight();

        if (width == 0 || height == 0) {
            return;
        }

        float[] positions = {
                0.520f,
                0.59f,
                0.66f,
                0.73f
        };

        float arrowX = width * 0.35f;
        float arrowY = height * positions[selectedItem];

        arrow.setX(arrowX);
        arrow.setY(arrowY);
    }

    // =================================
    // キーボード操作
    // =================================

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        // ゲーム中の操作
        if (isGameScreen && gameView != null) {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {

                if (gameView.onKeyDown(
                        event.getKeyCode(),
                        event
                )) {
                    return true;
                }

            } else if (event.getAction() == KeyEvent.ACTION_UP) {

                if (gameView.onKeyUp(
                        event.getKeyCode(),
                        event
                )) {
                    return true;
                }
            }
        }

        // タイトル画面の操作
        if (!isGameScreen
                && event.getAction() == KeyEvent.ACTION_DOWN) {

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

    // =================================
    // メニュー選択
    // =================================

    private void selectMenuItem() {

        switch (selectedItem) {

            case 0:

                resetGame();
                showGame();

                break;

            case 1:

                Toast.makeText(
                        this,
                        "続きからはまだ実装されていません",
                        Toast.LENGTH_SHORT
                ).show();

                break;

            case 2:

                Toast.makeText(
                        this,
                        "設定はまだ実装されていません",
                        Toast.LENGTH_SHORT
                ).show();

                break;

            case 3:

                finish();

                break;
        }
    }

    // =================================
    // ゲーム状態の初期化
    // =================================

    private void resetGame() {

        puzzleCleared = false;
        numPuzzleCleared = false;
        magicPuzzleCleared = false;

        firstFloorMode = false;
        hasKey = false;

        mapReturnX = 0f;
        mapReturnY = 0f;

        gameOverOpened = false;
    }

    // =================================
    // ゲーム画面
    // =================================

    private void showGame() {

        isGameScreen = true;
        gameOverOpened = false;

        FrameLayout gameContainer = new FrameLayout(this);

        // =================================
        // マップ
        // =================================

        mapView = new ImageView(this);

        mapView.setImageResource(R.drawable.map);
        mapView.setScaleType(ImageView.ScaleType.FIT_XY);

        gameContainer.addView(
                mapView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        // =================================
        // GameView
        // =================================

        gameView = new GameView(this);

        // =================================
        // ゲームオーバー
        // =================================

        gameView.setOnGameOverListener(() -> {

            // 二重起動を防止
            if (gameOverOpened) {
                return;
            }

            gameOverOpened = true;

            // ゲーム画面ではなくする
            isGameScreen = false;

            // ゲームオーバー画面を起動
            Intent intent = new Intent(
                    MainActivity.this,
                    GameOverActivity.class
            );

            startActivityForResult(
                    intent,
                    REQUEST_GAME_OVER
            );
        });

        // =================================
        // パズル
        // =================================

        gameView.setOnPuzzleRequestListener(
                this::openPuzzle
        );

        gameView.setOnNumPuzzleRequestListener(
                this::openNumPuzzle
        );

        gameView.setOnMagicPuzzleRequestListener(
                this::openMagicPuzzle
        );

        // =================================
        // 鍵の取得
        // =================================

        gameView.setOnKeyRequestListener(() -> {

            if (!hasKey) {

                hasKey = true;

                gameView.setFirstFloorOpenMode(true);

                areaChange.changeArea(
                        mapView,
                        R.drawable.first_floar_open
                );

                Toast.makeText(
                        this,
                        "鍵を入手した！",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // =================================
        // 扉
        // =================================

        gameView.setOnDoorRequestListener(() -> {

            if (!firstFloorMode) {

                // マップから1階へ移動

                if (gameView.getWidth() > 0
                        && gameView.getHeight() > 0) {

                    mapReturnX =
                            gameView.getPlayerX()
                                    / gameView.getWidth();

                    mapReturnY =
                            gameView.getPlayerY()
                                    / gameView.getHeight();
                }

                // 1階の背景
                if (hasKey) {

                    areaChange.changeArea(
                            mapView,
                            R.drawable.first_floar_open
                    );

                    gameView.setFirstFloorOpenMode(true);

                } else {

                    areaChange.changeArea(
                            mapView,
                            R.drawable.first_floar
                    );

                    gameView.setFirstFloorOpenMode(false);
                }

                firstFloorMode = true;

                gameView.setFirstFloorMode(true);

                gameView.setPlayerPosition(
                        0.56f,
                        0.10f
                );

            } else {

                // 1階からマップへ戻る

                areaChange.changeArea(
                        mapView,
                        R.drawable.map_tobiranasi
                );

                firstFloorMode = false;

                gameView.setFirstFloorMode(false);
                gameView.setFirstFloorOpenMode(false);

                gameView.setPlayerPosition(
                        mapReturnX,
                        mapReturnY
                );
            }
        });

        // =================================
        // 玄関
        // =================================

        gameView.setOnExitRequestListener(() -> {

            if (hasKey) {

                openGameClear();

            } else {

                Toast.makeText(
                        this,
                        "鍵が必要だ！",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // =================================
        // GameViewを追加
        // =================================

        gameContainer.addView(
                gameView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(gameContainer);

        gameView.setFocusable(true);
        gameView.setFocusableInTouchMode(true);

        gameView.post(() -> gameView.requestFocus());
    }

    // =================================
    // パズル1
    // =================================

    private void openPuzzle() {

        if (gameView == null) {
            return;
        }

        gameView.setPuzzleActive(true);

        Intent intent = new Intent(
                MainActivity.this,
                PuzzleActivity2.class
        );

        startActivityForResult(
                intent,
                REQUEST_PUZZLE
        );
    }

    // =================================
    // パズル2
    // =================================

    private void openNumPuzzle() {

        if (gameView == null) {
            return;
        }

        gameView.setPuzzleActive(true);

        Intent intent = new Intent(
                MainActivity.this,
                num2.class
        );

        startActivityForResult(
                intent,
                REQUEST_NUM
        );
    }

    // =================================
    // パズル3
    // =================================

    private void openMagicPuzzle() {

        if (gameView == null) {
            return;
        }

        gameView.setPuzzleActive(true);

        Intent intent = new Intent(
                MainActivity.this,
                MagicSquareActivity.class
        );

        startActivityForResult(
                intent,
                REQUEST_MAGIC
        );
    }

    // =================================
    // パズル・ゲームオーバー終了
    // =================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        // =================================
        // ゲームオーバー終了後
        // =================================

        if (requestCode == REQUEST_GAME_OVER) {

            if (resultCode == RESULT_OK) {

                // 古いゲーム画面への参照を解除
                gameView = null;
                mapView = null;

                // タイトル画面へ戻る
                resetGame();
                showHomeMenu();
            }

            return;
        }

        // =================================
        // パズル終了後
        // =================================

        if (requestCode == REQUEST_PUZZLE
                || requestCode == REQUEST_NUM
                || requestCode == REQUEST_MAGIC) {

            if (gameView == null) {
                return;
            }

            gameView.setPuzzleActive(false);

            gameView.restorePuzzlePosition();

            // クリア状態を保存
            if (resultCode == RESULT_OK) {

                if (requestCode == REQUEST_PUZZLE) {

                    puzzleCleared = true;

                } else if (requestCode == REQUEST_NUM) {

                    numPuzzleCleared = true;

                } else if (requestCode == REQUEST_MAGIC) {

                    magicPuzzleCleared = true;
                }
            }

            // 3つのパズルをクリア
            if (puzzleCleared
                    && numPuzzleCleared
                    && magicPuzzleCleared) {

                Toast.makeText(
                        this,
                        "3つの謎が解けた！",
                        Toast.LENGTH_LONG
                ).show();

                if (mapView != null) {

                    areaChange.changeArea(
                            mapView,
                            R.drawable.map_tobiranasi
                    );
                }
            }
        }
    }

    // =================================
    // ゲームクリア
    // =================================

    private void openGameClear() {

         isGameScreen = false;

        Intent intent = new Intent(
                MainActivity.this,
                GameClearActivity.class
        );

        startActivity(intent);
    }

    // =================================
    // 戻るボタン
    // =================================

    @Override
    public void onBackPressed() {

        if (isGameScreen) {

            isGameScreen = false;

            showHomeMenu();

        } else {

            super.onBackPressed();
        }
    }
}