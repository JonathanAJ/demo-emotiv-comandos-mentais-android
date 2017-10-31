package com.projeto.controllers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.projeto.adapters.AdapterSpinner;
import com.projeto.adapters.DataSpinner;
import com.projeto.dao.EngineTrain;
import com.projeto.interfaces.EngineTrainInterface;
import com.emotiv.insight.IEmoStateDLL;
import com.emotiv.insight.MentalCommandDetection;
import com.projeto.util.Emotiv;
import com.projeto.util.Util;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentTrain extends Fragment implements EngineTrainInterface {

    private Spinner spinnerAction;
    private Button btnTrain,btnClear;
    private ProgressBar progressBarTime,progressPower;
    private AdapterSpinner spinAdapter;
    private ImageView imgBox;
    private ArrayList<DataSpinner> model = new ArrayList<DataSpinner>();
    private int indexSpinnerAction, _currentAction, count = 0;

    private ActivityMain activityContext;
    private EngineTrain engineTrain;

    private Timer timer;
    private TimerTask timerTask;

    float _currentPower = 0;
    float startLeft     = -1;
    float startRight    = 0;
    float widthScreen   = 0;

    boolean isTrainning = false;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activityContext = (ActivityMain) getActivity();
        rootView = inflater.inflate(R.layout.fragment_treino, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        engineTrain = EngineTrain.shareInstance(this);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        engineTrain.createTimerTask();
        // seta data do spinner
        setDataSpinner();
        Log.d(Util.TAG, "Train OnResume - TimerInit");
    }

    @Override
    public void onPause() {
        super.onPause();
        engineTrain.stopTimertask();
        Log.d(Util.TAG, "Train OnPause - Timer Cancel");
    }

    public void init() {
        btnTrain = (Button) rootView.findViewById(R.id.btstartTraing);
        btnClear = (Button) rootView.findViewById(R.id.btClearData);
        btnClear.setOnClickListener(new View.OnClickListener() {
            /**
             * indexSpinnerAction é o parâmetro que diz em qual
             * posição do Spinner está para limpar o comando treinado
             */
            @Override
            public void onClick(View arg0) {
                switch (indexSpinnerAction) {
                    case 0:
                        engineTrain.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_NEUTRAL);
                        break;
                    case 1:
                        engineTrain.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH);
                        break;
                    case 2:
                        engineTrain.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL);
                        break;
                    case 3:
                        engineTrain.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT);
                        break;
                    case 4:
                        engineTrain.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT);
                        break;
                    default:
                        break;
                }
            }
        });
        progressBarTime = (ProgressBar) rootView.findViewById(R.id.progressBarTime);
        progressBarTime.setVisibility(View.INVISIBLE);
        progressPower = (ProgressBar) rootView.findViewById(R.id.ProgressBarpower);
        imgBox = (ImageView) rootView.findViewById(R.id.imgBox);

        final ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        viewTreeObserver.addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
            @Override
            public void onWindowFocusChanged(final boolean hasFocus) {
                Display display = activityContext.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                widthScreen = size.x;
                if (imgBox != null) {
                    startLeft = imgBox.getLeft();
                    startRight = imgBox.getRight();
                }
            }
        });

        // Spinner com comandos
        spinnerAction = (Spinner) rootView.findViewById(R.id.spinnerAction);
        spinAdapter = new AdapterSpinner(activityContext, R.layout.row, model);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAction.setAdapter(spinAdapter);

        // muda o item do spinner atual
        spinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                indexSpinnerAction = arg2;
            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        btnTrain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!Emotiv.isConnected()) {
                    Toast.makeText(activityContext, getString(R.string.connect_emotiv), Toast.LENGTH_SHORT).show();
                }
                else{
                    switch (indexSpinnerAction) {
                        case 0:
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_NEUTRAL);
                            break;
                        case 1:
                            engineTrain.enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH);
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH);
                            break;
                        case 2:
                            engineTrain.enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL);
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL);
                            break;
                        case 3:
                            engineTrain.enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT);
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT);
                            break;
                        case 4:
                            engineTrain.enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT);
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT);
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        Timer timerListenAction = new Timer();
        timerListenAction.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        handlerUpdateUI.sendEmptyMessage(1);
                    }
                }, 0, 20);
    }

    /**
     * Pega o tempo de treinamento e mostra em uma
     * progressBar
     */
    Handler handlerUpdateUI = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    count ++;
                    int trainningTime = (int) MentalCommandDetection.IEE_MentalCommandGetTrainingTime(Emotiv.getUserID())[1]/1000;
                    if(trainningTime > 0)
                        progressBarTime.setProgress(count / trainningTime);
                    if (progressBarTime.getProgress() >= 100) {
                        timerTask.cancel();
                        timer.cancel();
                    }
                    break;
                case 1:
                    moveImage();
                    break;
                default:
                    break;
            }
        };
    };

    public void startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t MentalCommandAction) {
        isTrainning = engineTrain.startTrainingMentalcommand(isTrainning, MentalCommandAction);
        btnTrain.setText((isTrainning) ? getString(R.string.abort) : getString(R.string.train));
    }

    public void TimerTask() {
        count = 0;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handlerUpdateUI.sendEmptyMessage(0);
            }
        };
    }

    private void moveImage() {
        float power = _currentPower;
        if(isTrainning){
            imgBox.setLeft((int) startLeft);
            imgBox.setRight((int) startRight);
            imgBox.setScaleX(1.0f);
            imgBox.setScaleY(1.0f);
        }
        if(( _currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT.ToInt())  || (_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT.ToInt()) && power > 0) {

            if(imgBox.getScaleX() == 1.0f && startLeft > 0) {
                imgBox.setRight((int) widthScreen);
                power = (_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT.ToInt()) ? power*3 : power*-3;
                imgBox.setLeft((int) (power > 0 ? Math.max(0, (int)(imgBox.getLeft() - power)) : Math.min(widthScreen - imgBox.getMeasuredWidth(), (int)(imgBox.getLeft() - power))));
            }
        }
        else if(imgBox.getLeft() != startLeft && startLeft > 0){
            power = (imgBox.getLeft() > startLeft) ? 6 : -6;
            imgBox.setLeft(power > 0  ? Math.max((int)startLeft, (int)(imgBox.getLeft() - power)) : Math.min((int)startLeft, (int)(imgBox.getLeft() - power)));
        }
        if(((_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL.ToInt()) || (_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH.ToInt())) && power > 0) {
            if(imgBox.getLeft() != startLeft)
                return;
            imgBox.setRight((int) startRight);
            power = (_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH.ToInt()) ? power / 20 : power/-20;
            imgBox.setScaleX((float) (power > 0 ? Math.max(0.1, (imgBox.getScaleX() - power)) : Math.min(2, (imgBox.getScaleX() - power))));
            imgBox.setScaleY((float) (power > 0 ? Math.max(0.1, (imgBox.getScaleY() - power)) : Math.min(2, (imgBox.getScaleY() - power))));
        }
        else if(imgBox.getScaleX() != 1.0f){
            power = (imgBox.getScaleX() < 1.0f) ? 0.03f : -0.03f;
            imgBox.setScaleX((float) (power > 0 ? Math.min(1, (imgBox.getScaleX() + power)) : Math.max(1, (imgBox.getScaleX() + power))));
            imgBox.setScaleY((float) (power > 0 ? Math.min(1, (imgBox.getScaleY() + power)) : Math.max(1, (imgBox.getScaleY() + power))));
        }
    }

    public void enableClick() {
        btnClear.setClickable(true);
        spinnerAction.setClickable(true);
    }

    @Override
    public void currentAction(int typeAction, float power) {
        progressPower.setProgress((int)(power * 100));
        _currentAction = typeAction;
        _currentPower  = power;
    }

    @Override
    public void userAdd(int userId) {

    }

    @Override
    public void userRemoved() {

    }

    @Override
    public void trainStarted() {
        progressBarTime.setProgress(0);
        progressBarTime.setVisibility(View.VISIBLE);
        btnClear.setClickable(false);
        spinnerAction.setClickable(false);
        timer = new Timer();
        TimerTask();
        timer.schedule(timerTask , 0, 10);
    }

    @Override
    public void trainSucceed() {
        progressBarTime.setVisibility(View.INVISIBLE);
        btnTrain.setText(getString(R.string.train));
        enableClick();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activityContext);
        // set title
        alertDialogBuilder.setTitle(getString(R.string.success));
        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.msgTrainSuccess))
                .setCancelable(false)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("Sim",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                engineTrain.setTrainControl(Emotiv.COMMAND_ACCEPT);
                            }
                        })
                .setNegativeButton("Não",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                engineTrain.setTrainControl(Emotiv.COMMAND_REJECT);
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public  void trainFailed(){
        progressBarTime.setVisibility(View.INVISIBLE);
        btnTrain.setText(getString(R.string.train));
        enableClick();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activityContext);
        // set title
        alertDialogBuilder.setTitle(getString(R.string.error));
        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.msgTrainNoise))
                .setCancelable(false)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int which) {

                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        isTrainning = false;
    }

    @Override
    public void trainCompleted() {
        DataSpinner data = model.get(indexSpinnerAction);
        data.setChecked(true);
        model.set(indexSpinnerAction, data);
        spinAdapter.notifyDataSetChanged();
        isTrainning = false;
    }

    @Override
    public void trainRejected() {
        DataSpinner data=model.get(indexSpinnerAction);
        data.setChecked(false);
        model.set(indexSpinnerAction, data);
        spinAdapter.notifyDataSetChanged();
        enableClick();
        isTrainning = false;
    }

    @Override
    public void trainErased() {
        DataSpinner data = model.get(indexSpinnerAction);
        data.setChecked(false);
        model.set(indexSpinnerAction, data);
        spinAdapter.notifyDataSetChanged();
        enableClick();
        isTrainning = false;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activityContext);
        // set title
        alertDialogBuilder.setTitle("Apagado");
        // set dialog message
        alertDialogBuilder
                .setMessage("Treinamento - " + data.getTvName() + " - apagado com sucesso!")
                .setCancelable(false)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void trainReset() {
        if(timer != null){
            timer.cancel();
            timerTask.cancel();
        }
        isTrainning = false;
        progressBarTime.setVisibility(View.INVISIBLE);
        progressBarTime.setProgress(0);
        enableClick();
    }

    public void setDataSpinner() {
        Log.d(Util.TAG, "Enter setDataSpinner Train");
        /*
         * Seta o Spinner com os respectivos nomes e se já foram treinados
         */
        model.clear();
        DataSpinner data = new DataSpinner();
        data.setTvName("Neutro");
        data.setChecked(engineTrain.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_NEUTRAL.ToInt()));
        model.add(data);

        data = new DataSpinner();
        data.setTvName("Empurrar");
        data.setChecked(engineTrain.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH.ToInt()));
        model.add(data);

        data = new DataSpinner();
        data.setTvName("Puxar");
        data.setChecked(engineTrain.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL.ToInt()));
        model.add(data);

        data = new DataSpinner();
        data.setTvName("Esquerda");
        data.setChecked(engineTrain.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT.ToInt()));
        model.add(data);

        data = new DataSpinner();
        data.setTvName("Direita");
        data.setChecked(engineTrain.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT.ToInt()));
        model.add(data);

        spinAdapter.notifyDataSetChanged();
    }
}