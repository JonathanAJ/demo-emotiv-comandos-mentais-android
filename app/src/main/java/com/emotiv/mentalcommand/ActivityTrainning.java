package com.emotiv.mentalcommand;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.emotiv.getdata.EngineConnector;
import com.emotiv.getdata.EngineInterface;
import com.emotiv.insight.MentalCommandDetection.IEE_MentalCommandTrainingControl_t;
import com.emotiv.insight.IEmoStateDLL.IEE_MentalCommandAction_t;
import com.emotiv.insight.MentalCommandDetection;
import com.emotiv.spinner.AdapterSpinner;
import com.emotiv.spinner.DataSpinner;
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

public class ActivityTrainning extends AppCompatActivity implements EngineInterface {

	private static final int REQUEST_ENABLE_BT = 1;
	private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 0;
	private BluetoothAdapter mBluetoothAdapter;
	
	EngineConnector engineConnector;
	
	Spinner spinnerAction;
	Button btnTrain,btnClear;
	ProgressBar progressBarTime,progressPower;
	AdapterSpinner spinAdapter;
	ImageView imgBox;
	ArrayList<DataSpinner> model = new ArrayList<DataSpinner>();
	int indexSpinnerAction, _currentAction, userId = 0, count = 0;

	Timer timer;
	TimerTask timerTask;

	float _currentPower = 0;
	float startLeft     = -1;
	float startRight    = 0;
	float widthScreen   = 0;

	boolean isTrainning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trainning);

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		Util.initToolbar(this, false, this.getResources().getString(R.string.app_name));

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			/*Android 6.0 and higher need to request permission*****/
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_FINE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
			/****Request turn on Bluetooth***************/
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

		}else {
			/**
			 * EngineConnector é a classe que controla
			 * e se comunica com o Emotiv.
			 */
			EngineConnector.setContext(this);
			engineConnector = EngineConnector.shareInstance();
			engineConnector.delegate = this;
			init();
		}
	}

	public void init() {
			spinnerAction =(Spinner)findViewById(R.id.spinnerAction);
			btnTrain=(Button)findViewById(R.id.btstartTraing);
			btnClear=(Button)findViewById(R.id.btClearData);
			btnClear.setOnClickListener(new OnClickListener() {
				/**
				 * indexSpinnerAction é o parâmetro que diz em qual
				 * posição do Spinner está para limpar o comando treinado
				 */
				@Override
				public void onClick(View arg0) {
					switch (indexSpinnerAction) {
					case 0:
						engineConnector.trainningClear(IEE_MentalCommandAction_t.MC_NEUTRAL);
						break;
					case 1:
						engineConnector.trainningClear(IEE_MentalCommandAction_t.MC_PUSH);
						break;
					case 2:
						engineConnector.trainningClear(IEE_MentalCommandAction_t.MC_PULL);
						break;
					case 3:
						engineConnector.trainningClear(IEE_MentalCommandAction_t.MC_LEFT);
						break;
					case 4:
						engineConnector.trainningClear(IEE_MentalCommandAction_t.MC_RIGHT);
						break;
					default:
						break;
					}
				}
			});
			progressBarTime = (ProgressBar)findViewById(R.id.progressBarTime);
			progressBarTime.setVisibility(View.INVISIBLE);
			progressPower = (ProgressBar)findViewById(R.id.ProgressBarpower);
			imgBox = (ImageView)findViewById(R.id.imgBox);

			// seta o spinner
			setDataSpinner();

			// muda o item do spinner atual
			spinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					indexSpinnerAction = arg2;
				}
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
			btnTrain.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!engineConnector.isConnected) {
						Toast.makeText(ActivityTrainning.this, "Você precisa conectar seu Emotiv", Toast.LENGTH_SHORT).show();
					}
					else{
						switch (indexSpinnerAction) {
						case 0:
							startTrainingMentalcommand(IEE_MentalCommandAction_t.MC_NEUTRAL);
							break;
						case 1:
							engineConnector.enableMentalcommandActions(IEE_MentalCommandAction_t.MC_PUSH);
							startTrainingMentalcommand(IEE_MentalCommandAction_t.MC_PUSH);
							break;
						case 2:
							engineConnector.enableMentalcommandActions(IEE_MentalCommandAction_t.MC_PULL);
							startTrainingMentalcommand(IEE_MentalCommandAction_t.MC_PULL);
							break;
						case 3:
							engineConnector.enableMentalcommandActions(IEE_MentalCommandAction_t.MC_LEFT);
							startTrainingMentalcommand(IEE_MentalCommandAction_t.MC_LEFT);
							break;
						case 4:
							engineConnector.enableMentalcommandActions(IEE_MentalCommandAction_t.MC_RIGHT);
							startTrainingMentalcommand(IEE_MentalCommandAction_t.MC_RIGHT);
							break;
						default:
							break;
						}
					}
				}
			});

			Timer timerListenAction = new Timer();
			timerListenAction.scheduleAtFixedRate(new TimerTask() {
			    @Override
			    public void run() {
			    	handlerUpdateUI.sendEmptyMessage(1);
			    }
			},
			0, 20);	
			
	}

	/**
	 * Pega o tempo de treinamento e mostra em uma
	 * progressBar
	 */
	Handler handlerUpdateUI = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				count ++;
				int trainningTime=(int)MentalCommandDetection.IEE_MentalCommandGetTrainingTime(userId)[1]/1000;
				if(trainningTime > 0)
					progressBarTime.setProgress(count / trainningTime);
				if (progressBarTime.getProgress() >= 100) {
					timerTask.cancel();
					timer.cancel();
				}
				break;
			case 1:
				moveImage();
				break;
			default:
				break;
			}
		};
	};

	public void startTrainingMentalcommand(IEE_MentalCommandAction_t MentalCommandAction) {
		isTrainning = engineConnector.startTrainingMentalcommand(isTrainning, MentalCommandAction);
		btnTrain.setText((isTrainning) ? "Abortar treino" : "Treinar");
	}
	
	public void setDataSpinner()
	{
		/**
		 * Seta o Spinner com os respectivos nomes e se já foram treinados
		 */
		model.clear();
		DataSpinner data = new DataSpinner();
		data.setTvName("Neutral");
		data.setChecked(engineConnector.checkTrained(IEE_MentalCommandAction_t.MC_NEUTRAL.ToInt()));
		model.add(data);
		
		data=new DataSpinner();
		data.setTvName("Push");
		data.setChecked(engineConnector.checkTrained(IEE_MentalCommandAction_t.MC_PUSH.ToInt()));
		model.add(data);
		
		data=new DataSpinner();
		data.setTvName("Pull");
		data.setChecked(engineConnector.checkTrained(IEE_MentalCommandAction_t.MC_PULL.ToInt()));
		model.add(data);
		
		data=new DataSpinner();
		data.setTvName("Left");
		data.setChecked(engineConnector.checkTrained(IEE_MentalCommandAction_t.MC_LEFT.ToInt()));
		model.add(data);
		
		data=new DataSpinner();
		data.setTvName("Right");
		data.setChecked(engineConnector.checkTrained(IEE_MentalCommandAction_t.MC_RIGHT.ToInt()));
		model.add(data);
		
		spinAdapter = new AdapterSpinner(this, R.layout.row, model);
		spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerAction.setAdapter(spinAdapter);
	}

	public void TimerTask() {
		count = 0;
		timerTask = new TimerTask() {
			@Override
			public void run() {
				handlerUpdateUI.sendEmptyMessage(0);
			}
		};
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		    Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			widthScreen = size.x;
			startLeft = imgBox.getLeft();
			startRight = imgBox.getRight();
	}
	
	private void moveImage() {
			float power = _currentPower;
			if(isTrainning){
				imgBox.setLeft((int) startLeft);
				imgBox.setRight((int) startRight);
				imgBox.setScaleX(1.0f);
				imgBox.setScaleY(1.0f);
			}
			if(( _currentAction == IEE_MentalCommandAction_t.MC_LEFT.ToInt())  || (_currentAction == IEE_MentalCommandAction_t.MC_RIGHT.ToInt()) && power > 0) {

				if(imgBox.getScaleX() == 1.0f && startLeft > 0) {
					imgBox.setRight((int) widthScreen);
					power = (_currentAction == IEE_MentalCommandAction_t.MC_LEFT.ToInt()) ? power*3 : power*-3;
					imgBox.setLeft((int) (power > 0 ? Math.max(0, (int)(imgBox.getLeft() - power)) : Math.min(widthScreen - imgBox.getMeasuredWidth(), (int)(imgBox.getLeft() - power))));
				}
			}
			else if(imgBox.getLeft() != startLeft && startLeft > 0){
				power = (imgBox.getLeft() > startLeft) ? 6 : -6;
				imgBox.setLeft(power > 0  ? Math.max((int)startLeft, (int)(imgBox.getLeft() - power)) : Math.min((int)startLeft, (int)(imgBox.getLeft() - power)));
			}
			if(((_currentAction == IEE_MentalCommandAction_t.MC_PULL.ToInt()) || (_currentAction == IEE_MentalCommandAction_t.MC_PUSH.ToInt())) && power > 0) {
				if(imgBox.getLeft() != startLeft)
					return;
				imgBox.setRight((int) startRight);
				power = (_currentAction == IEE_MentalCommandAction_t.MC_PUSH.ToInt()) ? power / 20 : power/-20;
				imgBox.setScaleX((float) (power > 0 ? Math.max(0.1, (imgBox.getScaleX() - power)) : Math.min(2, (imgBox.getScaleX() - power))));
				imgBox.setScaleY((float) (power > 0 ? Math.max(0.1, (imgBox.getScaleY() - power)) : Math.min(2, (imgBox.getScaleY() - power))));
			} 
			else if(imgBox.getScaleX() != 1.0f){
				power = (imgBox.getScaleX() < 1.0f) ? 0.03f : -0.03f;
				imgBox.setScaleX((float) (power > 0 ? Math.min(1, (imgBox.getScaleX() + power)) : Math.max(1, (imgBox.getScaleX() + power))));
				imgBox.setScaleY((float) (power > 0 ? Math.min(1, (imgBox.getScaleY() + power)) : Math.max(1, (imgBox.getScaleY() + power))));		
			}
	}
	public void enableClick() {
		btnClear.setClickable(true);
		spinnerAction.setClickable(true);
	}

	@Override
	public void userAdd(int userId) {
		this.userId = userId;
	}

	@Override
	public void currentAction(int typeAction, float power) {
		progressPower.setProgress((int)(power * 100));
		_currentAction = typeAction;
		_currentPower  = power;
	}

	@Override
	public void userRemoved() {

	}
	
	@Override
	public void trainStarted() {
		progressBarTime.setVisibility(View.VISIBLE);
		btnClear.setClickable(false);
		spinnerAction.setClickable(false);
		timer = new Timer();
		TimerTask();
		timer.schedule(timerTask , 0, 10);
	}

	@Override
	public void trainSucceed() {
		progressBarTime.setVisibility(View.INVISIBLE);
		btnTrain.setText("Treinar");
		enableClick();
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				ActivityTrainning.this);
		// set title
		alertDialogBuilder.setTitle("Sucesso");
		// set dialog message
		alertDialogBuilder
				.setMessage("Treinamento com sucesso. Você aceita este treinamento?")
				.setCancelable(false)
				.setIcon(R.mipmap.ic_launcher)
				.setPositiveButton("Sim",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,int which) {
								engineConnector.setTrainControl(IEE_MentalCommandTrainingControl_t.MC_ACCEPT.getType());
							}
						})
				.setNegativeButton("Não",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								engineConnector.setTrainControl(IEE_MentalCommandTrainingControl_t.MC_REJECT.getType());
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	@Override
	public  void trainFailed(){
		progressBarTime.setVisibility(View.INVISIBLE);
		btnTrain.setText("Treinar");
		enableClick();
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				ActivityTrainning.this);
		// set title
		alertDialogBuilder.setTitle("Falha");
		// set dialog message
		alertDialogBuilder
				.setMessage("Sinal com muito ruído. Não foi possível treinar")
				.setCancelable(false)
				.setIcon(R.mipmap.ic_launcher)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog, int which) {

							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
		isTrainning = false;
	}

	@Override
	public void trainCompleted() {
		DataSpinner data = model.get(indexSpinnerAction);
		data.setChecked(true);
		model.set(indexSpinnerAction, data);
		spinAdapter.notifyDataSetChanged();
		isTrainning = false;
	}

	@Override
	public void trainRejected() {
		DataSpinner data=model.get(indexSpinnerAction);
		data.setChecked(false);
		model.set(indexSpinnerAction, data);
		spinAdapter.notifyDataSetChanged();
		enableClick();
		isTrainning = false;
	}

	@Override
	public void trainErased() {
		 new AlertDialog.Builder(this)
	    .setTitle("Treinamento apagado")
	    .setMessage("")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	     public void onClick(DialogInterface dialog, int which) { 
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();
		DataSpinner data = model.get(indexSpinnerAction);
		data.setChecked(false);
		model.set(indexSpinnerAction, data);
		spinAdapter.notifyDataSetChanged();
		enableClick();
		isTrainning = false;
	}
	
	@Override
	public void trainReset() {
		if(timer != null){
			timer.cancel();
			timerTask.cancel();
		}
		isTrainning = false;
		progressBarTime.setVisibility(View.INVISIBLE);
		progressBarTime.setProgress(0);
		enableClick();
	};
	
	public void onBackPressed() {
		android.os.Process.killProcess(android.os.Process.myPid());
		finish();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_BLUETOOTH: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					/****Request turn on Bluetooth***************/
					Toast.makeText(this, "Permissão concedida", Toast.LENGTH_SHORT).show();
					checkConnect();
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this, "App não funcionará sem essa permissão", Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if(resultCode == Activity.RESULT_OK){
				Toast.makeText(this, "Bluetooth ligado", Toast.LENGTH_SHORT).show();
			}
			if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(this, "Você deve ligar o bluetooth para conectar com Emotiv", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
