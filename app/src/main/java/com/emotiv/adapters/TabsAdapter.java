package com.emotiv.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.emotiv.controllers.ActivityTabs;
import com.emotiv.controllers.FragmentGame;
import com.emotiv.controllers.FragmentStatus;
import com.emotiv.controllers.FragmentTrain;

public class TabsAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private ActivityTabs activityTabs;

    public TabsAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        activityTabs = (ActivityTabs) (this.context);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position){
            case 0:
                fragment = new FragmentStatus();
                break;
            case 1:
                fragment = new FragmentTrain();
                break;
            default:
                fragment = new FragmentGame();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return activityTabs.getMenuBottomNavigation().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
