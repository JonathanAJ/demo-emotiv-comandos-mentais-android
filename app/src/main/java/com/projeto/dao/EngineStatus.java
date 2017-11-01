package com.projeto.dao;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEmoStateDLL;
import com.projeto.interfaces.EngineStatusInterface;
import com.projeto.util.Emotiv;
import com.projeto.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class EngineStatus {

    private EngineStatusInterface delegate;
    private TimerTask timerTask;
    private Timer timer;
    private static EngineStatus engineInstance = null;
    private double statusQuality;

    public static EngineStatus shareInstance(EngineStatusInterface delegate) {
        if (engineInstance == null) {
            engineInstance = new EngineStatus(delegate);
        }
        return engineInstance;
    }

    private EngineStatus(EngineStatusInterface delegate){
        this.delegate = delegate;
        Log.d(Util.TAG, "EngineStatus");
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
                int typeEvent = IEdk.IEE_EmoEngineEventGetType();
                if(typeEvent == Emotiv.TYPE_EMOSTATE_UPDATE) {
                    if(Emotiv.isConnected()) {
                        // Retorna um EmoState na memória quando houver mudanças
                        IEdk.IEE_EmoEngineEventGetEmoState();
                        /*
                         * Verifica a qualidade do sinal
                         */
                        int values[] = IEmoStateDLL.IS_GetContactQualityFromAllChannels();
                        int numGood = 0;
                        for (int value : values) {
                            if (value == IEmoStateDLL.IEE_EEG_ContactQuality_t.IEEG_CQ_GOOD.ordinal())
                                numGood++;
                        }
                        // calcula a porcentagem da qualidade boa de sinal
                        statusQuality = ((double) numGood / (double) values.length) * 100.0;
                        Log.d(Util.TAG, "Qualidade: " + statusQuality + "%");
                    }else{
                        statusQuality = 0;
                    }
                    handler.sendEmptyMessage(0);
                }
            }
        };
        return timerTask;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (delegate != null)
                        delegate.updateStatusQuality(statusQuality);
                    break;
                default:
                    break;
            }
        }
    };
}