
package com.example.helloandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.Choreographer;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {

    public interface OnPuzzleRequestListener {
        void onPuzzleRequest();
    }

    private OnPuzzleRequestListener puzzleRequestListener;

    public void setOnPuzzleRequestListener(OnPuzzleRequestListener listener) {
        puzzleRequestListener = listener;
    }

    // ==============================
    // プレイヤー設定
    // ==============================

    private final Movement movement = new Movement();
    private final Bitmap playerBitmap;
    private final RectF playerRect = new RectF();

    private float playerX = 750f;
    private float playerY = 2650f;

    private final float[] movementAmount = new float[2];
    private final float playerSize = 80f;

    private boolean up, down, left, right;
    private boolean touchUp, touchDown, touchLeft, touchRight;

    private boolean interactRequested = false;
    private float puzzleStartX;
    private float puzzleStartY;
    // ==============================
    // 敵設定
    // ==============================

    // 敵の画像を指定する場所
    private final Bitmap enemyBitmap;

    private final EnemyManager enemyManager;

    private static final float ENEMY_SIZE = 80f;
    private static final float ENEMY_SPEED = 100f;
    private static final float ENEMY_MIN_DISTANCE = 320f;

    // 部屋番号（部屋が変わったら変更する）
    private int currentRoomId = 0;

    // パズル操作中かどうか
    private boolean puzzleActive = false;

    // ==============================
    // ゲームループ
    // ==============================

    private boolean running = false;
    private long lastFrameTime = 0L;

    private final Choreographer.FrameCallback frameCallback =
            new Choreographer.FrameCallback() {

                @Override
                public void doFrame(long frameTimeNanos) {

                    if (!running) {
                        return;
                    }

                    float deltaTime = lastFrameTime == 0L
                            ? 0f
                            : Math.min(
                            (frameTimeNanos - lastFrameTime)
                                    / 1_000_000_000f,
                            0.05f
                    );

                    lastFrameTime = frameTimeNanos;

                    updatePlayer(deltaTime);

                    // 敵の更新
                    enemyManager.update(
                            deltaTime,
                            playerX,
                            playerY,
                            playerSize,
                            getWidth(),
                            getHeight(),
                            currentRoomId,
                            puzzleActive
                    );

                    // ゲームオーバー判定
                    if (enemyManager.isGameOver()) {
                        running = false;
                    }

                    invalidate();

                    if (running) {
                        Choreographer.getInstance()
                                .postFrameCallback(this);
                    }
                }
            };

    // ==============================
    // コンストラクタ
    // ==============================

    public GameView(Context context) {
        super(context);

        // プレイヤー画像の読み込み
        playerBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.character_monster_ghost_white
        );

        // 敵画像の読み込み
        // ★ここを自分の画像リソース名に変更する
        enemyBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.shinigami
        );

        // 敵の管理クラス
        enemyManager = new EnemyManager(
                enemyBitmap,
                ENEMY_SIZE,
                ENEMY_SPEED,
                ENEMY_MIN_DISTANCE
        );

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setKeepScreenOn(true);
    }

    // ==============================
    // 画面サイズ変更
    // ==============================

    @Override
    protected void onSizeChanged(
            int w,
            int h,
            int oldw,
            int oldh
    ) {
        super.onSizeChanged(w, h, oldw, oldh);

        playerX = w * (785f / 1500f);
        playerY = h * (2500f / 2750f);
    }

    // ==============================
    // 描画
    // ==============================

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawGame(canvas);

        // 敵の描画
        enemyManager.draw(canvas);
    }

    private void drawGame(Canvas canvas) {

        if (playerBitmap == null) {
            return;
        }

        float halfSize = playerSize / 2f;

        playerRect.set(
                playerX - halfSize,
                playerY - halfSize,
                playerX + halfSize,
                playerY + halfSize
        );

        canvas.drawBitmap(
                playerBitmap,
                null,
                playerRect,
                null
        );
    }

    // ==============================
    // プレイヤー更新
    // ==============================

    private void updatePlayer(float deltaTime) {

        boolean moveUp = up || touchUp;
        boolean moveDown = down || touchDown;
        boolean moveLeft = left || touchLeft;
        boolean moveRight = right || touchRight;

        movement.move(
                moveUp,
                moveDown,
                moveLeft,
                moveRight,
                deltaTime,
                movementAmount
        );

        playerX += movementAmount[0];
        playerY += movementAmount[1];

        float halfSize = playerSize / 2f;

        playerX = Math.max(
                halfSize,
                Math.min(playerX, getWidth() - halfSize)
        );

        playerY = Math.max(
                halfSize,
                Math.min(playerY, getHeight() - halfSize)
        );
    }

    // ==============================
    // キーボード操作
    // ==============================

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_W:
                up = true;
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_S:
                down = true;
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_A:
                left = true;
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_D:
                right = true;
                return true;

            case KeyEvent.KEYCODE_J:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:

                if (event.getRepeatCount() == 0) {
                    interact();
                }

                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_W:
                up = false;
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_S:
                down = false;
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_A:
                left = false;
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_D:
                right = false;
                return true;

            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    // ==============================
    // タッチ操作
    // ==============================

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {

            touchUp = isInsideButton(
                    x, y, 86, getHeight() - 120
            );

            touchDown = isInsideButton(
                    x, y, 86, getHeight() - 45
            );

            touchLeft = isInsideButton(
                    x, y, 36, getHeight() - 82
            );

            touchRight = isInsideButton(
                    x, y, 136, getHeight() - 82
            );

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                // 風呂場の星をタップ
                if (isInsideButton(
                        x, y,
                        getWidth() * 0.78f,
                        getHeight() * 0.12f
                )) {
                    interact();
                }

                // 冷蔵庫の星をタップ
                if (isInsideButton(
                        x, y,
                        getWidth() * 0.23f,
                        getHeight() * 0.09f
                )) {
                    interact();
                }

                // テレビの星をタップ
                if (isInsideButton(
                        x, y,
                        getWidth() * 0.24f,
                        getHeight() * 0.68f
                )) {
                    interact();
                }

                // 画面右下の操作ボタン
                if (isInsideButton(
                        x, y,
                        getWidth() - 75,
                        getHeight() - 70
                )) {
                    interact();
                }
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {

            touchUp = false;
            touchDown = false;
            touchLeft = false;
            touchRight = false;
        }

        return true;
    }

    private boolean isInsideButton(
            float x,
            float y,
            float buttonX,
            float buttonY
    ) {

        float dx = x - buttonX;
        float dy = y - buttonY;

        return dx * dx + dy * dy <= 30f * 30f;
    }

    private void interact() {

        if (enemyManager.isPuzzleLocked() || puzzleActive) return;

        float dx, dy, distance;

        // 冷蔵庫の星（左上）
        float fridgeX = getWidth() * 0.23f;
        float fridgeY = getHeight() * 0.09f;

        dx = playerX - fridgeX;
        dy = playerY - fridgeY;
        distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance <= 120f) {
            if (numPuzzleRequestListener != null) {
                puzzleStartX = playerX;
                puzzleStartY = playerY;
                numPuzzleRequestListener.onNumPuzzleRequest();
            }
            return;
        }

        // 浴室の星（右上）
        float starX = getWidth() * 0.78f;
        float starY = getHeight() * 0.12f;

        dx = playerX - starX;
        dy = playerY - starY;
        distance = (float) Math.sqrt(dx * dx + dy * dy);
        android.util.Log.d(
                "BATH_TEST",
                "player=(" + playerX + "," + playerY + ")" +
                        " star=(" + starX + "," + starY + ")" +
                        " distance=" + distance +
                        " listener=" + (puzzleRequestListener != null)
        );

        if (distance <= 120f) {
            if (puzzleRequestListener != null) {
                puzzleStartX = playerX;
                puzzleStartY = playerY;
                puzzleRequestListener.onPuzzleRequest();
            }
            return;
        }

        // テレビの星（左下）
        float tvX = getWidth() * 0.24f;
        float tvY = getHeight() * 0.68f;

        dx = playerX - tvX;
        dy = playerY - tvY;
        distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance <= 120f) {
            if (magicPuzzleRequestListener != null) {
                puzzleStartX = playerX;
                puzzleStartY = playerY;
                magicPuzzleRequestListener.onMagicPuzzleRequest();
            }
        }
    }

    public void restorePuzzlePosition() {
        playerX = puzzleStartX;
        playerY = puzzleStartY;
        invalidate();
    }

    // ==============================
    // ゲームループ開始・終了
    // ==============================

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        setFocusableInTouchMode(true);
        requestFocus();

        running = true;
        lastFrameTime = 0L;

        Choreographer.getInstance()
                .postFrameCallback(frameCallback);
    }

    @Override
    protected void onDetachedFromWindow() {

        running = false;

        Choreographer.getInstance()
                .removeFrameCallback(frameCallback);

        super.onDetachedFromWindow();
    }

    // ==============================
    // 外部から使用するメソッド
    // ==============================

    public boolean consumeInteractRequest() {

        if (!interactRequested) {
            return false;
        }

        interactRequested = false;

        return true;
    }

    public float getPlayerX() {
        return playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public boolean isGameOver() {
        return enemyManager.isGameOver();
    }

    public void setPuzzleActive(boolean active) {
        if (puzzleActive == active) return;

        puzzleActive = active;

        if (active) {
            // ★パズルへ移動する瞬間に移動フラグをリセットする
            up = down = left = right = false;
            touchUp = touchDown = touchLeft = touchRight = false;
            enemyManager.onPuzzleStarted();
        } else {
            enemyManager.onPuzzleEnded();
        }
    }

    public interface OnNumPuzzleRequestListener {
        void onNumPuzzleRequest();
    }
    public interface OnMagicPuzzleRequestListener {
        void onMagicPuzzleRequest();
    }

    private OnMagicPuzzleRequestListener magicPuzzleRequestListener;

    public void setOnMagicPuzzleRequestListener(
            OnMagicPuzzleRequestListener listener
    ) {
        this.magicPuzzleRequestListener = listener;
    }

    private OnNumPuzzleRequestListener numPuzzleRequestListener;

    public void setOnNumPuzzleRequestListener(
            OnNumPuzzleRequestListener listener
    ) {
        this.numPuzzleRequestListener = listener;
    }

    public void setRoomId(int roomId) {
        currentRoomId = roomId;
    }
}
