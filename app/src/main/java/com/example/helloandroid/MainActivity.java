package com.example.helloandroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    // =========================
    // ナンプレの正解
    // =========================

    private final int[][] solution = {
            {5,3,4,6,7,8,9,1,2},
            {6,7,2,1,9,5,3,4,8},
            {1,9,8,3,4,2,5,6,7},
            {8,5,9,7,6,1,4,2,3},
            {4,2,6,8,5,3,7,9,1},
            {7,1,3,9,2,4,8,5,6},
            {9,6,1,5,3,7,2,8,4},
            {2,8,7,4,1,9,6,3,5},
            {3,4,5,2,8,6,1,7,9}
    };

    // =========================
    // 初級問題
    // =========================

    private final int[][] easyPuzzle = {
            {5,3,0,0,7,0,0,0,0},
            {6,0,0,1,9,5,0,0,0},
            {0,9,8,0,0,0,0,6,0},
            {8,0,0,0,6,0,0,0,3},
            {4,0,0,8,0,3,0,0,1},
            {7,0,0,0,2,0,0,0,6},
            {0,6,0,0,0,0,2,8,0},
            {0,0,0,4,1,9,0,0,5},
            {0,0,0,0,8,0,0,7,9}
    };

    // =========================
    // 中級問題
    // =========================

    private final int[][] normalPuzzle = {
            {5,3,0,0,0,0,0,0,0},
            {6,0,0,1,0,0,0,0,0},
            {0,9,8,0,0,0,0,6,0},
            {8,0,0,0,6,0,0,0,3},
            {4,0,0,8,0,3,0,0,1},
            {7,0,0,0,2,0,0,0,6},
            {0,6,0,0,0,0,2,8,0},
            {0,0,0,4,0,9,0,0,5},
            {0,0,0,0,8,0,0,7,9}
    };

    // =========================
    // 上級問題
    // =========================

    private final int[][] hardPuzzle = {
            {0,0,0,0,7,0,0,0,0},
            {6,0,0,0,0,5,0,0,0},
            {0,9,0,0,0,0,0,6,0},
            {0,0,9,0,6,0,0,0,0},
            {4,0,0,0,0,3,0,0,1},
            {0,0,0,0,2,0,8,0,0},
            {0,6,0,0,0,0,0,8,0},
            {0,0,0,4,0,0,0,0,5},
            {0,0,0,0,8,0,0,0,0}
    };

    // =========================
    // ゲーム変数
    // =========================

    private int[][] puzzle = new int[9][9];
    private int[][] board = new int[9][9];
    private boolean[][] fixed = new boolean[9][9];

    private Button[][] cells = new Button[9][9];

    private int selectedRow = -1;
    private int selectedCol = -1;

    private int mistakes = 0;
    private int difficulty = 0;

    // 制限時間15分
    private static final int TIME_LIMIT_SECONDS = 15 * 60;

    // ヒント使用による時間減少
    private int hintPenaltySeconds = 0;

    // ゲーム終了判定
    private boolean gameOver = false;

    private long startTime;

    private TextView timerText;
    private TextView mistakeText;
    private TextView statusText;
    private TextView difficultyText;

    private GridLayout grid;

    // Game Over画面
    private FrameLayout gameOverOverlay;
    private TextView gameOverDetailText;

    private final Handler handler = new Handler();

    // =========================
    // タイマー
    // =========================

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            updateTimer();

            if (!gameOver) {
                handler.postDelayed(this, 1000);
            }
        }
    };

    // =========================
    // タイマー更新
    // =========================

    private void updateTimer() {

        if (gameOver) {
            return;
        }

        long elapsed =
                (System.currentTimeMillis() - startTime) / 1000;

        long remaining =
                TIME_LIMIT_SECONDS - elapsed - hintPenaltySeconds;

        // 時間切れ
        if (remaining <= 0) {

            if (timerText != null) {
                timerText.setText("00:00");
            }

            showGameOver("制限時間がなくなりました。");
            return;
        }

        long minutes = remaining / 60;
        long seconds = remaining % 60;

        if (timerText != null) {

            timerText.setText(
                    String.format("%02d:%02d", minutes, seconds)
            );
        }
    }

    // =========================
    // Game Over表示
    // =========================

    private void showGameOver(String message) {

        if (gameOver) {
            return;
        }

        gameOver = true;

        handler.removeCallbacks(timerRunnable);

        if (statusText != null) {
            statusText.setText("Game Over");
        }

        if (gameOverDetailText != null) {
            gameOverDetailText.setText(message);
        }

        if (gameOverOverlay != null) {
            gameOverOverlay.setVisibility(View.VISIBLE);
        }

        Toast.makeText(
                this,
                "GAME OVER",
                Toast.LENGTH_LONG
        ).show();
    }

    // =========================
    // 起動
    // =========================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );

        loadPuzzle();
        createScreen();

        startTime = System.currentTimeMillis();

        handler.removeCallbacks(timerRunnable);

        updateTimer();

        if (!gameOver) {
            handler.post(timerRunnable);
        }
    }

    // =========================
    // 問題を読み込む
    // =========================

    private void loadPuzzle() {

        if (difficulty == 0) {
            puzzle = easyPuzzle;
        } else if (difficulty == 1) {
            puzzle = normalPuzzle;
        } else {
            puzzle = hardPuzzle;
        }

        mistakes = 0;
        hintPenaltySeconds = 0;
        gameOver = false;

        if (gameOverOverlay != null) {
            gameOverOverlay.setVisibility(View.GONE);
        }

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {

                board[r][c] = puzzle[r][c];

                fixed[r][c] = puzzle[r][c] != 0;
            }
        }

        selectedRow = -1;
        selectedCol = -1;
    }

    // =========================
    // 画面を作る
    // =========================

    private void createScreen() {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(8), dp(6), dp(8), dp(6));
        root.setBackgroundColor(Color.rgb(18, 18, 22));

        scrollView.addView(root);

        // 上部情報欄
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        root.addView(top);

        // 難易度
        difficultyText = new TextView(this);
        difficultyText.setTextSize(14);
        difficultyText.setTextColor(Color.rgb(220, 70, 75));
        difficultyText.setTypeface(null, Typeface.BOLD);

        top.addView(
                difficultyText,
                new LinearLayout.LayoutParams(0, dp(40), 1f)
        );

        // ミス回数
        mistakeText = new TextView(this);
        mistakeText.setTextSize(14);
        mistakeText.setTextColor(Color.rgb(235, 225, 225));
        mistakeText.setGravity(Gravity.CENTER);

        top.addView(
                mistakeText,
                new LinearLayout.LayoutParams(0, dp(40), 1f)
        );

        // タイマー
        timerText = new TextView(this);
        timerText.setTextSize(16);
        timerText.setTextColor(Color.rgb(235, 225, 225));
        timerText.setGravity(Gravity.CENTER);

        top.addView(
                timerText,
                new LinearLayout.LayoutParams(0, dp(40), 1f)
        );

        updateInfo();

        // 状態表示
        statusText = new TextView(this);
        statusText.setText("マスを選んで数字を入力してください");
        statusText.setTextSize(13);
        statusText.setTextColor(Color.rgb(205, 190, 190));
        statusText.setGravity(Gravity.CENTER);

        root.addView(statusText);

        // =========================
        // 盤面
        // =========================

        grid = new GridLayout(this);
        grid.setRowCount(9);
        grid.setColumnCount(9);

        int screenWidth =
                getResources().getDisplayMetrics().widthPixels;

        int boardSize = screenWidth - dp(16);

        FrameLayout boardFrame = new FrameLayout(this);

        LinearLayout.LayoutParams boardFrameParams =
                new LinearLayout.LayoutParams(
                        boardSize,
                        boardSize
                );

        boardFrameParams.gravity = Gravity.CENTER;
        boardFrameParams.setMargins(0, dp(5), 0, dp(5));

        root.addView(boardFrame, boardFrameParams);

        boardFrame.addView(
                grid,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        // =========================
        // 3×3の太い境界線
        // =========================

        View blockLines = new View(this) {

            private final Paint linePaint =
                    new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                linePaint.setColor(Color.rgb(205, 85, 92));
                linePaint.setStyle(Paint.Style.FILL);

                float lineWidth = dp(3);

                float thirdWidth = getWidth() / 3f;
                float thirdHeight = getHeight() / 3f;

                for (int i = 1; i <= 2; i++) {

                    float x = thirdWidth * i;
                    float y = thirdHeight * i;

                    // 縦線
                    canvas.drawRect(
                            x - lineWidth / 2f,
                            0,
                            x + lineWidth / 2f,
                            getHeight(),
                            linePaint
                    );

                    // 横線
                    canvas.drawRect(
                            0,
                            y - lineWidth / 2f,
                            getWidth(),
                            y + lineWidth / 2f,
                            linePaint
                    );
                }
            }
        };

        blockLines.setClickable(false);
        blockLines.setFocusable(false);

        boardFrame.addView(
                blockLines,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        // =========================
        // 81マス作成
        // =========================

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {

                final int row = r;
                final int col = c;

                Button cell = new Button(this);

                cell.setTextSize(17);
                cell.setPadding(0, 0, 0, 0);

                cell.setMinWidth(0);
                cell.setMinimumWidth(0);

                cell.setMinHeight(0);
                cell.setMinimumHeight(0);

                GridLayout.LayoutParams params =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(r, 1, 1f),
                                GridLayout.spec(c, 1, 1f)
                        );

                params.width = 0;
                params.height = 0;

                params.setMargins(1, 1, 1, 1);

                cells[r][c] = cell;

                cell.setOnClickListener(v -> {

                    if (gameOver) {
                        return;
                    }

                    selectedRow = row;
                    selectedCol = col;

                    updateBoard();
                });

                grid.addView(cell, params);
            }
        }

        // =========================
        // 数字ボタン
        // =========================

        GridLayout numberGrid = new GridLayout(this);

        numberGrid.setRowCount(3);
        numberGrid.setColumnCount(3);

        LinearLayout.LayoutParams numberParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(150)
                );

        numberParams.setMargins(0, dp(4), 0, dp(4));

        root.addView(numberGrid, numberParams);

        for (int n = 1; n <= 9; n++) {

            final int number = n;

            Button button = new Button(this);

            button.setText(String.valueOf(n));
            button.setTextSize(23);

            button.setTextColor(Color.rgb(245, 230, 225));
            button.setBackgroundColor(Color.rgb(42, 39, 43));

            button.setPadding(0, 0, 0, 0);

            GridLayout.LayoutParams params =
                    new GridLayout.LayoutParams(
                            GridLayout.spec((n - 1) / 3, 1, 1f),
                            GridLayout.spec((n - 1) % 3, 1, 1f)
                    );

            params.width = 0;
            params.height = 0;

            params.setMargins(dp(3), dp(3), dp(3), dp(3));

            numberGrid.addView(button, params);

            button.setOnClickListener(v -> inputNumber(number));
        }

        // =========================
        // 操作ボタン
        // =========================

        LinearLayout controls = new LinearLayout(this);

        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);

        root.addView(controls);

        Button eraseButton = createControlButton("消去");
        Button hintButton = createControlButton("ヒント");
        Button checkButton = createControlButton("判定");
        Button resetButton = createControlButton("リセット");

        controls.addView(
                eraseButton,
                new LinearLayout.LayoutParams(0, dp(48), 1f)
        );

        controls.addView(
                hintButton,
                new LinearLayout.LayoutParams(0, dp(48), 1f)
        );

        controls.addView(
                checkButton,
                new LinearLayout.LayoutParams(0, dp(48), 1f)
        );

        controls.addView(
                resetButton,
                new LinearLayout.LayoutParams(0, dp(48), 1f)
        );

        eraseButton.setOnClickListener(v -> inputNumber(0));

        hintButton.setOnClickListener(v -> showHint());

        checkButton.setOnClickListener(v -> checkAnswer());

        resetButton.setOnClickListener(v -> resetGame());

        // =========================
        // 難易度変更ボタン
        // =========================

        Button difficultyButton =
                createControlButton("難易度変更");

        root.addView(difficultyButton);

        difficultyButton.setOnClickListener(v -> {

            difficulty++;

            if (difficulty > 2) {
                difficulty = 0;
            }

            loadPuzzle();

            updateInfo();
            updateBoard();

            statusText.setText("新しい問題を開始しました");

            startTime = System.currentTimeMillis();

            handler.removeCallbacks(timerRunnable);

            updateTimer();

            if (!gameOver) {
                handler.post(timerRunnable);
            }
        });

        // =========================
        // Game Over画面
        // =========================

        FrameLayout screenRoot = new FrameLayout(this);

        screenRoot.addView(
                scrollView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        gameOverOverlay = new FrameLayout(this);

        gameOverOverlay.setBackgroundColor(
                Color.argb(245, 5, 5, 8)
        );

        gameOverOverlay.setVisibility(View.GONE);

        LinearLayout gameOverContent = new LinearLayout(this);

        gameOverContent.setOrientation(LinearLayout.VERTICAL);
        gameOverContent.setGravity(Gravity.CENTER);

        gameOverContent.setPadding(
                dp(24),
                dp(24),
                dp(24),
                dp(24)
        );

        // GAME OVERタイトル
        TextView gameOverTitle = new TextView(this);

        gameOverTitle.setText("GAME OVER");
        gameOverTitle.setTextColor(Color.rgb(230, 45, 55));
        gameOverTitle.setTextSize(38);
        gameOverTitle.setTypeface(null, Typeface.BOLD);
        gameOverTitle.setGravity(Gravity.CENTER);

        gameOverContent.addView(
                gameOverTitle,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        // Game Overの理由
        gameOverDetailText = new TextView(this);

        gameOverDetailText.setTextColor(
                Color.rgb(235, 220, 220)
        );

        gameOverDetailText.setTextSize(17);
        gameOverDetailText.setGravity(Gravity.CENTER);

        gameOverDetailText.setPadding(
                0,
                dp(18),
                0,
                dp(28)
        );

        gameOverContent.addView(
                gameOverDetailText,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        // 再挑戦ボタン
        Button retryButton =
                createControlButton("リセットして再挑戦");

        retryButton.setTextSize(16);
        retryButton.setTextColor(Color.WHITE);

        retryButton.setBackgroundColor(
                Color.rgb(130, 25, 35)
        );

        gameOverContent.addView(
                retryButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                )
        );

        retryButton.setOnClickListener(v -> resetGame());

        gameOverOverlay.addView(
                gameOverContent,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                )
        );

        screenRoot.addView(
                gameOverOverlay,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(screenRoot);

        updateBoard();
    }

    // =========================
    // 操作ボタン作成
    // =========================

    private Button createControlButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(13);

        button.setTextColor(
                Color.rgb(220, 75, 80)
        );

        button.setBackgroundColor(
                Color.rgb(38, 35, 39)
        );

        return button;
    }

    // =========================
    // 数字入力
    // =========================

    private void inputNumber(int number) {

        if (gameOver) {
            return;
        }

        if (selectedRow == -1 || selectedCol == -1) {

            Toast.makeText(
                    this,
                    "マスを選択してください",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (fixed[selectedRow][selectedCol]) {

            Toast.makeText(
                    this,
                    "最初からある数字は変更できません",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (number != 0 &&
                number != solution[selectedRow][selectedCol]) {

            mistakes++;

            updateInfo();

            // ミス3回でGame Over
            if (mistakes >= 3) {

                updateBoard();

                showGameOver(
                        "ミスが3回になりました。"
                );

                return;
            }

            statusText.setText("数字が違います");

        } else {

            statusText.setText("入力しました");
        }

        board[selectedRow][selectedCol] = number;

        updateBoard();
    }

    // =========================
    // ヒント
    // =========================

    private void showHint() {

        if (gameOver) {
            return;
        }

        if (selectedRow == -1 || selectedCol == -1) {

            Toast.makeText(
                    this,
                    "マスを選択してください",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (fixed[selectedRow][selectedCol]) {

            Toast.makeText(
                    this,
                    "別のマスを選択してください",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // 正解を入力
        board[selectedRow][selectedCol] =
                solution[selectedRow][selectedCol];

        // ヒント1回につき60秒減少
        hintPenaltySeconds += 60;

        statusText.setText(
                "ヒントを使用しました。残り時間を1分減らしました"
        );

        // すぐに残り時間を更新
        updateTimer();

        if (gameOver) {
            return;
        }

        updateBoard();
    }

    // =========================
    // 盤面更新
    // =========================

    private void updateBoard() {

        int selectedNumber = 0;

        if (selectedRow != -1 && selectedCol != -1) {

            selectedNumber =
                    board[selectedRow][selectedCol];
        }

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {

                Button cell = cells[r][c];

                if (board[r][c] == 0) {
                    cell.setText("");
                } else {
                    cell.setText(
                            String.valueOf(board[r][c])
                    );
                }

                // 最初からある数字
                if (fixed[r][c]) {

                    cell.setTextColor(
                            Color.rgb(245, 238, 230)
                    );

                    cell.setTypeface(
                            null,
                            Typeface.BOLD
                    );

                } else {

                    cell.setTextColor(
                            Color.rgb(255, 105, 110)
                    );

                    cell.setTypeface(
                            null,
                            Typeface.NORMAL
                    );
                }

                int bg = Color.rgb(34, 34, 39);

                // 選択行・列
                if (r == selectedRow || c == selectedCol) {
                    bg = Color.rgb(54, 42, 47);
                }

                // 同じ数字
                if (selectedNumber != 0 &&
                        board[r][c] == selectedNumber) {

                    bg = Color.rgb(75, 42, 48);
                }

                // 選択マス
                if (r == selectedRow &&
                        c == selectedCol) {

                    bg = Color.rgb(125, 38, 46);
                }

                GradientDrawable background =
                        new GradientDrawable();

                background.setColor(bg);

                // 通常のマスの線は細くする
                background.setStroke(
                        dp(1),
                        Color.rgb(100, 60, 66)
                );

                cell.setBackground(background);
            }
        }
    }

    // =========================
    // 答え合わせ
    // =========================

    private void checkAnswer() {

        if (gameOver) {
            return;
        }

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {

                if (board[r][c] == 0) {

                    statusText.setText(
                            "空欄があります"
                    );

                    return;
                }

                if (board[r][c] != solution[r][c]) {

                    statusText.setText(
                            "間違いがあります"
                    );

                    return;
                }
            }
        }

        gameOver = true;

        handler.removeCallbacks(timerRunnable);

        statusText.setText(
                "クリア！おめでとうございます！"
        );

        Toast.makeText(
                this,
                "ナンプレクリア！",
                Toast.LENGTH_LONG
        ).show();
    }

    // =========================
    // リセット
    // =========================

    private void resetGame() {

        loadPuzzle();

        updateBoard();
        updateInfo();

        statusText.setText(
                "最初からやり直します"
        );

        startTime = System.currentTimeMillis();

        handler.removeCallbacks(timerRunnable);

        updateTimer();

        if (!gameOver) {
            handler.post(timerRunnable);
        }
    }

    // =========================
    // 情報表示
    // =========================

    private void updateInfo() {

        if (difficultyText != null) {

            String[] names = {
                    "初級",
                    "中級",
                    "上級"
            };

            difficultyText.setText(
                    "難易度：" + names[difficulty]
            );
        }

        if (mistakeText != null) {

            mistakeText.setText(
                    "ミス：" + mistakes + "/3"
            );
        }
    }

    // =========================
    // dp変換
    // =========================

    private int dp(int value) {

        return (int) (
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    // =========================
    // 終了時タイマー停止
    // =========================

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(timerRunnable);

        super.onDestroy();
    }
}