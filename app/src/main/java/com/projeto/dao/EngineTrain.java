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

    private EngineTrainInterface delegate;
    private TimerTask timerTask;
    private Timer timer;
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
                // Recupera o pŕoximo evento de EmoEngine
                // Retorna OK, ERROR ou NO EVENT
                if(Emotiv.isConnected()) {
                    int state = IEdk.IEE_EngineGetNextEvent();
                    if (state == Emotiv.OK) {

                        int typeEvent = IEdk.IEE_EmoEngineEventGetType();

                        if(typeEvent == Emotiv.TYPE_USER_REMOVE) {
                            Log.d(Util.TAG, "Emotiv Desconectado");
                            Emotiv.setConnected(false);
                            Emotiv.clearUserID();
                            handler.sendEmptyMessage(Emotiv.TYPE_USER_REMOVE);

                        }else if (typeEvent == Emotiv.TYPE_EMOSTATE_UPDATE) {
                            // Retorna um EmoState na memória quando houver mudanças
                            IEdk.IEE_EmoEngineEventGetEmoState();
                            handler.sendMessage(handler.obtainMessage(Emotiv.TYPE_EMOSTATE_UPDATE));

                        } else if (typeEvent == Emotiv.TYPE_MENTALCOMMAND) {

                            int typeCommand = MentalCommandDetection.IEE_MentalCommandEventGetType();

                            if (typeCommand == Emotiv.TYPE_TRAIN_STARTED) {
                                Log.d(Util.TAG, "MentalCommand training started");
                                handler.sendEmptyMessage(Emotiv.TYPE_TRAIN_STARTED);

                            } else if (typeCommand == Emotiv.TYPE_TRAIN_SUCCEED) {
                                Log.d(Util.TAG, "MentalCommand training Succeeded");
                                handler.sendEmptyMessage(Emotiv.TYPE_TRAIN_SUCCEED);

                            } else if (typeCommand == Emotiv.TYPE_TRAIN_COMPLETED) {
                                Log.d(Util.TAG, "MentalCommand training Completed");
                                handler.sendEmptyMessage(Emotiv.TYPE_TRAIN_COMPLETED);

                            } else if (typeCommand == Emotiv.TYPE_TRAIN_ERASED) {
                                Log.d(Util.TAG, "MentalCommand training erased");
                                handler.sendEmptyMessage(Emotiv.TYPE_TRAIN_ERASED);

                            } else if (typeCommand == Emotiv.TYPE_TRAIN_FAILED) {
                                Log.d(Util.TAG, "MentalCommand training failed");
                                handler.sendEmptyMessage(Emotiv.TYPE_TRAIN_FAILED);

                            } else if (typeCommand == Emotiv.TYPE_TRAIN_REJECTED) {
                                Log.d(Util.TAG, "MentalCommand training rejected");
                                handler.sendEmptyMessage(Emotiv.TYPE_TRAIN_REJECTED);

                            } else if (typeCommand == Emotiv.TYPE_TRAIN_RESET) {
                                Log.d(Util.TAG, "MentalCommand training Reset");
                                handler.sendEmptyMessage(Emotiv.TYPE_TRAIN_RESET);
                            }
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
            if(delegate != null){
                int type = msg.what;

                if (type == Emotiv.TYPE_EMOSTATE_UPDATE){
                    delegate.currentAction(IEmoStateDLL.IS_MentalCommandGetCurrentAction(),
                                           IEmoStateDLL.IS_MentalCommandGetCurrentActionPower());
                }
                else if (type == Emotiv.TYPE_USER_REMOVE) {
                    delegate.onUserRemoved();
                }
                else if (type == Emotiv.TYPE_TRAIN_STARTED) {
                    delegate.trainStarted();
                }
                else if (type == Emotiv.TYPE_TRAIN_SUCCEED) {
                        delegate.trainSucceed();
                }
                else if (type == Emotiv.TYPE_TRAIN_FAILED) {
                    delegate.trainFailed();
                }
                else if (type == Emotiv.TYPE_TRAIN_COMPLETED) {
                    delegate.trainCompleted();
                }
                else if (type == Emotiv.TYPE_TRAIN_ERASED) {
                    delegate.trainErased();
                }
                else if (type == Emotiv.TYPE_TRAIN_REJECTED) {
                    delegate.trainRejected();
                }
                else if (type == Emotiv.TYPE_TRAIN_RESET) {
                    delegate.trainReset();
                }
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
