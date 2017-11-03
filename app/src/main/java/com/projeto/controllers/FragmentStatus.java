package com.projeto.controllers;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.projeto.dao.EngineStatus;
import com.projeto.interfaces.EngineStatusInterface;
import com.projeto.util.Util;

public class FragmentStatus extends Fragment implements EngineStatusInterface{

    private EngineStatus engineStatus;
    private View rootView;
    private ArcProgress arcProgress;
    private ActivityMain activityMain;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_status, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        engineStatus = EngineStatus.shareInstance(this);
        activityMain = (ActivityMain) getActivity();
        initListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        engineStatus.createTimerTask();
        Log.d(Util.TAG, "Status OnResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        engineStatus.stopTimertask();
        Log.d(Util.TAG, "Status OnPause");
    }

    private void initListeners(){
        arcProgress = (ArcProgress) rootView.findViewById(R.id.arc_progress);
    }

    @Override
    public void onUserRemoved() {
        activityMain.showMessageSnackbar(R.string.connect_emotiv_fail);
    }

    @Override
    public void updateStatusQuality(double status) {
        arcProgress.setProgress((int) status);
    }
}