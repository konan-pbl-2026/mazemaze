package com.example.helloandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;


public class GameView extends View{
    private final Movement movement = new Movement();

    private float playerX = 400f;
    private float playerY = 400f;
    private boolean up,down,left,right;
    private boolean touchUp,touchDown,touchLeft,touchRight;
    private long lastFrameTime = 0L;
    private boolean interactRequested = false;

    public GameView(Context context)
    {
        super(context);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setKeepScreenOn(true);


    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        long now = System.nanoTime();

        float deltaTime = lastFrameTime == 0L?0f:Math.min(
                (now -lastFrameTime)/1_000_000_000f,0.05f);

        lastFrameTime = now;
        updatePlayer (deltaTime);
        postInvalidateOnAnimation();
    }

    private void drawGame(Canvas canvas)
    {

    }

    private void updatePlayer(float deltaTime)
    {
        boolean moveUp = up || touchUp;
        boolean moveDown = down || touchDown;
        boolean moveLeft = left || touchLeft;
        boolean moveRight = right || touchRight;

        float [] movementAmount = movement.move
                (
                        moveUp,
                        moveDown,
                        moveLeft,
                        moveRight,
                        deltaTime);

        playerX += movementAmount[0];
        playerY += movementAmount[1];

        playerX = Math.max(0f,Math.min(playerX,getWidth()));
        playerY = Math.max(0f,Math.min(playerY,getHeight()));

    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_W:
                up = true;
                return  true;

            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                down = true;
                return true;

            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                left = true;
                return true;

            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                right = true;
                return true;

            case KeyEvent.KEYCODE_J:
            case KeyEvent.KEYCODE_ENTER:
                if (event.getRepeatCount()==0)
                {
                    interact();
                }
                return true;

            default:
                return super.onKeyDown(keyCode,event);
        }

    }

    @Override
    public boolean onKeyUp(int keyCode,KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_W:
            case KeyEvent.KEYCODE_DPAD_UP:
                up = false;
                return true;

            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                down = false;
                return true;

            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                left = false;
                return  true;

            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                right = false;
                return  true;

            default:
                return super.onKeyUp(keyCode,event);

        }
    }

    @Override
    public boolean performClick()
    {
        super.performClick();
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        if(event.getAction() == MotionEvent.ACTION_DOWN||
        event.getAction() == MotionEvent.ACTION_MOVE)
        {
            touchUp = isInsideButton(x,y,85,getHeight()-120);

            touchDown = isInsideButton(x,y,85,getHeight()-45);

            touchLeft = isInsideButton(x,y,35,getHeight()-82);

            touchRight = isInsideButton(x,y,135,getHeight()-82);

            if(event.getAction() == MotionEvent.ACTION_DOWN &&
            isInsideButton
                    (x,y,getWidth() - 75,getHeight() - 70))
            {
                interact();
            }

        }

        else if(event.getAction() == MotionEvent.ACTION_UP||
        event.getAction() == MotionEvent.ACTION_CANCEL)
        {
            touchUp = false;
            touchDown = false;
            touchLeft = false;
            touchRight = false;
        }
        return true;
    }

    private boolean isInsideButton(float x,float y,float buttonX,float buttonY)
    {
        float dx = x - buttonX;
        float dy = y - buttonY;

        return dx * dx + dy * dy <= 30f * 30f;
    }

    private void interact()
    {
        interactRequested = true;
    }

    public boolean consumeInteractRequest()
    {
        return interactRequested;
    }

    public float getPlayerX()
    {
        return playerX;
    }

    public float getPlayerY()
    {
        return playerY;
    }

}
