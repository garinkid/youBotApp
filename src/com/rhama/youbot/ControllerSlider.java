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

package com.rhama.youbot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class ControllerSlider extends Activity {
	
	private String TAG = "Controller Slider";
	private boolean D = true;
	
	private static String youBotAddress;
	
	private BluetoothService rBluetoothService = null;
	private BluetoothAdapter rBluetoothAdapter;
	private BluetoothDevice youBot = null;
	private BaseMovement youBotBaseMovement = new BaseMovement();
	private Button stopButtonView, connectView, switchView, returnView;
	
	int maxSeekBar = 50;
	int middleOffset = maxSeekBar / 2 ;
		
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "onCreate");
		setContentView(R.layout.controller_slider);
		
		// view to show base movement
		TextView baseMovementView = (TextView)findViewById(R.id.base_movement_text);
		baseMovementView.setText("0.0, 0.0, 0.0");
		
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
		
		
		stopButtonView = (Button)findViewById(R.id.stop_base_button);
		connectView = (Button)findViewById(R.id.connect_button);
		switchView = (Button)findViewById(R.id.switch_controller);
		returnView = (Button)findViewById(R.id.return_main_menu);
		
		SeekBar longitudinalSeekBar, transversalSeekBar, angularSeekBar;
		
		longitudinalSeekBar = (SeekBar)findViewById(R.id.slider_longitudinal);
		transversalSeekBar = (SeekBar)findViewById(R.id.slider_transversal);
		angularSeekBar = (SeekBar)findViewById(R.id.slider_angular);
		
		longitudinalSeekBar.setMax(maxSeekBar);
		transversalSeekBar.setMax(maxSeekBar);
		angularSeekBar.setMax(maxSeekBar);
		
		longitudinalSeekBar.setProgress(middleOffset);
		transversalSeekBar.setProgress(middleOffset);
		angularSeekBar.setProgress(middleOffset);

		longitudinalSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		transversalSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		angularSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		
		stopButtonView.setOnClickListener(onClickListener);
		connectView.setOnClickListener(onClickListener);
		switchView.setOnClickListener(onClickListener);
		returnView.setOnClickListener(onClickListener);
		
		
		
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
		if(youBot != null){
			setUpConnection();
		}
	}	
	
	@Override
	protected void onDestroy(){
		Log.d(TAG, "ON_DESTROY" );
		super.onDestroy();
	}
	
	OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener(){
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				switch(seekBar.getId()){
				case R.id.slider_longitudinal:
					youBotBaseMovement.setLongitudinal( ( progress - middleOffset ) * (1.0 / middleOffset) );
					break;
				case R.id.slider_transversal:
					youBotBaseMovement.setTransversal( (-( progress - middleOffset )) *(1.0 / middleOffset) );
					break;
				case R.id.slider_angular:
					if (youBotBaseMovement.longitudinalVelocity < 0 ){
						youBotBaseMovement.setAngular(  ( progress - middleOffset ) * (1.0 / middleOffset) );
					}else{
						youBotBaseMovement.setAngular(  (- ( progress - middleOffset ) ) * (1.0 / middleOffset) );
					}
				}
				sendCommand("base, " + youBotBaseMovement.longitudinalVelocity +", " +youBotBaseMovement.transversalVelocity + ","+ youBotBaseMovement.angularVelocity);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	OnClickListener onClickListener = new OnClickListener(){
		public void onClick(View view) {
			SeekBar longitudinalSeekBar, transversalSeekBar, angularSeekBar;
			longitudinalSeekBar = (SeekBar)findViewById(R.id.slider_longitudinal);
			transversalSeekBar = (SeekBar)findViewById(R.id.slider_transversal);
			angularSeekBar = (SeekBar)findViewById(R.id.slider_angular);
			switch(view.getId()){
				case R.id.stop_base_button:
					longitudinalSeekBar.setProgress(middleOffset);
					transversalSeekBar.setProgress(middleOffset);
					angularSeekBar.setProgress(middleOffset);
					sendCommand("base, " + youBotBaseMovement.transversalVelocity + ", "+ youBotBaseMovement.longitudinalVelocity +", " + youBotBaseMovement.angularVelocity);
					break;
				case R.id.connect_button:
					if(youBot != null && rBluetoothService != null){
						sendCommand("onPause");
						setUpConnection();
					}else{
						Toast.makeText(view.getContext(), "no device selected", Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.switch_controller:
					sendCommand("base, 0.0, 0.0, 0.0");
					finish();
					Intent controller = new Intent(view.getContext(), ControllerCartesian.class);
					controller.putExtra(YouBot.youBotAddressKey, youBotAddress);
					startActivity(controller);
		    		break;
				case R.id.return_main_menu:
					finish();
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
		if (command != "onPause"){
			TextView youBotBaseMovementView = (TextView)findViewById(R.id.base_movement_text);
			youBotBaseMovementView.setText(command);
		}
		if(rBluetoothService != null){
			byte[] sendCommand = command.getBytes();
			rBluetoothService.send(sendCommand);
		}
	};
	
}
