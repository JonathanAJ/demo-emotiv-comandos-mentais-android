package com.projeto.controllers;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.emotiv.emotivcloud.EmotivCloudClient;
import com.emotiv.emotivcloud.EmotivCloudErrorCode;
import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;

public class ActivityLogin extends AppCompatActivity {

    private Thread processingThread;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean lock = false;
    int userId;
    boolean cloudConnected                 = false;
    boolean headsetConnected               = false;
    int engineUserID           			   = 0;
    int  userCloudID                       = -1;
    Button btSave, btLoad, btLogin;
    EditText txtEmotivId, txtPass;
    TextView txtStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /***Android 6.0 and higher need to request permission*****/
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH);
            }
            else{
                checkConnect();
            }
        }
        else {
            checkConnect();
        }

        btSave = (Button)findViewById(R.id.btSave);
        btLoad = (Button)findViewById(R.id.btLoad);
        btLogin = (Button)findViewById(R.id.btLogin);
        txtEmotivId = (EditText)findViewById(R.id.txtEmotivId);
        txtPass = (EditText)findViewById(R.id.txtPass);
        txtStatus = (TextView)findViewById(R.id.txtStatus);

        btSave.setEnabled(false);
        btLoad.setEnabled(false);

        int n = EmotivCloudClient.EC_GetAllProfileName(22732);//myprofiel
        boolean n2 = (EmotivCloudClient.EC_Connect(ActivityLogin.this) == EmotivCloudErrorCode.EC_OK.ToInt());

        btSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(!headsetConnected) {
                    txtStatus.setText("Connect headset first");
                    return;
                }
                if(userCloudID < 0) {
                    txtStatus.setText("Login first");
                    return;
                }

                if(EmotivCloudClient.EC_SaveUserProfile(userCloudID, engineUserID, "test", EmotivCloudClient.profileFileType.TRAINING.ToInt() ) == EmotivCloudErrorCode.EC_OK.ToInt()) {
                    txtStatus.setText("Save new profile successfully");

                    int n = EmotivCloudClient.EC_GetAllProfileName(22732);//myprofiel
                    System.out.println(n);
                }
                else {
                    txtStatus.setText("Profile is existed or can't create new profile");
                }

            }
        });
        btLoad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(!headsetConnected) {
                    txtStatus.setText("Connect headset first");
                    return;
                }
                if(userCloudID < 0) {
                    txtStatus.setText("Login first");
                    return;
                }
                int profileID = EmotivCloudClient.EC_GetProfileId(userCloudID, "test");
                if ( profileID < 0) {
                    txtStatus.setText("Profile isnt existed");
                    return;
                }
                if(EmotivCloudClient.EC_LoadUserProfile(userCloudID, engineUserID, profileID,-1) == EmotivCloudErrorCode.EC_OK.ToInt()) {
                    txtStatus.setText("Load profile successfully");
                    int n = EmotivCloudClient.EC_GetAllProfileName(22732);//myprofiel
                    System.out.println(n);
                }
                else {
                    txtStatus.setText("Cant load this profile");
                }

            }
        });
        btLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(!cloudConnected) {
                    cloudConnected = (EmotivCloudClient.EC_Connect(ActivityLogin.this) == EmotivCloudErrorCode.EC_OK.ToInt());
                    if(!cloudConnected) {
                        txtStatus.setText("Please check internet connection and connect again");
                        return;
                    }
                }
                if(txtEmotivId.getTextSize() == 0 || txtPass.getTextSize() == 0) {
                    txtStatus.setText("Enter username and password");
                    return;
                }
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btLogin.getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
                if(EmotivCloudClient.EC_Login(txtEmotivId.getText().toString(), txtPass.getText().toString()) == EmotivCloudErrorCode.EC_OK.ToInt()) {
                    txtStatus.setText("Login successfully");
                    userCloudID = EmotivCloudClient.EC_GetUserDetail();
                    if(EmotivCloudClient.EC_GetUserDetail() != -1) {
                        btSave.setEnabled(true);
                        btLoad.setEnabled(true);
                    }
                    else {
                        txtStatus.setText("Cant get user detail. Please try again");
                    }
                }
                else {
                    txtStatus.setText("Username or password is wrong. Check again");
                }
            }
        });

        processingThread=new Thread()
        {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                super.run();
                while(true)
                {
                    try
                    {
                        handler.sendEmptyMessage(0);
                        handler.sendEmptyMessage(1);
                        Thread.sleep(5);
                    }

                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        };
        processingThread.start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 0:
                    int state = IEdk.IEE_EngineGetNextEvent();
                    if (state == IEdkErrorCode.EDK_OK.ToInt()) {
                        int eventType = IEdk.IEE_EmoEngineEventGetType();
                        userId = IEdk.IEE_EmoEngineEventGetUserId();
                        if(eventType == IEdk.IEE_Event_t.IEE_UserAdded.ToInt()){
                            Log.e("SDK","User added");
                            headsetConnected = true;
                        }
                        if(eventType == IEdk.IEE_Event_t.IEE_UserRemoved.ToInt()){
                            Log.e("SDK","User removed");
                            headsetConnected = false;
                        }
                    }

                    break;
                case 1:
					/*Connect device with Insight headset*/
                    /*************************************/
					/*Connect device with Epoc Plus headset*/
                    int number = IEdk.IEE_GetEpocPlusDeviceCount();
                    if(number != 0) {
                        if(!lock){
                            lock = true;
                            IEdk.IEE_ConnectEpocPlusDevice(0,false);
                        }
                    }
                    /*************************************/
                    else lock = false;
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BLUETOOTH: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkConnect();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "App can't run without this permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK){
                //Connect to emoEngine
                IEdk.IEE_EngineConnect(this,"");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "You must be turn on bluetooth to connect with Emotiv devices"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void checkConnect(){
        if (!mBluetoothAdapter.isEnabled()) {
            /****Request turn on Bluetooth***************/
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            //Connect to emoEngine
            IEdk.IEE_EngineConnect(this,"");
        }
    }
}
