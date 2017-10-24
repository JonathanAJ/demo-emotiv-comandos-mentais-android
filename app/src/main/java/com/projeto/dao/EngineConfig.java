package com.projeto.dao;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.emotiv.insight.IEmoStateDLL;
import com.projeto.interfaces.EngineConfigInterface;
import com.projeto.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class EngineConfig {

    private EngineConfigInterface delegate;
    private boolean isConnected = false;
    private TimerTask timerTask;
    private Timer timer;
    private int userId = -1;

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

    public void newTimerTask(){
        timerTask();
        timer = new Timer();
        // Roda um TimerTask com delay inicial 0 a cada 10 milissegundos
        timer.schedule(timerTask, 0, 10);
    }

    public void cancelTimerTask(){
        timerTask.cancel();
        timer.cancel();
    }

    private void timerTask() {
        // Inicializa TimerTask rodando a cada 10 milissegundos
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
					/*Conexão com Epoc Plus*/
                    int numberDevice = IEdk.IEE_GetEpocPlusDeviceCount();
                    if (numberDevice != 0) {
                        Log.d(Util.TAG, "Config conectando-se...");
                        if (!isConnected) {
                            // Conecta Epoc+, posicao 0 na lista, modo de configuração falso
                            IEdk.IEE_ConnectEpocPlusDevice(0, false);
                        }
                    }
                    // Recupera o pŕoximo evento de EmoEngine
                    // Retorna OK, ERROR ou NO EVENT
                    if (IEdk.IEE_EngineGetNextEvent() == IEdkErrorCode.EDK_OK.ToInt()) {
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
                        double resNumGood = ((double) numGood / (double) values.length) * 100.0;
                        Log.d(Util.TAG, "Qualidade: " + resNumGood + "%");

                        int typeEvent = IEdk.IEE_EmoEngineEventGetType();
                        if (typeEvent == IEdk.IEE_Event_t.IEE_UserAdded.ToInt()) {
                            Log.d(Util.TAG, "Emotiv Conectado Config");
                            isConnected = true;
                            // Retorna o ID do usuário nos eventos IEE_UserAdded e IEE_UserRemoved.
                            userId = IEdk.IEE_EmoEngineEventGetUserId();
                            handler.sendEmptyMessage(IEdk.IEE_Event_t.IEE_UserAdded.ToInt());

                        } else if (typeEvent == IEdk.IEE_Event_t.IEE_UserRemoved.ToInt()) {
                            Log.d(Util.TAG, "Emotiv Desconectado Config");
                            isConnected = false;
                            userId = -1;
                            handler.sendEmptyMessage(IEdk.IEE_Event_t.IEE_UserRemoved.ToInt());
                        }
                    }
                }
            };
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == IEdk.IEE_Event_t.IEE_UserAdded.ToInt()){
                if (delegate != null)
                    delegate.userAdd(userId);

            }else if(msg.what == IEdk.IEE_Event_t.IEE_UserRemoved.ToInt()){
                if (delegate != null)
                    delegate.userRemoved();
            }
        }
    };

}