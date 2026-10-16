package com.example.helloandroid;

public class Movement {

    public float[] move(boolean up,
                        boolean down,
                        boolean left,
                        boolean right,
                        float deltaTime)
    {
        float dx = 0f;
        float dy = 0f;

        float moveSpeed = 240f;
        if(up)
        {
            dy = -moveSpeed *deltaTime;
        }
        else if(down)
        {
            dy = moveSpeed *deltaTime;
        }
        else if(left)
        {
            dx = -moveSpeed *deltaTime;
        }
        else if(right)
        {
            dx = moveSpeed *deltaTime;
        }

        return new float[]{dx,dy};
    }

}
