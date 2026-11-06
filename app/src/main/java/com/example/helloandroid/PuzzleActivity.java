package com.example.helloandroid;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PuzzleActivity extends AppCompatActivity {

    private final TextView[] numTextViews = new TextView[4];
    private EditText editExpression;
    private TextView txtPuzzleResult;
    private TextView txtTimer;

    private LinearLayout layoutGameContent, layoutResultScreen;
    private TextView txtResultTitle, txtResultSubMessage;

    private int[] currentPuzzleNumbers = new int[4];
    private final Random puzzleRandom = new Random();
    private CountDownTimer countDownTimer;
    private boolean isGameActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        numTextViews[0] = findViewById(R.id.num1);
        numTextViews[1] = findViewById(R.id.num2);
        numTextViews[2] = findViewById(R.id.num3);
        numTextViews[3] = findViewById(R.id.num4);
        editExpression = findViewById(R.id.editExpression);
        txtPuzzleResult = findViewById(R.id.txtPuzzleResult);
        txtTimer = findViewById(R.id.txtTimer);

        Button btnCheck = findViewById(R.id.btnCheck);
        Button btnNew = findViewById(R.id.btnNew);
        Button btnBack = findViewById(R.id.btnBack);

        layoutGameContent = findViewById(R.id.layoutGameContent);
        layoutResultScreen = findViewById(R.id.layoutResultScreen);
        txtResultTitle = findViewById(R.id.txtResultTitle);
        txtResultSubMessage = findViewById(R.id.txtResultSubMessage);
        Button btnRetry = findViewById(R.id.btnRetry);

        startNewGame();

        btnNew.setOnClickListener(v -> startNewGame());
        btnCheck.setOnClickListener(v -> checkUserAnswer());
        btnBack.setOnClickListener(v -> finish());
        btnRetry.setOnClickListener(v -> startNewGame());
    }

    private void startNewGame() {
        isGameActive = true;

        layoutGameContent.setVisibility(View.VISIBLE);
        layoutResultScreen.setVisibility(View.GONE);

        generatePuzzleProblem();
        startTimer();
    }

    // --- タイマーの開始（2分 = 120,000ミリ秒） ---
    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(120000, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                txtTimer.setText(String.format("残り時間: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                if (!isGameActive) return;
                isGameActive = false;
                showGameOverScreen();
            }
        }.start();
    }

    @SuppressLint("SetTextI18n")
    private void showGameOverScreen() {
        if (countDownTimer != null) countDownTimer.cancel();

        layoutGameContent.setVisibility(View.GONE);
        layoutResultScreen.setVisibility(View.VISIBLE);

        txtResultTitle.setText("GAME OVER");
        txtResultTitle.setTextColor(Color.parseColor("#FF3333"));
        txtResultSubMessage.setText("あなたは魔方陣に閉じ込められた……\n\nタイムアップです。\nもう数式は解けない……");
    }

    @SuppressLint("SetTextI18n")
    private void showGameClearScreen() {
        if (countDownTimer != null) countDownTimer.cancel();

        layoutGameContent.setVisibility(View.GONE);
        layoutResultScreen.setVisibility(View.VISIBLE);

        txtResultTitle.setText("YOU ESCAPED");
        txtResultTitle.setTextColor(Color.parseColor("#00FF66"));
        txtResultSubMessage.setText("儀式は終了した……\n\n正しい数式を入力した。\nあなたは脱出に成功した。");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void generatePuzzleProblem() {
        int[] nums = new int[4];
        String solution = null;

        while (solution == null) {
            for (int i = 0; i < 4; i++) {
                nums[i] = puzzleRandom.nextInt(9) + 1;
            }
            solution = solve24ForGeneration(nums);
        }

        currentPuzzleNumbers = nums;
        for (int i = 0; i < 4; i++) {
            numTextViews[i].setText(String.valueOf(currentPuzzleNumbers[i]));
        }
        editExpression.setText("");
        txtPuzzleResult.setText("数式を入力して答え合わせを押してください");
    }

    @SuppressLint("SetTextI18n")
    private void checkUserAnswer() {
        if (!isGameActive) return;

        String expr = editExpression.getText().toString().trim();
        if (expr.isEmpty()) {
            txtPuzzleResult.setText("数式を入力してください。");
            return;
        }

        if (!isValidNumberUsage(expr, currentPuzzleNumbers)) {
            txtPuzzleResult.setText("不正解: 指定された4つの数字を正しく使っていません。");
            return;
        }

        try {
            double result = eval(expr);
            if (Math.abs(result - 24.0) < 1e-6) {
                isGameActive = false;
                showGameClearScreen();
            } else {
                txtPuzzleResult.setText("不正解: 計算結果が " + result + " です（24ではありません）。");
            }
        } catch (Exception e) {
            txtPuzzleResult.setText("エラー: 数式の形式が正しくありません。");
        }
    }

    private boolean isValidNumberUsage(String expr, int[] targetNums) {
        List<Integer> userNums = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            } else {
                if (sb.length() > 0) {
                    userNums.add(Integer.parseInt(sb.toString()));
                    sb.setLength(0);
                }
            }
        }
        if (sb.length() > 0) {
            userNums.add(Integer.parseInt(sb.toString()));
        }

        if (userNums.size() != 4) return false;

        int[] uArr = new int[4];
        for (int i = 0; i < 4; i++) uArr[i] = userNums.get(i);
        int[] tArr = targetNums.clone();

        Arrays.sort(uArr);
        Arrays.sort(tArr);

        return Arrays.equals(uArr, tArr);
    }

    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void next() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') next();
                if (ch == charToEat) {
                    next();
                    return true;
                }
                return false;
            }

            double parse() {
                next();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') next();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                return x;
            }
        }.parse();
    }

    private static class ExpressionItem {
        String str;
        double val;
        ExpressionItem(String s, double v) {
            str = s;
            val = v;
        }
    }

    private String solve24ForGeneration(int[] nums) {
        List<ExpressionItem> list = new ArrayList<>();
        for (int n : nums) {
            list.add(new ExpressionItem(String.valueOf(n), (double) n));
        }
        Set<String> found = new HashSet<>();
        return searchPuzzleForGeneration(list, found);
    }

    private String searchPuzzleForGeneration(List<ExpressionItem> list, Set<String> visited) {
        if (list.size() == 1) {
            if (Math.abs(list.get(0).val - 24.0) < 1e-6) {
                return list.get(0).str;
            }
            return null;
        }

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (i == j) continue;

                List<ExpressionItem> nextList = new ArrayList<>();
                for (int k = 0; k < list.size(); k++) {
                    if (k != i && k != j) {
                        nextList.add(list.get(k));
                    }
                }

                ExpressionItem e1 = list.get(i);
                ExpressionItem e2 = list.get(j);

                ExpressionItem[] ops = {
                        new ExpressionItem("(" + e1.str + " + " + e2.str + ")", e1.val + e2.val),
                        new ExpressionItem("(" + e1.str + " - " + e2.str + ")", e1.val - e2.val),
                        new ExpressionItem("(" + e1.str + " * " + e2.str + ")", e1.val * e2.val),
                        e2.val != 0 ? new ExpressionItem("(" + e1.str + " / " + e2.str + ")", e1.val / e2.val) : null
                };

                for (ExpressionItem op : ops) {
                    if (op == null) continue;
                    List<ExpressionItem> subList = new ArrayList<>(nextList);
                    subList.add(op);

                    String res = searchPuzzleForGeneration(subList, visited);
                    if (res != null) return res;
                }
            }
        }
        return null;
    }
}