package com.example.helloandroid;

public class EnemyMovement {

    private final float MOVE_SPEED;

    public EnemyMovement(float moveSpeed) {
        this.MOVE_SPEED = moveSpeed;
    }

    public void move(
            Enemy enemy,
            float playerX,
            float playerY,
            float deltaTime
    ) {
        // プレイヤーとの距離を計算
        float dx = playerX - enemy.getX();
        float dy = playerY - enemy.getY();

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 十分近ければ移動しない
        if (distance < 1f) {
            return;
        }

        // 移動方向を正規化
        float directionX = dx / distance;
        float directionY = dy / distance;

        // 移動量を計算
        float moveAmount = MOVE_SPEED * deltaTime;

        // プレイヤーを通り越さないようにする
        moveAmount = Math.min(moveAmount, distance);

        // 敵の位置を更新
        float newX = enemy.getX() + directionX * moveAmount;
        float newY = enemy.getY() + directionY * moveAmount;

        enemy.setPosition(newX, newY);
    }
}
