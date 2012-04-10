package com.youbot.app;


import com.youbot.app.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class YouBotMazeGame extends Activity{
	
	private Button gripperOpenButton, gripperCloseButton, setPositionButton;

	private static String youBotAddress;
	
	private static String TAG = "Maze game";
	
	private BluetoothService rBluetoothService = null;
	private BluetoothAdapter rBluetoothAdapter;
	private BluetoothDevice youBot = null;
	
	private float[] default_pose = {(float)2.9172909934892601, (float)0.56619152215177582, (float)-1.231205868905108, (float)2.5834732319552178, (float)2.9052652400289047};
	private float[] offset = {(float)169, (float)65, (float)-142, (float)108, (float)171};
	
	private float fixedThetaX, fixedThetaY, thetaX, thetaY;
	
	private SensorManager sensorManager = null;
	private Sensor orientSensor;
	private int i;
	int maxSeekBar = 50;
	int middleOffset = maxSeekBar / 2 ;
	double gameSensitivity;
	
	private ToggleButton gameStatusToggleButton;
	private SeekBar gameSensitivitySeekBar;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maze_game);
		setTitle("youBot Maze Game");
		
		// get MAC address
		Bundle controllerSimple = getIntent().getExtras();
		youBotAddress = controllerSimple.getString(YouBot.youBotAddressKey);
		if(youBotAddress!=null){
			rBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			youBot = rBluetoothAdapter.getRemoteDevice(youBotAddress);
			setTitle("youBot Maze Game --> Selected device : " + youBot.getName());
		}
		

		for(i=0;i<5;i++){
			default_pose[i] = (float)Math.toDegrees(default_pose[i]) - offset[i];
		};
			
		gripperOpenButton = (Button)this.findViewById(R.id.gripper_open);
		gripperOpenButton.setOnClickListener(onClickListener);
		
		gripperCloseButton = (Button)this.findViewById(R.id.gripper_close);
		gripperCloseButton.setOnClickListener(onClickListener);
		
		setPositionButton = (Button)this.findViewById(R.id.reset_position);
		setPositionButton.setOnClickListener(onClickListener);

		gameStatusToggleButton = (ToggleButton)this.findViewById(R.id.game_status_toggle_button);
		gameStatusToggleButton.setOnClickListener(onClickListener);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		orientSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensorManager.registerListener(sensorEventListener, orientSensor, SensorManager.SENSOR_DELAY_FASTEST);
		
		gameSensitivitySeekBar = (SeekBar)findViewById(R.id.game_sensitivity_seekbar);
		gameSensitivitySeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		gameSensitivitySeekBar.setMax(maxSeekBar);
		gameSensitivitySeekBar.setProgress(middleOffset);
		
		
		
	}
	
	@Override
	public synchronized void onPause(){
		super.onPause();
		if(rBluetoothService != null){
			sendCommand("manipulator, 0.0, 0.0, 0.0, 0.0, 0.0");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendCommand("onPause");
		}
	}
	
	@Override
	public synchronized void onResume(){
		super.onResume();
		if(youBot != null){
			setUpConnection();
		}
	}	
	
	@Override
	protected void onDestroy(){
		Log.d(TAG, "ON_DESTROY" );
		if(rBluetoothService != null){
			sendCommand("manipulator ,0.0, 0.0, 0.0, 0.0, 0.0");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendCommand("onPause");
		}
		super.onDestroy();
	}
	
	private OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener(){

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
				switch(seekBar.getId()){
				case R.id.game_sensitivity_seekbar:
					gameSensitivity = ((progress) * (1.0 / maxSeekBar)) + 0.5;
					break;
				}
			
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

	};
	
	private SensorEventListener sensorEventListener = new SensorEventListener(){
		public void onSensorChanged(SensorEvent sensorEvent){
			thetaY = sensorEvent.values[1];
			thetaX = sensorEvent.values[2];
			float deltaThetaX, deltaThetaY;
			
			if (gameStatusToggleButton.isChecked()){
				deltaThetaX = fixedThetaX - thetaX;
				deltaThetaY = fixedThetaY - thetaY ;
				sendCommand("maze," + deltaThetaX + "," + deltaThetaY + "," + gameSensitivity);
			}
			
			
		}
		
		public void onAccuracyChanged(Sensor arg0, int arg1) {
	    }
	};
	
	private OnClickListener onClickListener = new OnClickListener(){
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.gripper_open:
				sendCommand("gripper, 0.023");
				break;
			case R.id.gripper_close:
				sendCommand("gripper, 0.000");
				break;
			case R.id.reset_position:
				sendCommand("arm_joint_position, " + default_pose[0] + ", "  + default_pose[1] + ", "  + default_pose[2] + ", "  + default_pose[3] + ", "  + default_pose[4]);
				break;
			case R.id.game_status_toggle_button:
				if (gameStatusToggleButton.isChecked()){
					fixedThetaX = thetaX;
					fixedThetaY = thetaY;
				}
				break;
			}
		}
	};
	
	public void setUpConnection(){
		rBluetoothService = new BluetoothService(this, bluetoothHandler);
		rBluetoothService.connect(this.youBot);		
	}
	
	private Handler bluetoothHandler = new Handler(){
		@Override
		public void handleMessage(Message message){	
			switch (message.what){
				case BluetoothService.STATE_CHANGE:
					switch(message.arg1){
					case BluetoothService.CONNECTING:
						setTitle("Connecting to "+youBot.getName());
						break;
					case BluetoothService.STATE_CONNECTED:
						setTitle("Connected to "+youBot.getName());		
						break;
					}
				break;
			}
		}
	};
	
	public void sendCommand(String command){
		//Log.i(TAG, command);
		if(rBluetoothService != null){
			byte[] sendCommand = command.getBytes();
			rBluetoothService.send(sendCommand);
		}
	};
	
}