package com.projeto.controllers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projeto.dao.EngineGame;
import com.projeto.interfaces.EngineGameInterface;
import com.projeto.util.Emotiv;
import com.projeto.util.Util;
import com.projeto.game.GameMentalView;

public class FragmentGame extends Fragment implements EngineGameInterface{

    private GameMentalView gameMentalView;
    private EngineGame engineGame;
    private ActivityMain activityMain;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        gameMentalView = new GameMentalView(getActivity());
        return gameMentalView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        engineGame = EngineGame.shareInstance(this);
        activityMain = (ActivityMain) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        engineGame.createTimerTask();
        Log.d(Util.TAG, "Game OnResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        engineGame.stopTimertask();
        Log.d(Util.TAG, "Game OnPause");
    }

    @Override
    public void currentAction(int typeAction, float power) {
        if(gameMentalView != null && gameMentalView.getGameMentalDraw() != null) {
            if (typeAction == Emotiv.COMMAND_NEUTRAL) {
                gameMentalView.getGameMentalDraw().toIdle();
            } else if (typeAction == Emotiv.COMMAND_PUSH) {
                gameMentalView.getGameMentalDraw().moveToTopWith(power);
            } else if (typeAction == Emotiv.COMMAND_PULL) {
                gameMentalView.getGameMentalDraw().moveToBottomWith(power);
            } else if (typeAction == Emotiv.COMMAND_LEFT) {
                gameMentalView.getGameMentalDraw().moveToLeftWith(power);
            } else if (typeAction == Emotiv.COMMAND_RIGHT) {
                gameMentalView.getGameMentalDraw().moveToRightWith(power);
            }
        }
    }

    @Override
    public void onUserRemoved() {
        activityMain.showMessageSnackbar(R.string.connect_emotiv_fail);
    }
}