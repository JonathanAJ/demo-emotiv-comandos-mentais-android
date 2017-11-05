package com.projeto.game;

import android.graphics.Canvas;

import java.util.List;

public class GameMultiAnimation {

    private List<GameAnimation> gameAnimationList;
    private GameAnimation currentAnimation;

    public GameMultiAnimation(List<GameAnimation> gameAnimationList){
        this.gameAnimationList = gameAnimationList;
        currentAnimation = this.gameAnimationList.get(0);
    }

    public GameAnimation getCurrentAnimation() {
        return currentAnimation;
    }

    public void play(){
        currentAnimation.play();
    }

    public void stop(){
        currentAnimation.stop();
    }

    public void play(GameAnimation gameAnimation){
        for(GameAnimation game : gameAnimationList){
            if (game != gameAnimation)
                game.stop();
        }
        currentAnimation = gameAnimation;
        currentAnimation.play();
    }

    public void draw(Canvas canvas, float x, float y){
        currentAnimation.draw(canvas, x, y);
    }

}