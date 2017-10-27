package com.projeto.controllers;

import android.app.Activity;
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
    private boolean cloudConnected                 = false;
    private int engineUserID           			   = 0;
    private int  userCloudID                       = -1;
    private Button btSave, btLogin;
    private EditText txtEmotivId, txtPass;
    private TextView txtStatus;
    private final String MY_PROFILE = "my_profile";

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

        getCredentials();
        initListeners();

        if(Emotiv.isConnected())
            txtStatus.setText(getString(R.string.connect_emotiv_success));
        else
            txtStatus.setText(getString(R.string.connect_emotiv));
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

                saveCredentials(emotivId, password);

                if(Emotiv.isConnected()) {
                    IEdk.IEE_EngineConnect(ActivityLogin.this,"");
                }else{
                    txtStatus.setText(getString(R.string.connect_emotiv));
                    return;
                }

                if(!cloudConnected) {
                    cloudConnected = (EmotivCloudClient.EC_Connect(ActivityLogin.this) == EmotivCloudErrorCode.EC_OK.ToInt());
                    if(!cloudConnected) {
                        txtStatus.setText(getString(R.string.connect_internet));
                        return;
                    }
                }

                // Fecha Teclado
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btLogin.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                if(EmotivCloudClient.EC_Login(emotivId, password) == EmotivCloudErrorCode.EC_OK.ToInt()) {
                    txtStatus.setText(getString(R.string.msgLoginSuccess));
                    userCloudID = EmotivCloudClient.EC_GetUserDetail();
                    if(userCloudID != -1) {
                        if(!Emotiv.isConnected()) {
                            txtStatus.setText(getString(R.string.connect_emotiv));
                            return;
                        }
                        int profileID = EmotivCloudClient.EC_GetProfileId(userCloudID, MY_PROFILE);
                        if ( profileID < 0) {
                            txtStatus.setText("Login feito com sucesso, mas perfil não existe. Salve um perfil de treinamento antes.");
                            return;
                        }
                        if(EmotivCloudClient.EC_LoadUserProfile(userCloudID, engineUserID, profileID, -1) == EmotivCloudErrorCode.EC_OK.ToInt()) {
                            txtStatus.setText("Perfil carregado com sucesso!");
                        }
                        else {
                            txtStatus.setText("Você não pode carregar este perfil. Tente novamente.");
                        }
                    }
                    else {
                        txtStatus.setText("Você não pode pegar detalhes deste perfil. Tente novamente.");
                    }
                }
                else {
                    txtStatus.setText("EmotivID ou senha incorretos. Tente novamente.");
                }
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

                // Verifica se há um perfil
                int profileID = EmotivCloudClient.EC_GetProfileId(userCloudID, MY_PROFILE);
                // Se houver, deleta o perfil existente
                if ( profileID > 0) {
                    if(!(EmotivCloudClient.EC_DeleteUserProfile(userCloudID, profileID) == EmotivCloudErrorCode.EC_OK.ToInt())){
                        Log.d("TAG", "Não foi possível deletar perfil");
                    }
                }

                // Salva ou sobrescreve um novo perfil
                if(EmotivCloudClient.EC_SaveUserProfile(userCloudID, engineUserID, MY_PROFILE, EmotivCloudClient.profileFileType.TRAINING.ToInt() ) == EmotivCloudErrorCode.EC_OK.ToInt()) {
                    txtStatus.setText(getString(R.string.msgPerfil));
                }else {
                    txtStatus.setText(getString(R.string.connect_internet));
                }
            }
        });
    }

    public void saveCredentials(String login, String pass){
        getPreferences(Context.MODE_PRIVATE).edit()
                .putString("login", login)
                .putString("password", pass)
                .apply();
    }

    public void getCredentials(){
        txtEmotivId.setText(getPreferences(Context.MODE_PRIVATE).getString("login", ""));
        txtPass.setText(getPreferences(Context.MODE_PRIVATE).getString("password", ""));
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
        Timer timer = new Timer();
        timer.schedule(initTimerTask(), 0, 10);
        Log.d(Util.TAG, "Login OnResume - TimerInit");
    }

    @Override
    public void onPause() {
        super.onPause();
        timerTask.cancel();
        Log.d(Util.TAG, "Login OnPause - Timer Cancel");
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
