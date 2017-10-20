package com.emotiv.controllers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
