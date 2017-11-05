package com.projeto.controllers;

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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.projeto.dao.EngineConnector;
import com.projeto.interfaces.EngineConnectorInterface;
import com.projeto.util.Emotiv;
import com.projeto.util.Util;

public class ActivityMain extends AppCompatActivity implements EngineConnectorInterface {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 0;
    private BluetoothAdapter mBluetoothAdapter;

    private EngineConnector engineConnector;
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
                    toolbar.setVisibility(View.VISIBLE);
                    initFragments(fragmentStatus, "txtStatus");
                }else if (id == R.id.navigation_treino){
                    toolbar.setVisibility(View.VISIBLE);
                    initFragments(fragmentTrain, "train");
                }else if (id == R.id.navigation_jogo){
                    toolbar.setVisibility(View.GONE);
                    initFragments(fragmentGame, "game");
                }
                return true;
            }
        });
//        bottomNavigationView.setSelectedItemId(R.id.navigation_status);
        bottomNavigationView.setSelectedItemId(R.id.navigation_jogo);
    }

    private void checkConnect(){
        if (!mBluetoothAdapter.isEnabled()) {
            // Requisita bluethoot ao usuário
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else {
            /*
             * EngineConnector é a classe que controla e se comunica com o Emotiv.
             * Iniciando conexão e verificando se há um Emotiv conectado.
             */
            engineConnector = EngineConnector.shareInstance(this);
            Log.d(Util.TAG, "EngineConnector");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Util.TAG, "Activity OnResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Util.TAG, "Activity OnPause");
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
                    showMessageSnackbar(R.string.permission_success);
                    checkConnect();
                } else {
                    showMessageSnackbar(R.string.permission_fail);
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idMenu = item.getItemId();
        switch (idMenu){
            case R.id.menuLogin : {
                if(Emotiv.isConnected())
                    Util.mudaTela(this, ActivityLogin.class);
                else
                    showMessageSnackbar(R.string.connect_emotiv);
                break;
            }
        }
        return true;
    }

    @Override
    public void onUserAdd() {
        showMessageSnackbar(R.string.connect_emotiv_success);
    }

    public void showMessageSnackbar(int res){
        Snackbar mySnackbar = Snackbar.make(coordinatorLayout, getString(res), Snackbar.LENGTH_LONG);
        TextView textView = (TextView) mySnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        mySnackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK){
                showMessageSnackbar(R.string.message_initial);
            }
            checkConnect();
        }
    }
}
