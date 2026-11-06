package com.example.helloandroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

public class Enemy {

    private final Bitmap enemyBitmap;
    private final float enemySize;

    private float x;
    private float y;

    private final RectF enemyRect = new RectF();

    public Enemy(Bitmap bitmap, float x, float y, float enemySize) {
        this.enemyBitmap = bitmap;
        this.x = x;
        this.y = y;
        this.enemySize = enemySize;
    }

    public void draw(Canvas canvas) {

        if (enemyBitmap == null) {
            return;
        }

        float halfSize = enemySize / 2f;

        enemyRect.set(
                x - halfSize,
                y - halfSize,
                x + halfSize,
                y + halfSize
        );

        canvas.drawBitmap(
                enemyBitmap,
                null,
                enemyRect,
                null
        );
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return enemySize;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
