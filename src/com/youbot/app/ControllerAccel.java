/****************************************************************
 *
 * Copyright (c) 2011
 * All rights reserved.
 *
 * Hochschule Bonn-Rhein-Sieg
 * University of Applied Sciences
 * Computer Science Department
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * Author : Rhama Dwiputra
 * Contributor : Azamat Shakhimardanov
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * This sofware is published under a dual-license: GNU Lesser General Public 
 * License LGPL 2.1 and ASL2.0 license. The dual-license implies that users of this
 * code may choose which terms they prefer.
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Hochschule Bonn-Rhein-Sieg nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License LGPL as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version or the ASL2.0 license.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License LGPL and the ASL2.0 license for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License LGPL and ASL2.0 license along with this program.
 *
 ****************************************************************/

package com.youbot.app;

import com.youbot.app.R;

import android.app.Activity;
import android.hardware.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.*;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;

// try out

public class ControllerAccel extends Activity{
	
	private Boolean D = true;
	private static String TAG = "ControllerAccel.java";
	private String youBotAddress = null;
	private BluetoothAdapter rBluetoothAdapter;
	private BluetoothDevice youBot;
	private SensorManager sensorManager = null;
	private Sensor orientSensor;
	private float fixedRoll, fixedPitch, roll, pitch;
	private ToggleButton accelButton;	
	public BluetoothService rBluetoothService;
	private Button stopButtonView, connectView, switchView, returnView;
	private ImageView accelAnimation;
	private String oldCommand = "";
	private BaseMovement youBotBaseMovement = new BaseMovement();
	int maxSeekBar = 50;
	int middleOffset = maxSeekBar / 2 ;
    private WindowManager windowManager;
    private Display display; 
	private int naturalOrientation;
	SeekBar angularSeekBar;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "onCreate");
		setContentView(R.layout.controller_accel);
		
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
		naturalOrientation = display.getRotation();
		//toggle button
		accelButton = (ToggleButton)findViewById(R.id.accel_button);
		accelButton.setOnClickListener(onClickListener);
		
		accelAnimation = (ImageView)findViewById(R.id.accel_animation);
		accelAnimation.setImageResource(R.drawable.arrow_none);
		
		angularSeekBar = (SeekBar)findViewById(R.id.slider_angular);
		angularSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		angularSeekBar.setMax(maxSeekBar);
		angularSeekBar.setProgress(middleOffset);
		
		//menu view
		stopButtonView = (Button)findViewById(R.id.stop_base_button);
		connectView = (Button)findViewById(R.id.connect_button);
		switchView = (Button)findViewById(R.id.switch_controller);
		returnView = (Button)findViewById(R.id.return_main_menu);


		stopButtonView.setOnClickListener(onClickListener);
		connectView.setOnClickListener(onClickListener);
		switchView.setOnClickListener(onClickListener);
		returnView.setOnClickListener(onClickListener);
		
		// view to show base movement
		TextView baseMovementView = (TextView)findViewById(R.id.base_movement_text);
		baseMovementView.setText("base, 0.0, 0.0, 0.0");
		
		// get MAC address
		Bundle controllerSimple = getIntent().getExtras();
		youBotAddress = controllerSimple.getString(YouBot.youBotAddressKey);
		if(youBotAddress!=null){
			rBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			youBot = rBluetoothAdapter.getRemoteDevice(youBotAddress);
			setTitle("Selected device : " + youBot.getName());
		}else{
			setTitle("No device selected");
		}
		
		//get reference to sensor service
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		orientSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensorManager.registerListener(sensorEventListener, orientSensor, SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	public synchronized void onPause(){
		super.onPause();
		if(rBluetoothService != null){
			sendCommand("onPause");
		}
	}	
	
	@Override
	public synchronized void onResume(){
		super.onResume();
		if(youBot != null){setUpConnection();}
	}
	
	@Override
	protected void onDestroy(){
		Log.d(TAG, "ON_DESTROY" );
		super.onDestroy();
		// Unregister sensor listener
		sensorManager.unregisterListener(sensorEventListener);
	}
	
	
	
	SensorEventListener sensorEventListener = new SensorEventListener(){
		public void onSensorChanged(SensorEvent sensorEvent){
			if(sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION){
				switch(naturalOrientation){
				//default 
				case Surface.ROTATION_0:
					pitch = sensorEvent.values[1];
					roll = sensorEvent.values[2];
					break;
				// tablet
				case Surface.ROTATION_90:
					pitch = - sensorEvent.values[2];
					roll = sensorEvent.values[1];
					break;
				// others
				case Surface.ROTATION_180:
					pitch = - sensorEvent.values[1];
					roll = - sensorEvent.values[2];
					break;
				// others
				case Surface.ROTATION_270:
					pitch = sensorEvent.values[2];
					roll = - sensorEvent.values[1];
					break;
				}
				if (accelButton.isChecked() == true ){
					float deltaRoll, deltaPitch;
					deltaRoll = roll - fixedRoll;
					deltaPitch = pitch - fixedPitch;
					// if (D) Log.d(TAG, "deltaRoll : " + Float.toString(deltaRoll));
					// check if the inclination threshold have been reach
					if (Math.abs(deltaRoll) > 15 || Math.abs(deltaPitch) > 15){
						// check which inclination that reach the threshold
						if (Math.abs(deltaRoll) > Math.abs(deltaPitch)){
							//check the direction of the inclination
							
							if (deltaRoll > 0 ){
								youBotBaseMovement.setTransversal(0.70);
								accelAnimation.setImageResource(R.drawable.arrow_left);
							}else{
								youBotBaseMovement.setTransversal(-0.70);
								accelAnimation.setImageResource(R.drawable.arrow_right);
							}
							youBotBaseMovement.setLongitudinal(0.0);
														
						}else{
							//check the direction of the inclination
							
							if (deltaPitch > 0){
								youBotBaseMovement.setLongitudinal(0.70);
								accelAnimation.setImageResource(R.drawable.arrow_up);
							}else{
								youBotBaseMovement.setLongitudinal(-0.70);
								accelAnimation.setImageResource(R.drawable.arrow_down);
							}
							youBotBaseMovement.setTransversal(0.0);
							
						}
					}else{
						youBotBaseMovement.setTransversal(0.0);
						youBotBaseMovement.setLongitudinal(0.0);
						accelAnimation.setImageResource(R.drawable.arrow_none);
					}
					
					sendCommand("base, " + youBotBaseMovement.longitudinalVelocity +", " +youBotBaseMovement.transversalVelocity + ", "+ youBotBaseMovement.angularVelocity);
				}
			};
		};
		
		//this function have to exist within SensorEventListener
		public void onAccuracyChanged(Sensor arg0, int arg1) {
	    }
	};
	
	OnClickListener onClickListener = new OnClickListener(){
		public void onClick(View view){
			switch(view.getId()){
				case R.id.connect_button:
					if(youBot != null && rBluetoothService != null){
						// the connection is already established,
						// disconnect through "onPause"
						// create new connection
						sendCommand("onPause");
						setUpConnection();
					}else{
						Toast.makeText(view.getContext(), "no device selected", Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.switch_controller:
					sensorManager.unregisterListener(sensorEventListener);
					sendCommand("base, 0.0, 0.0, 0.0");
					finish();
					Intent controller = new Intent(view.getContext(), ControllerSlider.class);
					controller.putExtra(YouBot.youBotAddressKey, youBotAddress);
					startActivity(controller);
		    		break;
		    		
				case R.id.return_main_menu:
					finish();
					break;
				case R.id.accel_button:
					if (accelButton.isChecked() == true){
						fixedPitch = pitch;
						fixedRoll = roll;
					}else{
						accelAnimation.setImageResource(R.drawable.arrow_none);
						youBotBaseMovement.setStop();
						sendCommand("base," + youBotBaseMovement.longitudinalVelocity +", " +youBotBaseMovement.transversalVelocity + ","+ youBotBaseMovement.angularVelocity);
					}
					break;
				case R.id.stop_base_button:
					if(accelButton.isChecked()){
						accelButton.setChecked(false);
					}
					angularSeekBar.setProgress(middleOffset);
					youBotBaseMovement.setStop();
					sendCommand("base," + youBotBaseMovement.longitudinalVelocity +", " +youBotBaseMovement.transversalVelocity + ","+ youBotBaseMovement.angularVelocity);
					accelAnimation.setImageResource(R.drawable.arrow_none);
					break;
			}
		}
	};

	

	OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener(){
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				switch(seekBar.getId()){
				case R.id.slider_angular:
					if(youBotBaseMovement.longitudinalVelocity < 0 ){
						youBotBaseMovement.setAngular( ( progress - middleOffset) * (1.0 / middleOffset));
					}else{
						youBotBaseMovement.setAngular( ( middleOffset - progress) * (1.0 / middleOffset));
					}
					break;				
				}	
				sendCommand("base," + youBotBaseMovement.longitudinalVelocity +", " +youBotBaseMovement.transversalVelocity + ", "+ youBotBaseMovement.angularVelocity);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
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
		if(command.compareTo(this.oldCommand) == 0){}
		else{
			this.oldCommand = command;
			if (command != "onPause"){
				TextView baseMovementView = (TextView)findViewById(R.id.base_movement_text);
				baseMovementView.setText(command);
			}
			byte[] sendCommand = command.getBytes();
			if(rBluetoothService != null){
				rBluetoothService.send(sendCommand);
			}
		}
	};
	

}


