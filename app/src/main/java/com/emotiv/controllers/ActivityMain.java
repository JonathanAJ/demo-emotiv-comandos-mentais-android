package com.emotiv.controllers;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.emotiv.dao.EngineConfig;
import com.emotiv.dao.EngineConnector;
import com.emotiv.interfaces.EngineConfigInterface;
import com.emotiv.util.Util;

public class ActivityMain extends AppCompatActivity implements EngineConfigInterface{

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 0;
    private BluetoothAdapter mBluetoothAdapter;

    private EngineConnector engineConnector;
    private EngineConfig engineConfig;

    private Toolbar toolbar;

    private BottomNavigationView bottomNavigationView;
    private CoordinatorLayout coordinatorLayout;

    private Fragment fragmentStatus;
    private Fragment fragmentTrain;
    private Fragment fragmentGame;

    public EngineConnector getEngineConnector() {
        return engineConnector;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = Util.initToolbar(this, false, "Comando Mental");
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			/*Android 6.0 and higher need to request permission*****/
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        MY_PERMISSIONS_REQUEST_BLUETOOTH);
            }else{
                checkConnect();
            }
        }
        else {
            checkConnect();
        }

        fragmentStatus = new FragmentStatus();
        fragmentTrain = new FragmentTrain();
        fragmentGame = new FragmentGame();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.navigation_status){
                    initFragments(fragmentStatus, "status");
                }else if (id == R.id.navigation_treino){
                    initFragments(fragmentTrain, "train");
                }else if (id == R.id.navigation_jogo){
                    initFragments(fragmentGame, "game");
                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.navigation_status);
    }

    private void checkConnect(){
        if (!mBluetoothAdapter.isEnabled()) {
            // Requisita bluethoot ao usuário
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(this, "Você deve ligar o Bluetooth e a Localização para conectar com Emotiv", Toast.LENGTH_SHORT).show();
        }else {
            /*
             * EngineConnector é a classe que controla
             * e se comunica com o Emotiv. Iniciando conexão.
             */
            engineConnector = EngineConnector.shareInstance(this);
            Log.d(Util.TAG, "EngineConnector");
            /*
             * EngineConfig é a classe responsável por
             * verificar alterações globais do Emotiv.
             */
            engineConfig = EngineConfig.shareInstance(this);
        }
    }

    private void initFragments(Fragment fragment, String tag){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.viewFragment, fragment, tag)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BLUETOOTH: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissão concedida.", Toast.LENGTH_SHORT).show();
                    checkConnect();
                } else {
                    Toast.makeText(this, "App não funcionará sem essa permissão.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void userAdd(int userId) {
        Snackbar mySnackbar = Snackbar.make(coordinatorLayout, "Emotiv conectado.",
                Snackbar.LENGTH_LONG);
        TextView textView = (TextView) mySnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        mySnackbar.show();
    }

    @Override
    public void userRemoved() {
        Snackbar mySnackbar = Snackbar.make(coordinatorLayout, "Emotiv desconectado.",
                Snackbar.LENGTH_LONG);
        TextView textView = (TextView) mySnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        mySnackbar.show();
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK){
                Snackbar mySnackbar = Snackbar.make(coordinatorLayout, "Bluetooth ligado. Verifique se a Localização está ligada.",
                                        Snackbar.LENGTH_LONG);
                TextView textView = (TextView) mySnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                mySnackbar.show();
            }
            checkConnect();
        }
    }
}
