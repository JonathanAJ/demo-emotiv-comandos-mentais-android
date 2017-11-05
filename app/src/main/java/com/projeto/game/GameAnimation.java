package com.projeto.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

public class GameAnimation {

    private Context context;
    private List<Bitmap> frames;
    private int frameIndex;
    private boolean isPlay = false;
    private long lastFrame;
    private long frameDuration;
    private CircleDetection circleDetection;

    /**
     * @param frames id frames de drawable
     * @param duration duração em milisegundos
     */
    public GameAnimation(Context context, int[] frames, int duration){
        this.context = context;
        this.frames = getDecodeBitmap(frames);
        frameIndex = 0;
        frameDuration = duration / frames.length;
        lastFrame = System.currentTimeMillis();
        circleDetection = new CircleDetection();
    }

    private List<Bitmap> getDecodeBitmap(int[] res){
        List<Bitmap> bitmapList = new ArrayList<>();
        for(int id : res){
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
            bitmapList.add(bitmap);
        }
        return bitmapList;
    }

    public CircleDetection getCircleDetection() {
        return circleDetection;
    }

    public void play(){
        isPlay = true;
        frameIndex = 0;
        lastFrame = System.currentTimeMillis();
    }

    public void stop(){
        isPlay = false;
    }

    public void draw(Canvas canvas, float x, float y){
        if(isPlay){
            Bitmap bitmap = frames.get(frameIndex);
            // anchor x e y são no centro
            float xBitmap = x - (bitmap.getWidth() * 0.5f);
            float yBitmap = y - (bitmap.getHeight() * 0.5f);
            canvas.drawBitmap(bitmap, xBitmap, yBitmap, null);

            circleDetection.setX(x);
            circleDetection.setY(y);
            circleDetection.setRadius((bitmap.getWidth() * 0.45f));

//            Paint paint = new Paint();
//            paint.setColor(Color.WHITE);
//            paint.setAlpha(100);
//            canvas.drawCircle(circleDetection.getX(), circleDetection.getY(), circleDetection.getRadius(), paint);
            updateFrame();
        }
    }

    public void updateFrame(){
        if(System.currentTimeMillis() - lastFrame > frameDuration){
            frameIndex++;
            frameIndex = frameIndex >= frames.size() ? 0 : frameIndex;
            lastFrame = System.currentTimeMillis();
        }
    }

    public class CircleDetection {
        private float x, y, radius;

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }
    }
}
