package com.projeto.dao;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.emotiv.insight.IEmoStateDLL;
import com.emotiv.insight.MentalCommandDetection;
import com.projeto.interfaces.EngineTrainInterface;
import com.projeto.util.Emotiv;
import com.projeto.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class EngineTrain {

    /* Handlers for Train */
    private static final int HANDLER_TRAIN_STARTED = 1;
    private static final int HANDLER_TRAIN_SUCCEED = 2;
    private static final int HANDLER_TRAIN_FAILED = 3;
    private static final int HANDLER_TRAIN_COMPLETED = 4;
    private static final int HANDLER_TRAIN_ERASED = 5;
    private static final int HANDLER_TRAIN_REJECTED = 6;
    private static final int HANDLER_ACTION_CURRENT = 7;
    private static final int HANDLER_USER_ADD = 8;
    private static final int HANDLER_USER_REMOVE = 9;
    private static final int HANDLER_TRAIN_RESET = 10;

    private EngineTrainInterface delegate;
    private TimerTask timerTask;
    private Timer timer;
    private int state;
    private static EngineTrain engineInstance = null;

    public static EngineTrain shareInstance(EngineTrainInterface delegate) {
        if (engineInstance == null) {
            engineInstance = new EngineTrain(delegate);
        }
        return engineInstance;
    }

    private EngineTrain(EngineTrainInterface delegate){
        this.delegate = delegate;
        Log.d(Util.TAG, "EngineTrain");
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
                    for (int value: values) {
                        if (value == IEmoStateDLL.IEE_EEG_ContactQuality_t.IEEG_CQ_GOOD.ordinal())
                            numGood++;
                    }
                    // calcula a porcentagem da qualidade boa de sinal
                    double resNumGood = ((double) numGood / (double) values.length) * 100.0;
                    Log.d(Util.TAG, "Qualidade: " + resNumGood + "%");

                    int typeEvent = IEdk.IEE_EmoEngineEventGetType();

                    if(typeEvent == Emotiv.TYPE_USER_ADD){
                        Log.d(Util.TAG, "Emotiv Conectado");
                        Emotiv.setConnected(true);
                        // Retorna o ID do usuário nos eventos IEE_UserAdded e IEE_UserRemoved.
                        Emotiv.setUserID(IEdk.IEE_EmoEngineEventGetUserId());
                        handler.sendEmptyMessage(HANDLER_USER_ADD);
                    }
                    else if(typeEvent == Emotiv.TYPE_USER_REMOVE){
                        Log.d(Util.TAG, "Emotiv Desconectado");
                        Emotiv.setConnected(false);
                        Emotiv.clearUserID();
                        handler.sendEmptyMessage(HANDLER_USER_REMOVE);
                    }
                    else if(typeEvent == Emotiv.TYPE_EMOSTATE_UPDATE){
                        if (!Emotiv.isConnected())
                            return;
                        // Retorna um EmoState na memória
                        IEdk.IEE_EmoEngineEventGetEmoState();
                        handler.sendMessage(handler.obtainMessage(HANDLER_ACTION_CURRENT));
                    }
                    else if(typeEvent == Emotiv.TYPE_MENTALCOMMAND){

                        int typeCommand = MentalCommandDetection.IEE_MentalCommandEventGetType();

                        if (typeCommand == Emotiv.TYPE_TRAIN_STARTED) {
                            Log.d(Util.TAG, "MentalCommand training started");
                            handler.sendEmptyMessage(HANDLER_TRAIN_STARTED);

                        } else if (typeCommand == Emotiv.TYPE_TRAIN_SUCCEED) {
                            Log.d(Util.TAG, "MentalCommand training Succeeded");
                            handler.sendEmptyMessage(HANDLER_TRAIN_SUCCEED);

                        } else if (typeCommand == Emotiv.TYPE_TRAIN_COMPLETED) {
                            Log.d(Util.TAG, "MentalCommand training Completed");
                            handler.sendEmptyMessage(HANDLER_TRAIN_COMPLETED);

                        } else if (typeCommand == Emotiv.TYPE_TRAIN_ERASED) {
                            Log.d(Util.TAG, "MentalCommand training erased");
                            handler.sendEmptyMessage(HANDLER_TRAIN_ERASED);

                        } else if (typeCommand == Emotiv.TYPE_TRAIN_FAILED) {
                            Log.d(Util.TAG, "MentalCommand training failed");
                            handler.sendEmptyMessage(HANDLER_TRAIN_FAILED);

                        } else if (typeCommand == Emotiv.TYPE_TRAIN_REJECTED) {
                            Log.d(Util.TAG, "MentalCommand training rejected");
                            handler.sendEmptyMessage(HANDLER_TRAIN_REJECTED);

                        } else if (typeCommand == Emotiv.TYPE_TRAIN_RESET) {
                            Log.d(Util.TAG, "MentalCommand training Reset");
                            handler.sendEmptyMessage(HANDLER_TRAIN_RESET);
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
            switch (msg.what) {
                case HANDLER_USER_ADD:
                    if (delegate != null)
                        delegate.userAdd(Emotiv.getUserID());
                    break;
                case HANDLER_USER_REMOVE:
                    if (delegate != null)
                        delegate.userRemoved();
                    break;
                case HANDLER_ACTION_CURRENT:
                    if (delegate != null) {
                        delegate.currentAction(
                                IEmoStateDLL.IS_MentalCommandGetCurrentAction(),
                                IEmoStateDLL.IS_MentalCommandGetCurrentActionPower());
                    }
                    break;
                case HANDLER_TRAIN_STARTED:
                    if (delegate != null)
                        delegate.trainStarted();
                    break;
                case HANDLER_TRAIN_SUCCEED:
                    if (delegate != null)
                        delegate.trainSucceed();
                    break;
                case HANDLER_TRAIN_FAILED:
                    if(delegate != null)
                        delegate.trainFailed();
                    break;
                case HANDLER_TRAIN_COMPLETED:
                    if (delegate != null)
                        delegate.trainCompleted();
                    break;
                case HANDLER_TRAIN_ERASED:
                    if (delegate != null)
                        delegate.trainErased();
                    break;
                case HANDLER_TRAIN_REJECTED:
                    if (delegate != null)
                        delegate.trainRejected();
                    break;
                case HANDLER_TRAIN_RESET:
                    if (delegate != null)
                        delegate.trainReset();
                    break;
                default:
                    break;
            }
        }
    };

    public void enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t _MentalcommandAction) {
        long MentaCommandActions;
        long[] activeAction = MentalCommandDetection.IEE_MentalCommandGetActiveActions(Emotiv.getUserID());
        if (activeAction[0] == Emotiv.OK) {
            long y = activeAction[1] & (long) _MentalcommandAction.ToInt(); // and
            if (y == 0) {
                MentaCommandActions = activeAction[1]| ((long) _MentalcommandAction.ToInt()); // or
                MentalCommandDetection.IEE_MentalCommandSetActiveActions(Emotiv.getUserID(), MentaCommandActions);
            }
        }
    }

    public boolean checkTrained(int action) {
        // Verifica se o comando já foi treinado
        long[] result = MentalCommandDetection.IEE_MentalCommandGetTrainedSignatureActions(Emotiv.getUserID());
        if (result[0] == Emotiv.OK) {
            long y = result[1] & action;
            return (y == action);
        }
        return false;
    }

    public void trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t MentalcommandAction) {
        // primeiramente seta qual comando você está "treinando"
        MentalCommandDetection.IEE_MentalCommandSetTrainingAction(Emotiv.getUserID(), MentalcommandAction.ToInt());
        // depois apaga esse comando e verifica se deu tudo certo
        if (setTrainControl(Emotiv.COMMAND_ERASE)) {
            Log.d(Util.TAG, "clear " + MentalcommandAction.name() + " sucesso");
        }else{
            Log.d(Util.TAG, "clear " + MentalcommandAction.name() + " erro ");
        }
    }

    public boolean startTrainingMentalcommand(Boolean isTrain, IEmoStateDLL.IEE_MentalCommandAction_t MentalCommandAction) {
        if (!isTrain) {
            if (MentalCommandDetection.IEE_MentalCommandSetTrainingAction(Emotiv.getUserID(),
                    MentalCommandAction.ToInt()) == Emotiv.OK) {
                if (setTrainControl(Emotiv.COMMAND_START)) {
                    Log.d(Util.TAG, "Treino de " + MentalCommandAction.name() + " sucesso");
                    return true;
                }
            }
        } else {
            if (setTrainControl(Emotiv.COMMAND_RESET)){
                return false;
            }
        }
        return false;
    }

    public boolean setTrainControl(int type) {
        return (MentalCommandDetection.IEE_MentalCommandSetTrainingControl(Emotiv.getUserID(), type) == Emotiv.OK);
    }
}
