package com.projeto.controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.emotiv.emotivcloud.EmotivCloudClient;
import com.emotiv.emotivcloud.EmotivCloudErrorCode;
import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.projeto.util.Emotiv;
import com.projeto.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityLogin extends AppCompatActivity {

    private TimerTask timerTask;
    private Thread threadSave, threadLogin;
    private Timer timer;
    private boolean cloudConnected                 = false;
    private int engineUserID           			   = 0;
    private int  userCloudID                       = -1;
    private Button btSave, btLogin;
    private EditText txtEmotivId, txtPass;
    private TextView txtStatus;
    private final String MY_PROFILE = "my_profile";
    private ProgressDialog myProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Util.initToolbar(this, true, "Login EmotivCloud");

        btLogin = (Button)findViewById(R.id.btLogin);
        txtEmotivId = (EditText)findViewById(R.id.txtEmotivId);
        txtPass = (EditText)findViewById(R.id.txtPass);
        txtStatus = (TextView)findViewById(R.id.txtStatus);

        btSave = (Button)findViewById(R.id.btSave);
        btSave.setEnabled(false);

        getCredentials();
        initListeners();

        if(Emotiv.isConnected())
            txtStatus.setText(getString(R.string.connect_emotiv_success));
        else
            txtStatus.setText(getString(R.string.connect_emotiv));

        Thread threadSaveButton = new Thread(){
            @Override
            public void run() {
                if(EmotivCloudClient.EC_GetUserDetail() != -1){
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         btSave.setEnabled(true);
                     }
                 });
                }
            }
        };
        threadSaveButton.start();
    }

    private void initListeners(){

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                String emotivId = txtEmotivId.getText().toString();
                String password = txtPass.getText().toString();

                if(emotivId.isEmpty()) {
                    txtEmotivId.setError(getString(R.string.error_field_required));
                    txtStatus.setText(getString(R.string.msgLoginWarn));
                    return;
                }

                if(password.isEmpty()) {
                    txtPass.setError(getString(R.string.error_field_required));
                    txtStatus.setText(getString(R.string.msgLoginWarn));
                    return;
                }

                if(Emotiv.isConnected()) {
//                    IEdk.IEE_EngineConnect(ActivityLogin.this,"");
                }else{
                    txtStatus.setText(getString(R.string.connect_emotiv));
                    return;
                }

                // Fecha Teclado
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btLogin.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                showDialog();

                final String EMOTIV_ID = emotivId;
                final String PASSWORD = password;

                threadLogin = new Thread() {
                    @Override
                    public void run() {
                        Log.d(Util.TAG, "Enter Thread Login");
                        if(!cloudConnected) {
                            cloudConnected = (EmotivCloudClient.EC_Connect(ActivityLogin.this) == EmotivCloudErrorCode.EC_OK.ToInt());
                            if(!cloudConnected) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtStatus.setText(getString(R.string.connect_internet));
                                        myProgressDialog.dismiss();
                                    }
                                });
                                return;
                            }
                        }

                        if(EmotivCloudClient.EC_Login(EMOTIV_ID, PASSWORD) == EmotivCloudErrorCode.EC_OK.ToInt()) {
                            userCloudID = EmotivCloudClient.EC_GetUserDetail();
                            saveCredentials(EMOTIV_ID, PASSWORD, userCloudID);
                            if(userCloudID != -1) {
                                // retornar o ID
                                Emotiv.setUserID(IEdk.IEE_EmoEngineEventGetUserId());

                                if(!Emotiv.isConnected()) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtStatus.setText(getString(R.string.connect_emotiv));
                                            myProgressDialog.dismiss();
                                        }
                                    });
                                    return;
                                }
                                int profileID = EmotivCloudClient.EC_GetProfileId(userCloudID, MY_PROFILE);
                                if ( profileID < 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtStatus.setText("Login feito com sucesso, mas perfil não existe. Salve um perfil de treinamento antes.");
                                            myProgressDialog.dismiss();
                                            btSave.setEnabled(true);
                                        }
                                    });
                                    return;
                                }
                                if(EmotivCloudClient.EC_LoadUserProfile(userCloudID, engineUserID, profileID, -1) == EmotivCloudErrorCode.EC_OK.ToInt()) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtStatus.setText("Login e Perfil carregados com sucesso!");
                                            myProgressDialog.dismiss();
                                            btSave.setEnabled(true);
                                        }
                                    });
                                }else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtStatus.setText("Você não pode carregar este perfil. Tente novamente.");
                                            myProgressDialog.dismiss();
                                        }
                                    });
                                }
                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtStatus.setText("Você não pode pegar detalhes deste perfil. Tente novamente.");
                                        myProgressDialog.dismiss();
                                    }
                                });
                            }
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtStatus.setText("EmotivID ou senha incorretos. Tente novamente.");
                                    myProgressDialog.dismiss();
                                }
                            });
                        }
                    }
                };
                threadLogin.start();
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(!Emotiv.isConnected()) {
                    txtStatus.setText(getResources().getString(R.string.connect_emotiv));
                    return;
                }

                if(userCloudID < 0) {
                    txtStatus.setText(getString(R.string.msgLogin));
                    return;
                }

                showDialog();
                threadSave = new Thread() {
                    @Override
                    public void run() {
                        Log.d(Util.TAG, "Enter Thread Save");

                        if(!(EmotivCloudClient.EC_DeleteUserProfile(userCloudID,
                                EmotivCloudClient.EC_GetProfileId(userCloudID, MY_PROFILE))
                                == EmotivCloudErrorCode.EC_OK.ToInt())){
                            Log.d(Util.TAG, "Não foi possível deletar perfil");
                        }else{
                            Log.d(Util.TAG, "Perfil deletado");
                        }

                        // Salva ou sobrescreve um novo perfil
                        if(EmotivCloudClient.EC_SaveUserProfile(userCloudID, engineUserID, MY_PROFILE, EmotivCloudClient.profileFileType.TRAINING.ToInt() ) == EmotivCloudErrorCode.EC_OK.ToInt()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtStatus.setText(getString(R.string.msgPerfil));
                                    myProgressDialog.dismiss();
                                    Log.d(Util.TAG, "Success Save UserCloudID " + userCloudID);
                                }
                            });
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtStatus.setText("Perfil já existe ou não pode criar um novo perfil.");
                                    myProgressDialog.dismiss();
                                    Log.d(Util.TAG, "Error Save UserCloudID " + userCloudID);
                                }
                            });
                        }
                    }
                };
                threadSave.start();
            }
        });
    }

    private void showDialog(){
        myProgressDialog = ProgressDialog
                            .show(ActivityLogin.this,
                            "Aguarde", "Conectando-se com servidor", true, false);
    }

    public void saveCredentials(String login, String pass, int userCloud){
        getPreferences(Context.MODE_PRIVATE).edit()
                .putString("login", login)
                .putString("password", pass)
                .putInt("userCloudID", userCloud)
                .apply();
    }

    public void getCredentials(){
        txtEmotivId.setText(getPreferences(Context.MODE_PRIVATE).getString("login", ""));
        txtPass.setText(getPreferences(Context.MODE_PRIVATE).getString("password", ""));
        userCloudID = getPreferences(Context.MODE_PRIVATE).getInt("userCloudID", -1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        createTimerTask();
        Log.d(Util.TAG, "Login OnResume - TimerInit");
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimertask();
        Log.d(Util.TAG, "Login OnPause - Timer Cancel");
    }

    private void createTimerTask(){
        // Roda um TimerTask com delay inicial 0 a cada 10 milissegundos
        timer = new Timer();
        timer.schedule(initTimerTask(), 0, 10);
    }

    private void stopTimertask(){
        timerTask.cancel();
        timer.cancel();
        timerTask = null;
        timer = null;
    }

    public TimerTask initTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    int state = IEdk.IEE_EngineGetNextEvent();
                    if (state == IEdkErrorCode.EDK_OK.ToInt()) {
                        int eventType = IEdk.IEE_EmoEngineEventGetType();
                        if(eventType == IEdk.IEE_Event_t.IEE_UserRemoved.ToInt()){
                            Log.d(Util.TAG, "User removed Login");
                            Emotiv.setConnected(false);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtStatus.setText(getString(R.string.connect_emotiv));
                                }
                            });
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        return timerTask;
    }
}
