package com.projeto.dao;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEmoStateDLL;
import com.projeto.interfaces.EngineGameInterface;
import com.projeto.util.Emotiv;
import com.projeto.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class EngineGame {

    private EngineGameInterface delegate;
    private TimerTask timerTask;
    private Timer timer;
    private static EngineGame engineInstance = null;

    public static EngineGame shareInstance(EngineGameInterface delegate) {
        if (engineInstance == null) {
            engineInstance = new EngineGame(delegate);
        }
        return engineInstance;
    }

    private EngineGame(EngineGameInterface delegate){
        this.delegate = delegate;
        Log.d(Util.TAG, "EngineGame");
    }

    public void createTimerTask(){
        // Roda um TimerTask com delay inicial 0 a cada 100 milissegundos
        timer = new Timer();
        timer.schedule(initTimerTask(), 0, 10);
    }

    public void stopTimertask(){
        timerTask.cancel();
        timer.cancel();
        timerTask = null;
        timer = null;
    }

    private TimerTask initTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // Recupera o pŕoximo evento de EmoEngine
                // Retorna OK, ERROR ou NO EVENT
                if(Emotiv.isConnected()) {
                    int state = IEdk.IEE_EngineGetNextEvent();
                    if (state == Emotiv.OK) {

                        int typeEvent = IEdk.IEE_EmoEngineEventGetType();

                        if (typeEvent == Emotiv.USER_REMOVED) {
                            Log.d(Util.TAG, "Emotiv Desconectado");
                            Emotiv.setConnected(false);
                            Emotiv.clearUserID();
                            handler.sendEmptyMessage(Emotiv.USER_REMOVED);

                        } else if (typeEvent == Emotiv.EMOSTATE_UPDATED) {
                            // Retorna um EmoState na memória quando houver mudanças
                            IEdk.IEE_EmoEngineEventGetEmoState();
                            handler.sendEmptyMessage(Emotiv.EMOSTATE_UPDATED);
                        }
                    }
                }
            }
        };
        return timerTask;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(delegate != null) {
                int type = msg.what;
                if (type == Emotiv.EMOSTATE_UPDATED) {
                    delegate.currentAction(IEmoStateDLL.IS_MentalCommandGetCurrentAction(),
                            IEmoStateDLL.IS_MentalCommandGetCurrentActionPower());
                } else if (type == Emotiv.USER_REMOVED) {
                    delegate.onUserRemoved();
                }
            }
        }
    };
}
