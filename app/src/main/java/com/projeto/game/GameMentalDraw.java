package com.projeto.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.projeto.controllers.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMentalDraw {

    private int WIDTH, HEIGHT;

    private Canvas canvas;
    private Context context;
    private float myX;
    private float myY;
    private float coinX;
    private float coinY;
    private float velocity = 10;
    private int score = 0;

    private GameAnimation coinAnimation;

    private GameAnimation idleAnimation;
    private GameAnimation leftAnimation;
    private GameAnimation rightAnimation;
    private GameAnimation topAnimation;
    private GameAnimation bottomAnimation;

    private GameMultiAnimation myAnimation;

    public GameMentalDraw(Canvas canvas, Context context){
        this.canvas = canvas;
        this.context = context;
        initConfigs();
        initAnimations();
    }

    public void initConfigs(){
        WIDTH = canvas.getWidth();
        HEIGHT = canvas.getHeight();
        myX = WIDTH * 0.5f;
        myY = HEIGHT * 0.5f;
        coinX = WIDTH * 0.5f;
        coinY = 200;
    }

    public void initAnimations(){

        coinAnimation = new GameAnimation(context,
                new int[]{
                        R.drawable.coin_1,
                        R.drawable.coin_2,
                        R.drawable.coin_3,
                        R.drawable.coin_4,
                        R.drawable.coin_5,
                        R.drawable.coin_6,
                        R.drawable.coin_7,
                        R.drawable.coin_8
                }, 1500);
        coinAnimation.play();

        leftAnimation = new GameAnimation(context,
                new int[]{
                        R.drawable.left_0,
                        R.drawable.left_1,
                        R.drawable.left_2,
                        R.drawable.left_3,
                        R.drawable.left_4,
                        R.drawable.left_5,
                        R.drawable.left_6,
                        R.drawable.left_7,
                }, 1000);

        rightAnimation = new GameAnimation(context,
                new int[]{
                        R.drawable.right_0,
                        R.drawable.right_1,
                        R.drawable.right_2,
                        R.drawable.right_3,
                        R.drawable.right_4,
                        R.drawable.right_5,
                        R.drawable.right_6,
                        R.drawable.right_7,
                }, 1000);

        topAnimation = new GameAnimation(context,
                new int[]{
                        R.drawable.top_0,
                        R.drawable.top_1,
                        R.drawable.top_2,
                        R.drawable.top_3,
                        R.drawable.top_4,
                        R.drawable.top_5,
                        R.drawable.top_6,
                        R.drawable.top_7,
                }, 1000);

        bottomAnimation = new GameAnimation(context,
                new int[]{
                        R.drawable.bottom_0,
                        R.drawable.bottom_1,
                        R.drawable.bottom_2,
                        R.drawable.bottom_3,
                        R.drawable.bottom_4,
                        R.drawable.bottom_5,
                        R.drawable.bottom_6,
                        R.drawable.bottom_7,
                }, 1000);

        idleAnimation = new GameAnimation(context,
                new int[]{
                        R.drawable.bottom_4
                }, 1000);

        List<GameAnimation> gameAnimationList = new ArrayList<>();
        gameAnimationList.add(idleAnimation);
        gameAnimationList.add(leftAnimation);
        gameAnimationList.add(rightAnimation);
        gameAnimationList.add(topAnimation);
        gameAnimationList.add(bottomAnimation);

        myAnimation = new GameMultiAnimation(gameAnimationList);
        myAnimation.play();
    }

    private void drawGround(Canvas canvas){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ground);
        float bitWidth = bitmap.getWidth();
        float bitHeight = bitmap.getHeight();
        float cols = WIDTH/bitWidth;
        float rows = HEIGHT/bitHeight;

        for(int i = 0; i <= cols; i++){
            for(int j = 0; j <=rows; j++) {
                canvas.drawBitmap(bitmap, i * bitWidth, j * bitHeight, null);
            }
        }
    }

    private void drawScore(Canvas canvas){
        Paint scorePaint = new Paint();
        scorePaint.setAntiAlias(true);
        scorePaint.setColor(Color.BLACK);
        scorePaint.setTextSize(24);
        canvas.drawText("Pontos:", 30, 50, scorePaint);
        scorePaint.setFakeBoldText(true);
        canvas.drawText(""+score, 130, 50, scorePaint);
    }

    public void onDrawGame(Canvas canvas){
        drawGround(canvas);
        drawScore(canvas);
        myAnimation.draw(canvas, myX, myY);
        coinAnimation.draw(canvas, coinX, coinY);

        if(isColission()){
            score += 100;
            setCoinRandom();
        }

        onLimited();
    }

    private void onLimited(){
        if(myX < 0)
            myX = WIDTH;
        else if(myX > WIDTH)
            myX = 0;

        if(myY < 0)
            myY = HEIGHT;
        else if(myY > HEIGHT)
            myY = 0;
    }

    private void setCoinRandom(){
        coinX = (float) (Math.random() * WIDTH);
        coinY = (float) (Math.random() * HEIGHT);
    }

    private boolean isColission(){
        GameAnimation.CircleDetection myDetection = myAnimation.getCurrentAnimation().getCircleDetection();
        GameAnimation.CircleDetection coinDetection = coinAnimation.getCircleDetection();

        double distancia = Math.sqrt(
                                Math.pow((double) (myDetection.getX() - coinDetection.getX()), 2)
                                +
                                Math.pow((double) (myDetection.getY() - coinDetection.getY()), 2));
        double radius = myDetection.getRadius() + coinDetection.getRadius();

        if(distancia > radius)
            return false;
        else
            return true;
    }

    public void moveToRightWith(float power){
        myX = myX + (velocity * power);
        myAnimation.play(rightAnimation);
    }

    public void moveToLeftWith(float power){
        myX = myX - (velocity * power);
        myAnimation.play(leftAnimation);
    }

    public void moveToTopWith(float power){
        myY = myY - (velocity * power);
        myAnimation.play(topAnimation);
    }

    public void moveToBottomWith(float power){
        myY = myY + (velocity * power);
        myAnimation.play(bottomAnimation);
    }

    public void toIdle(){
        myAnimation.play(idleAnimation);
    }

}