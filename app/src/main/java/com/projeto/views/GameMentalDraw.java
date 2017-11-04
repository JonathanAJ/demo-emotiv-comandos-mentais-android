package com.projeto.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class GameMentalDraw {

    private Paint paint;
    private float myX;
    private float myY;
    private float velocity = 10;

    public GameMentalDraw(Canvas canvas){
        paint = new Paint();
        paint.setColor(Color.CYAN);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        myX = canvas.getWidth() * 0.5f;
        myY = canvas.getHeight() * 0.5f;
    }

    public void onDrawGame(Canvas canvas){
        canvas.drawCircle(myX, myY, 20, paint);
    }

    public void moveToRightWith(float power){
        myX = myX + (velocity * power);
    }

    public void moveToLeftWith(float power){
        myX = myX - (velocity * power);
    }

    public void moveToTopWith(float power){
        myY = myY - (velocity * power);
    }

    public void moveToBottomWith(float power){
        myY = myY + (velocity * power);
    }

}