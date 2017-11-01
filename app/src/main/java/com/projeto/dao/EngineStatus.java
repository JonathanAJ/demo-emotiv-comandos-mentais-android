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
    private int state;
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
        // Roda um TimerTask com delay inicial 0 a cada 10 milissegundos
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
                /*Conexão com Epoc Plus*/
                int numberDevice = IEdk.IEE_GetEpocPlusDeviceCount();
                if (numberDevice != 0) {
                    Log.d(Util.TAG, "Conectando-se...");
                    if (!Emotiv.isConnected()) {
                        // Conecta Epoc+, posicao 0 na lista, modo de configuração falso
                        IEdk.IEE_ConnectEpocPlusDevice(0, false);
                    }
                }
                // Recupera o pŕoximo evento de EmoEngine
                // Retorna Emotiv.OK, ERROR ou NO EVENT
                state = IEdk.IEE_EngineGetNextEvent();
                if (state == Emotiv.OK) {

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

                    int typeEvent = IEdk.IEE_EmoEngineEventGetType();

                    if(typeEvent == Emotiv.TYPE_USER_ADD){
                        Log.d(Util.TAG, "Emotiv Conectado");
                        Emotiv.setConnected(true);
                        // Retorna o ID do usuário nos eventos IEE_UserAdded e IEE_UserRemoved.
                        Emotiv.setUserID(IEdk.IEE_EmoEngineEventGetUserId());
                    }
                    else if(typeEvent == Emotiv.TYPE_USER_REMOVE){
                        Log.d(Util.TAG, "Emotiv Desconectado");
                        Emotiv.setConnected(false);
                        Emotiv.clearUserID();
                    }
                    else if(typeEvent == Emotiv.TYPE_EMOSTATE_UPDATE){
                        if (!Emotiv.isConnected())
                            return;
                        // Retorna um EmoState na memória
                        IEdk.IEE_EmoEngineEventGetEmoState();
                        handler.sendEmptyMessage(0);
                    }
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