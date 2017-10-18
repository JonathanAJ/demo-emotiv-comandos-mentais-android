package com.emotiv.controllers;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.emotiv.dao.EngineConnector;
import com.emotiv.dao.EngineInterface;
import com.emotiv.insight.MentalCommandDetection.IEE_MentalCommandTrainingControl_t;
import com.emotiv.insight.IEmoStateDLL.IEE_MentalCommandAction_t;
import com.emotiv.insight.MentalCommandDetection;
import com.emotiv.adapters.AdapterSpinner;
import com.emotiv.adapters.DataSpinner;
import com.emotiv.util.Util;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class ActivityTrainning extends AppCompatActivity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trainning);
	}
	
	public void onBackPressed() {
		android.os.Process.killProcess(android.os.Process.myPid());
		finish();
	}
}
