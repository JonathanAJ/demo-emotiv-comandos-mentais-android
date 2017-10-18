package com.emotiv.controllers;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
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
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.emotiv.adapters.TabsAdapter;
import com.emotiv.dao.EngineConnector;
import com.emotiv.util.Util;

public class ActivityTabs extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 0;
    private BluetoothAdapter mBluetoothAdapter;

    private EngineConnector engineConnector;

    private Toolbar toolbar;

    private BottomNavigationView bottomNavigationView;
    private CoordinatorLayout coordinatorLayout;

    private ViewPager mViewPager;

    private int[] menuBottomNavigation = {
            R.id.navigation_status,
            R.id.navigation_treino,
            R.id.navigation_jogo
    };

    public int[] getMenuBottomNavigation(){
        return this.menuBottomNavigation;
    }

    public EngineConnector getEngineConnector() {
        return engineConnector;
    }

    public void setEngineConnector(EngineConnector engineConnector) {
        this.engineConnector = engineConnector;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);

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
    }

    private void checkConnect(){
        if (!mBluetoothAdapter.isEnabled()) {
            // Requisita bluethoot ao usuário
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(this, "Você deve ligar o Bluetooth e a Localização para conectar com Emotiv", Toast.LENGTH_SHORT).show();
        }else {
            /**
             * EngineConnector é a classe que controla
             * e se comunica com o Emotiv.
             */
            EngineConnector.setContext(this);
            engineConnector = EngineConnector.shareInstance();
            Log.d(Util.TAG, "EngineConnector");

            TabsAdapter adapter = new TabsAdapter( getSupportFragmentManager(), this);

            mViewPager = (ViewPager) findViewById(R.id.viewPagerTabs);
            mViewPager.setAdapter( adapter );

            bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.navigation_status);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    if (id == getMenuBottomNavigation()[0]){
                        mViewPager.setCurrentItem(0);
                    }else if (id == getMenuBottomNavigation()[1]){
                        mViewPager.setCurrentItem(1);
                    }else if (id == getMenuBottomNavigation()[2]){
                        mViewPager.setCurrentItem(2);
                    }
                    return true;
                }
            });

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    bottomNavigationView.setSelectedItemId(getMenuBottomNavigation()[position]);
                }
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
                @Override
                public void onPageScrollStateChanged(int state) {}
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.viewPagerTabs);
            if (f instanceof FragmentTreino) {
                ((FragmentTreino) f).onWindowFocusChanged(hasFocus);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
