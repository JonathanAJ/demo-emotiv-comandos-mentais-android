package com.emotiv.dao;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.emotiv.insight.IEmoStateDLL;
import com.emotiv.insight.MentalCommandDetection;
import com.emotiv.interfaces.EngineTrainInterface;
import com.emotiv.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class EngineTrain {

    /* Config */
    public static final int TYPE_USER_ADD = 16;
    public static final int TYPE_USER_REMOVE = 32;
    public static final int TYPE_EMOSTATE_UPDATE = 64;
    public static final int TYPE_METACOMMAND_EVENT = 256;

    /* Train */
    public static final int HANDLER_TRAIN_STARTED = 1;
    public static final int HANDLER_TRAIN_SUCCEED = 2;
    public static final int HANDLER_TRAIN_FAILED = 3;
    public static final int HANDLER_TRAIN_COMPLETED = 4;
    public static final int HANDLER_TRAIN_ERASED = 5;
    public static final int HANDLER_TRAIN_REJECTED = 6;
    public static final int HANDLER_ACTION_CURRENT = 7;
    public static final int HANDLER_USER_ADD = 8;
    public static final int HANDLER_USER_REMOVE = 9;
    public static final int HANDLER_TRAINED_RESET = 10;

    private EngineTrainInterface delegate;
    private boolean isConnected = false;
    private TimerTask timerTask;
    private int userId = -1;
    private int state;
    private static EngineTrain engineInstance = null;

    public static EngineTrain shareInstance(EngineTrainInterface delegate) {
        if (engineInstance == null) {
            engineInstance = new EngineTrain(delegate);
        }
        return engineInstance;
    }

    private EngineTrain(EngineTrainInterface delegate){
        timerTask();
        Timer timer = new Timer();
        // Roda um TimerTask com delay inicial 0 a cada 10 milissegundos
        timer.schedule(timerTask, 0, 10);
        this.delegate = delegate;
        Log.d(Util.TAG, "EngineTrain");
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
                        Log.d(Util.TAG, "Conectando-se...");
                        if (!isConnected) {
                            // Conecta Epoc+, posicao 0 na lista, modo de configuração falso
                            IEdk.IEE_ConnectEpocPlusDevice(0, false);
                        }
                    }
                    // Recupera o pŕoximo evento de EmoEngine
                    // Retorna OK, ERROR ou NO EVENT
                    state = IEdk.IEE_EngineGetNextEvent();

                    if (state == IEdkErrorCode.EDK_OK.ToInt()) {
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
                        switch (typeEvent) {
                            //Um Emotiv está conectado.
                            case TYPE_USER_ADD:
                                Log.d(Util.TAG, "Emotiv Conectado");
                                isConnected = true;
                                // Retorna o ID do usuário nos eventos IEE_UserAdded e IEE_UserRemoved.
                                userId = IEdk.IEE_EmoEngineEventGetUserId();
                                handler.sendEmptyMessage(HANDLER_USER_ADD);
                                break;
                            //Um Emotiv está desconectado.
                            case TYPE_USER_REMOVE:
                                Log.d(Util.TAG, "Emotiv Desconectado");
                                isConnected = false;
                                userId = -1;
                                handler.sendEmptyMessage(HANDLER_USER_REMOVE);
                                break;
                            // Os resultados de detecção foram atualizados da EmoEngine.
                            case TYPE_EMOSTATE_UPDATE:
                                if (!isConnected)
                                    break;
                                // Retorna um EmoState na memória
                                IEdk.IEE_EmoEngineEventGetEmoState();
                                handler.sendMessage(handler.obtainMessage(HANDLER_ACTION_CURRENT));
                                break;

                            case TYPE_METACOMMAND_EVENT:
                                int type = MentalCommandDetection.IEE_MentalCommandEventGetType();

                                if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingStarted
                                        .getType()) {
                                    Log.d(Util.TAG, "MentalCommand training started");
                                    handler.sendEmptyMessage(HANDLER_TRAIN_STARTED);

                                } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingSucceeded
                                        .getType()) {
                                    Log.d(Util.TAG, "MentalCommand training Succeeded");
                                    handler.sendEmptyMessage(HANDLER_TRAIN_SUCCEED);

                                } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingCompleted
                                        .getType()) {
                                    Log.d(Util.TAG, "MentalCommand training Completed");
                                    handler.sendEmptyMessage(HANDLER_TRAIN_COMPLETED);

                                } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingDataErased
                                        .getType()) {
                                    Log.d(Util.TAG, "MentalCommand training erased");
                                    handler.sendEmptyMessage(HANDLER_TRAIN_ERASED);

                                } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingFailed
                                        .getType()) {
                                    Log.d(Util.TAG, "MentalCommand training failed");
                                    handler.sendEmptyMessage(HANDLER_TRAIN_FAILED);

                                } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingRejected
                                        .getType()) {
                                    Log.d(Util.TAG, "MentalCommand training rejected");
                                    handler.sendEmptyMessage(HANDLER_TRAIN_REJECTED);

                                } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingReset
                                        .getType()) {
                                    Log.d(Util.TAG, "MentalCommand training Reset");
                                    handler.sendEmptyMessage(HANDLER_TRAINED_RESET);

                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            };
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_USER_ADD:
                    if (delegate != null)
                        delegate.userAdd(userId);
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
                case HANDLER_TRAINED_RESET:
                    if (delegate != null)
                        delegate.trainReset();
                    break;
                default:
                    break;
            }
        }
    };

    public boolean isConnected() {
        return isConnected;
    }

    public void enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t _MentalcommandAction) {
        long MentaCommandActions;
        long[] activeAction = MentalCommandDetection.IEE_MentalCommandGetActiveActions(userId);
        if (activeAction[0] == IEdkErrorCode.EDK_OK.ToInt()) {
            long y = activeAction[1] & (long) _MentalcommandAction.ToInt(); // and
            if (y == 0) {
                MentaCommandActions = activeAction[1]| ((long) _MentalcommandAction.ToInt()); // or
                MentalCommandDetection.IEE_MentalCommandSetActiveActions(userId, MentaCommandActions);
            }
        }
    }

    public boolean checkTrained(int action) {
        // Verifica se o comando já foi treinado
        long[] result = MentalCommandDetection.IEE_MentalCommandGetTrainedSignatureActions(userId);
        if (result[0] == IEdkErrorCode.EDK_OK.ToInt()) {
            long y = result[1] & action;
            return (y == action);
        }
        return false;
    }

    public void trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t MentalcommandAction) {
        // primeiramente seta qual comando você está "treinando"
        MentalCommandDetection.IEE_MentalCommandSetTrainingAction(userId, MentalcommandAction.ToInt());
        // depois apaga esse comando e verifica se deu tudo certo
        if (MentalCommandDetection.IEE_MentalCommandSetTrainingControl(userId,
                MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_ERASE.getType()) == IEdkErrorCode.EDK_OK.ToInt()) {
            Log.d(Util.TAG, "clear " + MentalcommandAction.name() + " sucesso");
        }else{
            Log.d(Util.TAG, "clear " + MentalcommandAction.name() + " erro ");
        }
    }

    public boolean startTrainingMentalcommand(Boolean isTrain, IEmoStateDLL.IEE_MentalCommandAction_t MentalCommandAction) {
        if (!isTrain) {
            if (MentalCommandDetection.IEE_MentalCommandSetTrainingAction(userId,
                    MentalCommandAction.ToInt()) == IEdkErrorCode.EDK_OK.ToInt()) {
                if (MentalCommandDetection.IEE_MentalCommandSetTrainingControl(userId,
                        MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_START.getType()) == IEdkErrorCode.EDK_OK
                        .ToInt()) {
                    Log.d(Util.TAG, "treino de " + MentalCommandAction.name() + " sucesso");
                    return true;
                }
            }
        } else {
            if (MentalCommandDetection.IEE_MentalCommandSetTrainingControl(userId,
                    MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_RESET.getType()) == IEdkErrorCode.EDK_OK
                    .ToInt()) {
                return false;
            }
        }
        return false;
    }

    public void setTrainControl(int type) {
        if (MentalCommandDetection.IEE_MentalCommandSetTrainingControl(userId, type) == IEdkErrorCode.EDK_OK
                .ToInt()) {
        }
    }
}
