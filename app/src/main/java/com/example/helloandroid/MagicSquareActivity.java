package com.example.helloandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MagicSquareActivity extends AppCompatActivity {

    // ミスの上限
    private static final int MAX_MISTAKES = 5;

    // 制限時間5分
    private static final int GAME_TIME = 300;

    // 最初から表示される数字
    private final int[][] initial = {
            {0, 1, 6},
            {0, 0, 0},
            {0, 0, 0}
    };

    // 現在の魔方陣
    private final int[][] board = new int[3][3];

    // マスの表示
    private final TextView[][] cellViews = new TextView[3][3];

    // 合計の表示
    private final TextView[] rowSums = new TextView[3];
    private final TextView[] colSums = new TextView[3];

    // 画面の部品
    private ScrollView gameScreen;
    private TextView timerView;
    private TextView mistakeView;
    private TextView messageView;
    private TextView diagonalView;

    private Button undoButton;
    private Button eraseButton;

    // 入力履歴
    private final ArrayList<Move> history = new ArrayList<>();

    // 数字ボタン
    private final ArrayList<Button> numberButtons = new ArrayList<>();

    // 選択中のマス
    private int selectedRow = -1;
    private int selectedCol = -1;

    // ミス回数
    private int mistakes = 0;

    // 残り時間
    private int remainingSeconds = GAME_TIME;

    // ゲーム終了判定
    private boolean gameEnded = false;

    private final Handler handler = new Handler();

    // タイマー
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if (gameEnded) {
                return;
            }

            remainingSeconds--;

            updateTimer();

            if (remainingSeconds <= 0) {

                endGame(false);

            } else {

                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createScreen();
        resetGame();
    }

    // ==================================
    // ゲーム画面を作る
    // ==================================

    private void createScreen() {

        gameScreen = new ScrollView(this);
        gameScreen.setFillViewport(true);
        gameScreen.setBackgroundColor(
                Color.rgb(18, 28, 48));

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL);

        root.setGravity(
                Gravity.CENTER_HORIZONTAL);

        root.setPadding(
                dp(14), dp(18), dp(14), dp(18));

        gameScreen.addView(root);

        // タイトル
        TextView title = createText(
                "MIDNIGHT MAGIC",
                14,
                Color.rgb(18, 28, 48)
        );

        title.setLetterSpacing(0.15f);

        root.addView(title);

        // サブタイトル
        TextView subtitle = createText(
                "深夜の魔方陣",
                29,
                Color.WHITE
        );

        subtitle.setTypeface(
                Typeface.DEFAULT_BOLD);

        subtitle.setPadding(
                0, dp(5), 0, dp(12));

        root.addView(subtitle);

        // ==================================
        // パズルのルール表示
        // ==================================

        // パズルのルール表示
        TextView rule = createText(
                "たて・よこ・ななめの数字を\n" +
                        "全部足して15にしろ",
                19,
                Color.WHITE
        );

        rule.setTypeface(Typeface.DEFAULT_BOLD);

// 他の合計表示と同じグレー
        gameScreen.setBackgroundColor(
                Color.rgb(18, 28, 48));

        rule.setPadding(
                dp(10), dp(12), dp(10), dp(12));

        LinearLayout.LayoutParams ruleParams =
                new LinearLayout.LayoutParams(
                        -1, -2);

        ruleParams.setMargins(
                0, dp(5), 0, dp(15));

        root.addView(rule, ruleParams);

        // ==================================
        // タイマーとミス回数
        // ==================================

        LinearLayout status = new LinearLayout(this);

        status.setOrientation(
                LinearLayout.HORIZONTAL);

        timerView = createText(
                "残り時間 05:00",
                17,
                0xFFFFA0A0
        );

        mistakeView = createText(
                "ミス 0 / 5",
                17,
                Color.WHITE
        );

        status.addView(
                timerView,
                new LinearLayout.LayoutParams(
                        0, dp(42), 1));

        status.addView(
                mistakeView,
                new LinearLayout.LayoutParams(
                        0, dp(42), 1));

        root.addView(status);

        // ==================================
        // 魔方陣のマス
        // ==================================

        LinearLayout boardLayout = new LinearLayout(this);

        boardLayout.setOrientation(
                LinearLayout.VERTICAL);

        for (int i = 0; i < 3; i++) {

            final int row = i;

            LinearLayout rowLayout = new LinearLayout(this);

            rowLayout.setOrientation(
                    LinearLayout.HORIZONTAL);

            for (int j = 0; j < 3; j++) {

                final int col = j;

                TextView cell = createText(
                        "", 31, Color.BLACK);

                cell.setTypeface(
                        Typeface.DEFAULT_BOLD);

                cellViews[i][j] = cell;

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                0, dp(82), 1);

                params.setMargins(
                        dp(2), dp(2), dp(2), dp(2));

                rowLayout.addView(cell, params);

                cell.setOnClickListener(v ->
                        selectCell(row, col));
            }

            // 右側の横の合計
            TextView sum = createText(
                    "0", 18, Color.WHITE);

            sum.setBackground(
                    makeBackground(0xFF293549));

            rowSums[i] = sum;

            LinearLayout.LayoutParams sumParams =
                    new LinearLayout.LayoutParams(
                            dp(43), dp(82));

            sumParams.setMargins(
                    dp(2), dp(2), 0, dp(2));

            rowLayout.addView(sum, sumParams);

            boardLayout.addView(rowLayout);
        }

        root.addView(boardLayout);

        // ==================================
        // 下側の縦の合計
        // ==================================

        LinearLayout bottomSums = new LinearLayout(this);

        bottomSums.setOrientation(
                LinearLayout.HORIZONTAL);

        for (int j = 0; j < 3; j++) {

            TextView sum = createText(
                    "0", 16, Color.WHITE);

            sum.setBackground(
                    makeBackground(0xFF293549));

            colSums[j] = sum;

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            0, dp(32), 1);

            params.setMargins(
                    dp(2), 0, dp(2), 0);

            bottomSums.addView(sum, params);
        }

        // 斜めの合計
        diagonalView = createText(
                "↘ 0\n↙ 0",
                12,
                Color.WHITE
        );

        diagonalView.setBackground(
                makeBackground(0xFF293549));

        bottomSums.addView(
                diagonalView,
                new LinearLayout.LayoutParams(
                        dp(43), dp(32)));

        root.addView(bottomSums);

        // ==================================
        // 数字ボタン
        // ==================================

        LinearLayout numberPad = new LinearLayout(this);

        numberPad.setOrientation(
                LinearLayout.HORIZONTAL);

        numberPad.setPadding(
                0, dp(12), 0, dp(8));

        for (int i = 1; i <= 9; i++) {

            final int number = i;

            Button button = createButton(
                    String.valueOf(i),
                    0xFFF1F2F5
            );

            button.setTextColor(0xFF172033);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            0, dp(45), 1);

            params.setMargins(
                    dp(2), 0, dp(2), 0);

            numberPad.addView(button, params);

            numberButtons.add(button);

            button.setOnClickListener(v ->
                    enterNumber(number));
        }

        root.addView(numberPad);

        // ==================================
        // 戻す・消去ボタン
        // ==================================

        LinearLayout actionLayout = new LinearLayout(this);

        actionLayout.setOrientation(
                LinearLayout.HORIZONTAL);

        undoButton = createButton(
                "戻す",
                0xFF777E89
        );

        eraseButton = createButton(
                "消去",
                0xFFEDEFF2
        );

        undoButton.setOnClickListener(v -> undo());

        eraseButton.setOnClickListener(v -> erase());

        actionLayout.addView(
                undoButton,
                new LinearLayout.LayoutParams(
                        0, dp(46), 1));

        actionLayout.addView(
                eraseButton,
                new LinearLayout.LayoutParams(
                        0, dp(46), 1));

        root.addView(actionLayout);

        // 最初からやり直すボタン
        Button restart = createButton(
                "最初からやり直す",
                0xFF38445A
        );

        restart.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams restartParams =
                new LinearLayout.LayoutParams(
                        -1, dp(43));

        restartParams.setMargins(
                0, dp(8), 0, dp(8));

        root.addView(restart, restartParams);

        restart.setOnClickListener(v -> resetGame());

        // メッセージ
        messageView = createText(
                "空いているマスを選んで数字を入力しよう。",
                14,
                0xFFFFA0A0
        );

        messageView.setPadding(
                dp(4), dp(8), dp(4), dp(8));

        root.addView(messageView);
    }

    // ==================================
    // マスを選択する
    // ==================================

    private void selectCell(int row, int col) {

        if (gameEnded) {
            return;
        }

        // 最初からある数字は変更できない
        if (initial[row][col] != 0) {

            showMessage(
                    "その数字は動かせないよ……",
                    0xFFFF7777
            );

            return;
        }

        selectedRow = row;
        selectedCol = col;

        showMessage(
                "マスを選択したよ。数字を選んでね。",
                Color.WHITE
        );

        updateBoard();
    }

    // ==================================
    // 数字を入力する
    // ==================================

    private void enterNumber(int number) {

        if (gameEnded) {
            return;
        }

        if (selectedRow == -1 ||
                selectedCol == -1) {

            showMessage(
                    "先にマスを選んでね……",
                    0xFFFF7777
            );

            return;
        }

        int r = selectedRow;
        int c = selectedCol;

        int oldValue = board[r][c];

        if (oldValue == number) {
            return;
        }

        // 同じ数字がないか確認
        if (isDuplicate(number, r, c)) {

            registerMistake(
                    "同じ数字は2回使えない……"
            );

            return;
        }

        // 数字を仮入力
        board[r][c] = number;

        // 合計が間違っていないか確認
        if (hasInvalidLine()) {

            board[r][c] = oldValue;

            updateBoard();

            registerMistake(
                    "線の合計が15にならないよ……"
            );

            return;
        }

        // 入力履歴を保存
        history.add(
                new Move(r, c, oldValue));

        selectedRow = -1;
        selectedCol = -1;

        updateBoard();

        showMessage(
                "数字を入力したよ……",
                Color.WHITE
        );

        // 完成したか確認
        if (isSolved()) {

            endGame(true);
        }
    }

    // ==================================
    // 数字の重複確認
    // ==================================

    private boolean isDuplicate(
            int number, int row, int col) {

        for (int i = 0; i < 3; i++) {

            for (int j = 0; j < 3; j++) {

                if (i == row && j == col) {
                    continue;
                }

                if (board[i][j] == number) {
                    return true;
                }
            }
        }

        return false;
    }

    // ==================================
    // 横・縦・斜めの合計確認
    // ==================================

    private boolean hasInvalidLine() {

        // 横の合計
        for (int i = 0; i < 3; i++) {

            int sum = 0;
            int count = 0;

            for (int j = 0; j < 3; j++) {

                if (board[i][j] != 0) {

                    sum += board[i][j];
                    count++;
                }
            }

            if (sum > 15 ||
                    (count == 3 && sum != 15)) {

                return true;
            }
        }

        // 縦の合計
        for (int j = 0; j < 3; j++) {

            int sum = 0;
            int count = 0;

            for (int i = 0; i < 3; i++) {

                if (board[i][j] != 0) {

                    sum += board[i][j];
                    count++;
                }
            }

            if (sum > 15 ||
                    (count == 3 && sum != 15)) {

                return true;
            }
        }

        // 左上から右下
        int diagonal1 = 0;
        int count1 = 0;

        for (int i = 0; i < 3; i++) {

            if (board[i][i] != 0) {

                diagonal1 += board[i][i];
                count1++;
            }
        }

        if (diagonal1 > 15 ||
                (count1 == 3 && diagonal1 != 15)) {

            return true;
        }

        // 右上から左下
        int diagonal2 = 0;
        int count2 = 0;

        for (int i = 0; i < 3; i++) {

            if (board[i][2 - i] != 0) {

                diagonal2 += board[i][2 - i];
                count2++;
            }
        }

        return diagonal2 > 15 ||
                (count2 == 3 && diagonal2 != 15);
    }

    // ==================================
    // 正解判定
    // ==================================

    private boolean isSolved() {

        for (int i = 0; i < 3; i++) {

            for (int j = 0; j < 3; j++) {

                if (board[i][j] == 0) {
                    return false;
                }
            }
        }

        return !hasInvalidLine();
    }

    // ==================================
    // ミスを登録する
    // ==================================

    private void registerMistake(String message) {

        mistakes++;

        updateStatus();

        showMessage(
                message,
                0xFFFF5555
        );

        Toast.makeText(
                this,
                "ミス " + mistakes + " / " + MAX_MISTAKES,
                Toast.LENGTH_SHORT
        ).show();

        // 5回ミスでゲームオーバー
        if (mistakes >= MAX_MISTAKES) {

            endGame(false);
        }
    }

    // ==================================
    // 1つ前に戻す
    // ==================================

    private void undo() {

        if (gameEnded) {
            return;
        }

        if (history.isEmpty()) {

            showMessage(
                    "戻せる入力がないよ。",
                    Color.WHITE
            );

            return;
        }

        Move move = history.remove(
                history.size() - 1);

        board[move.row][move.col] =
                move.oldValue;

        selectedRow = move.row;
        selectedCol = move.col;

        updateBoard();

        showMessage(
                "1つ前に戻したよ。",
                Color.WHITE
        );
    }

    // ==================================
    // 選択したマスを消去する
    // ==================================

    private void erase() {

        if (gameEnded) {
            return;
        }

        if (selectedRow == -1 ||
                selectedCol == -1) {

            showMessage(
                    "消すマスを選んでね。",
                    Color.WHITE
            );

            return;
        }

        int r = selectedRow;
        int c = selectedCol;

        if (board[r][c] == 0) {
            return;
        }

        history.add(
                new Move(r, c, board[r][c]));

        board[r][c] = 0;

        updateBoard();

        showMessage(
                "数字を消したよ。",
                Color.WHITE
        );
    }

    // ==================================
    // 画面を更新する
    // ==================================

    private void updateBoard() {

        // マスの数字
        for (int i = 0; i < 3; i++) {

            for (int j = 0; j < 3; j++) {

                TextView cell = cellViews[i][j];

                if (board[i][j] == 0) {

                    cell.setText("");

                } else {

                    cell.setText(
                            String.valueOf(board[i][j]));
                }

                // 最初からある数字
                if (initial[i][j] != 0) {

                    cell.setBackground(
                            makeBackground(0xFFDCE3ED));

                    // 選択しているマス
                } else if (i == selectedRow &&
                        j == selectedCol) {

                    cell.setBackground(
                            makeBackground(0xFFFFC5CD));

                    // 通常のマス
                } else {

                    cell.setBackground(
                            makeBackground(Color.WHITE));
                }

                cell.setTextColor(0xFF172033);
            }
        }

        // 横の合計
        for (int i = 0; i < 3; i++) {

            int sum = 0;

            for (int j = 0; j < 3; j++) {

                sum += board[i][j];
            }

            rowSums[i].setText(
                    String.valueOf(sum));
        }

        // 縦の合計
        for (int j = 0; j < 3; j++) {

            int sum = 0;

            for (int i = 0; i < 3; i++) {

                sum += board[i][j];
            }

            colSums[j].setText(
                    String.valueOf(sum));
        }

        // 斜めの合計
        int d1 = board[0][0]
                + board[1][1]
                + board[2][2];

        int d2 = board[0][2]
                + board[1][1]
                + board[2][0];

        diagonalView.setText(
                "↘ " + d1 + "\n↙ " + d2);

        updateStatus();
    }

    // ==================================
    // ミスと時間を表示する
    // ==================================

    private void updateStatus() {

        mistakeView.setText(
                "ミス " + mistakes + " / " + MAX_MISTAKES);

        updateTimer();
    }

    private void updateTimer() {

        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;

        timerView.setText(
                String.format(
                        Locale.JAPAN,
                        "残り時間 %02d:%02d",
                        minutes,
                        seconds
                )
        );

        if (remainingSeconds <= 60) {

            timerView.setTextColor(
                    0xFFFF3333);

        } else {

            timerView.setTextColor(
                    0xFFFFA0A0);
        }
    }

    // ==================================
    // ゲーム終了画面
    // ==================================

    private void endGame(boolean clear) {

        if (gameEnded) {
            return;
        }

        gameEnded = true;

        // タイマー停止
        handler.removeCallbacks(timerRunnable);

        // 全画面レイアウト
        LinearLayout over = new LinearLayout(this);

        over.setOrientation(
                LinearLayout.VERTICAL);

        over.setGravity(
                Gravity.CENTER);

        over.setPadding(
                dp(25), dp(25), dp(25), dp(25));

        over.setBackgroundColor(
                Color.rgb(5, 5, 12));

        over.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        // タイトル
        TextView title = createText(
                clear ? "YOU ESCAPED" : "GAME OVER",
                38,
                clear ? 0xFF64FF9A : 0xFFFF2337
        );

        title.setTypeface(
                Typeface.DEFAULT_BOLD);

        over.addView(title);

        // ホラー演出
        TextView horror = createText(
                clear
                        ? "\n儀式は終了した……"
                        : "\nあなたは魔方陣に閉じ込められた……",
                20,
                0xFFDDDDDD
        );

        horror.setPadding(
                0, dp(30), 0, dp(20));

        over.addView(horror);

        // 終了理由
        TextView reason = createText(
                "",
                18,
                Color.WHITE
        );

        if (clear) {

            reason.setText(
                    "すべての数字が正しい。\nあなたは脱出に成功した。");

            reason.setTextColor(
                    0xFF7CFFAE);

        } else if (mistakes >= MAX_MISTAKES) {

            reason.setText(
                    "5回間違えてしまった。\nもう魔方陣は解けない……");

            reason.setTextColor(
                    0xFFFF5555);

        } else {

            reason.setText(
                    "制限時間5分が終了した。\n時間切れ……");

            reason.setTextColor(
                    0xFFFF5555);
        }

        reason.setPadding(
                0, dp(10), 0, dp(35));

        over.addView(reason);

        // 再挑戦ボタン
        Button retry = createButton(
                "もう一度挑戦する",
                0xFF8B1828
        );

        retry.setTextSize(18);
        retry.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1, dp(60));

        over.addView(retry, params);

        retry.setOnClickListener(v ->
                resetGame());

        // ゲーム画面から終了画面に切り替える
        setContentView(over);
    }

    // ==================================
    // ゲームをリセットする
    // ==================================

    private void resetGame() {

        handler.removeCallbacks(timerRunnable);

        // ゲームオーバー画面から戻る場合だけ画面を切り替える
        if (gameScreen.getParent() == null) {
            setContentView(gameScreen);
        }

        mistakes = 0;
        remainingSeconds = GAME_TIME;

        selectedRow = -1;
        selectedCol = -1;

        gameEnded = false;

        history.clear();

        // 数字を初期状態に戻す
        for (int i = 0; i < 3; i++) {

            for (int j = 0; j < 3; j++) {

                board[i][j] = initial[i][j];
            }
        }

        // ボタンを有効にする
        for (Button button : numberButtons) {

            button.setEnabled(true);
        }

        undoButton.setEnabled(true);
        eraseButton.setEnabled(true);

        updateBoard();

        showMessage(
                "空いているマスを選んで数字を入力しよう。",
                0xFFFFA0A0
        );

        // 5分のカウント開始
        handler.postDelayed(
                timerRunnable, 1000);
    }

    // ==================================
    // テキストを作る
    // ==================================

    private TextView createText(
            String text, int size, int color) {

        TextView view = new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);

        view.setGravity(
                Gravity.CENTER);

        return view;
    }

    // ==================================
    // ボタンを作る
    // ==================================

    private Button createButton(
            String text, int color) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);

        button.setTextColor(0xFF172033);

        button.setBackground(
                makeBackground(color));

        button.setMinHeight(0);
        button.setMinWidth(0);

        return button;
    }

    // ==================================
    // 背景を作る
    // ==================================

    private GradientDrawable makeBackground(
            int color) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(6));
        drawable.setStroke(dp(1), color);

        return drawable;
    }

    // ==================================
    // メッセージを表示
    // ==================================

    private void showMessage(
            String message, int color) {

        messageView.setText(message);
        messageView.setTextColor(color);
    }

    // ==================================
    // dp変換
    // ==================================

    private int dp(float value) {

        return (int) (
                value *
                        getResources().getDisplayMetrics().density
                        + 0.5f);
    }

    // ==================================
    // 入力履歴を保存するクラス
    // ==================================

    private static class Move {

        int row;
        int col;
        int oldValue;

        Move(int row, int col, int oldValue) {

            this.row = row;
            this.col = col;
            this.oldValue = oldValue;
        }
    }

    // ==================================
    // アプリ終了時
    // ==================================

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(timerRunnable);

        super.onDestroy();
    }
}
