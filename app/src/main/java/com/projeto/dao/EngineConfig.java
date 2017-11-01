package com.projeto.dao;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.projeto.interfaces.EngineConfigInterface;
import com.projeto.util.Emotiv;
import com.projeto.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class EngineConfig {

    private EngineConfigInterface delegate;
    private TimerTask timerTask;
    private Timer timer;

    private static EngineConfig engineInstance = null;

    public static EngineConfig shareInstance(EngineConfigInterface delegate) {
        if (engineInstance == null) {
            engineInstance = new EngineConfig(delegate);
        }
        return engineInstance;
    }

    private EngineConfig(EngineConfigInterface delegate){
        this.delegate = delegate;
        Log.d(Util.TAG, "EngineConfig");
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
                // Retorna OK, ERROR ou NO EVENT
                int state = IEdk.IEE_EngineGetNextEvent();
                if (state == Emotiv.OK) {
                    int typeEvent = IEdk.IEE_EmoEngineEventGetType();
                    if(typeEvent == Emotiv.TYPE_USER_ADD) {
                        Log.d(Util.TAG, "Emotiv Conectado");
                        Emotiv.setConnected(true);
                        // Retorna o ID do usuário padrão
                        Emotiv.setUserID(IEdk.IEE_EmoEngineEventGetUserId());
                        handler.sendEmptyMessage(Emotiv.TYPE_USER_ADD);
                    }
                    else if(typeEvent == Emotiv.TYPE_USER_REMOVE) {
                        Log.d(Util.TAG, "Emotiv Desconectado");
                        Emotiv.setConnected(false);
                        Emotiv.clearUserID();
                        handler.sendEmptyMessage(Emotiv.TYPE_USER_REMOVE);
                    }
                }
            }
        };
        return timerTask;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (delegate != null){
                if (msg.what == Emotiv.TYPE_USER_ADD)
                        delegate.userAdd();
                else if(msg.what == Emotiv.TYPE_USER_REMOVE)
                        delegate.userRemoved();
            }
        }
    };

}