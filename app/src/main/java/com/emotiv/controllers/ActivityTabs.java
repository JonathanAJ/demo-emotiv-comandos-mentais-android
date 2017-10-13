package com.emotiv.controllers;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.emotiv.adapters.TabsAdapter;
import com.emotiv.util.Util;

public class ActivityTabs extends AppCompatActivity {

    private Toolbar toolbar;

    private BottomNavigationView bottomNavigationView;

    private ViewPager mViewPager;

    private int[] menuBottomNavigation = {
            R.id.navigation_status,
            R.id.navigation_treino,
            R.id.navigation_jogo
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);

        toolbar = Util.initToolbar(this, false, "Comando Mental");

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

    public int[] getMenuBottomNavigation(){
        return this.menuBottomNavigation;
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
}
