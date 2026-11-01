package com.example.helloandroid;

public class Movement {

    private static final float MOVE_SPEED = 320f;
    public void move(boolean up,
                     boolean down,
                     boolean left,
                     boolean right,
                     float deltaTime,
                     float[] result)
    {
        result[0]= 0f;
        result[1]= 0f;

        if(up)
        {
            result[1]= -MOVE_SPEED *deltaTime;
        }
        else if(down)
        {
            result[1] = MOVE_SPEED *deltaTime;
        }
        else if(left)
        {
            result[0] = -MOVE_SPEED *deltaTime;
        }
        else if(right)
        {
            result[0] = MOVE_SPEED *deltaTime;
        }
    }
}
