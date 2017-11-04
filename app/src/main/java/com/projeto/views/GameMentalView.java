package com.projeto.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.projeto.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class GameMentalView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private Canvas canvas;
    private Timer timer;
    private TimerTask gameLoop;
    private GameMentalDraw gameMentalDraw;

    public GameMentalDraw getGameMentalDraw() {
        return gameMentalDraw;
    }

    public GameMentalView(Context context){
        super(context);
        holder = getHolder();
        holder.addCallback(this);
    }

    public TimerTask initGameLoop(){
        gameLoop = new TimerTask() {
            @Override
            public void run() {
                canvas = holder.lockCanvas();
                if(canvas != null){
                    if(gameMentalDraw == null){
                        gameMentalDraw = new GameMentalDraw(canvas);
                    }
                    canvas.drawColor(Color.BLACK);
                    gameMentalDraw.onDrawGame(canvas);
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        };
        return gameLoop;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(Util.TAG, "SurfaceGame Create");
        timer = new Timer();
        timer.schedule(initGameLoop(), 0, 10);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(Util.TAG, "SurfaceGame Destroy");
        gameLoop.cancel();
        timer.cancel();
        gameLoop = null;
        timer = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
}