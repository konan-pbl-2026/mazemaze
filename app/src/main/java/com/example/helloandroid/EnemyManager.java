
package com.example.helloandroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

public class EnemyManager {

    // 時間設定
    private static final float SPAWN_INTERVAL = 90f;
    private static final float EXIST_TIME = 10f;

    // 敵の設定
    private final Bitmap enemyBitmap;
    private final float enemySize;
    private final float minSpawnDistance;

    // 移動処理
    private final EnemyMovement enemyMovement;

    // 敵の情報
    private Enemy enemy;

    // 時間管理
    private float spawnTimer = 0f;
    private float enemyTimer = 0f;

    // 状態管理
    private boolean gameOver = false;
    private int currentRoomId = Integer.MIN_VALUE;

    private final Random random = new Random();

    // コンストラクタ
    public EnemyManager(
            Bitmap enemyBitmap,
            float enemySize,
            float enemySpeed,
            float minSpawnDistance
    ) {

        this.enemyBitmap = enemyBitmap;
        this.enemySize = enemySize;
        this.minSpawnDistance = minSpawnDistance;

        enemyMovement = new EnemyMovement(enemySpeed);
    }

    // 毎フレーム呼び出す処理
    public void update(
            float deltaTime,
            float playerX,
            float playerY,
            float playerSize,
            float roomWidth,
            float roomHeight,
            int roomId,
            boolean puzzleActive
    ) {

        if (gameOver) {
            return;
        }

        deltaTime = Math.max(0f, Math.min(deltaTime, 0.05f));

        // 部屋が変わった場合
        if (currentRoomId != roomId) {

            if (enemy != null) {
                removeEnemy();
            }

            currentRoomId = roomId;
        }

        // 敵が存在する場合
        if (enemy != null) {

            enemyTimer += deltaTime;

            enemyMovement.move(
                    enemy,
                    playerX,
                    playerY,
                    deltaTime
            );

            // プレイヤーとの接触判定
            checkCollision(
                    playerX,
                    playerY,
                    playerSize
            );

            // ゲームオーバーなら終了
            if (gameOver) {
                return;
            }

            // 20秒経過で消滅
            if (enemyTimer >= EXIST_TIME) {
                removeEnemy();
            }

            return;
        }

        // パズル中は出現タイマーを停止
        if (puzzleActive) {
            return;
        }

        // 敵がいない通常プレイ中だけ計測
        spawnTimer += deltaTime;

        // 60秒経過で敵を出現させる
        if (spawnTimer >= SPAWN_INTERVAL) {

            spawnEnemy(
                    playerX,
                    playerY,
                    roomWidth,
                    roomHeight
            );
        }
    }
    public void onPuzzleStarted() {
        removeEnemy();
        spawnTimer = 0f;
    }

    public void onPuzzleEnded() {
        spawnTimer = 0f;
    }

    // 敵の出現処理
    private void spawnEnemy(
            float playerX,
            float playerY,
            float roomWidth,
            float roomHeight
    ) {

        if (enemyBitmap == null) {
            return;
        }

        float halfSize = enemySize / 2f;

        float minX = halfSize;
        float maxX = roomWidth - halfSize;

        float minY = halfSize;
        float maxY = roomHeight - halfSize;

        // 部屋が小さすぎる場合
        if (maxX < minX || maxY < minY) {
            spawnTimer = 0f;
            return;
        }

        float spawnX = 0f;
        float spawnY = 0f;

        boolean validPosition = false;

        // ランダムな出現位置を探す
        for (int i = 0; i < 100; i++) {

            spawnX = minX
                    + random.nextFloat() * (maxX - minX);

            spawnY = minY
                    + random.nextFloat() * (maxY - minY);

            float dx = spawnX - playerX;
            float dy = spawnY - playerY;

            float distance = (float) Math.sqrt(
                    dx * dx + dy * dy
            );

            // プレイヤーから一定距離以上離れているか
            if (distance >= minSpawnDistance) {
                validPosition = true;
                break;
            }
        }

        // 出現可能な場所がなかった場合
        if (!validPosition) {
            spawnTimer = 0f;
            return;
        }

        // 敵を生成
        enemy = new Enemy(
                enemyBitmap,
                spawnX,
                spawnY,
                enemySize
        );

        enemyTimer = 0f;
        spawnTimer = 0f;
    }

    // 当たり判定
    private void checkCollision(
            float playerX,
            float playerY,
            float playerSize
    ) {

        float dx = Math.abs(playerX - enemy.getX());
        float dy = Math.abs(playerY - enemy.getY());

        float collisionDistance =
                (playerSize + enemySize) / 2f;

        if (dx < collisionDistance
                && dy < collisionDistance) {

            gameOver = true;
        }
    }

    // 敵の消滅処理
    private void removeEnemy() {

        enemy = null;

        enemyTimer = 0f;
        spawnTimer = 0f;
    }

    // 敵の描画
    public void draw(Canvas canvas) {

        if (enemy != null) {
            enemy.draw(canvas);
        }
    }

    // 敵が存在するか
    public boolean isEnemyActive() {
        return enemy != null;
    }

    // パズル操作を禁止するか
    public boolean isPuzzleLocked() {
        return enemy != null;
    }

    // ゲームオーバー判定
    public boolean isGameOver() {
        return gameOver;
    }

    // 出現タイマー取得
    public float getSpawnTimer() {
        return spawnTimer;
    }

    // 敵の生存時間取得
    public float getEnemyTimer() {
        return enemyTimer;
    }
}
