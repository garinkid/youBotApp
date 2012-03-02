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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class ArmJointVelocity extends Activity {
	
	private String TAG = "Controller Slider";
	private boolean D = true;
	
	private static String youBotAddress;
	
	private BluetoothService rBluetoothService = null;
	private BluetoothAdapter rBluetoothAdapter;
	private BluetoothDevice youBot = null;
	private SeekBar joint1SeekBar, joint2SeekBar, joint3SeekBar, joint4SeekBar, joint5SeekBar;
	
	private Intent armController;
	
	int maxSeekBar = 50;
	float maxVelocity = (float)1.57;
	int middleOffset = maxSeekBar / 2 ;
	
	double joint1, joint2, joint3, joint4, joint5;
	private Button stopButton, switchButton;
	
	private View helpButton;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "onCreate");
		setContentView(R.layout.arm_joint_velocity);
		
		// view to show base movement
		
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
		
		
		stopButton = (Button)findViewById(R.id.stop_button);
		stopButton.setOnClickListener(onClickListener);
		
		switchButton = (Button)findViewById(R.id.change_mode);
		switchButton.setOnClickListener(onClickListener);
		
		helpButton = (View)findViewById(R.id.help_button);
		helpButton.setOnClickListener(onClickListener);
		
		
		joint1SeekBar = (SeekBar)findViewById(R.id.slider_joint_1);
		joint2SeekBar = (SeekBar)findViewById(R.id.slider_joint_2);
		joint3SeekBar = (SeekBar)findViewById(R.id.slider_joint_3);
		joint4SeekBar = (SeekBar)findViewById(R.id.slider_joint_4);
		joint5SeekBar = (SeekBar)findViewById(R.id.slider_joint_5);
		
		joint1SeekBar.setMax(maxSeekBar);
		joint2SeekBar.setMax(maxSeekBar);
		joint3SeekBar.setMax(maxSeekBar);
		joint4SeekBar.setMax(maxSeekBar);
		joint5SeekBar.setMax(maxSeekBar);
		
		joint1SeekBar.setProgress(middleOffset);
		joint2SeekBar.setProgress(middleOffset);
		joint3SeekBar.setProgress(middleOffset);
		joint4SeekBar.setProgress(middleOffset);
		joint5SeekBar.setProgress(middleOffset);

		joint1SeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		joint2SeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		joint3SeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		joint4SeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		joint5SeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		
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
	
	OnClickListener onClickListener = new OnClickListener(){
		public void onClick(View view) {
			switch(view.getId()){
				case R.id.stop_button:
					joint1 = 0.0;
					joint2 = 0.0;
					joint3 = 0.0;
					joint4 = 0.0;
					joint5 = 0.0;
					joint1SeekBar.setProgress(middleOffset);
					joint2SeekBar.setProgress(middleOffset);
					joint3SeekBar.setProgress(middleOffset);
					joint4SeekBar.setProgress(middleOffset);
					joint5SeekBar.setProgress(middleOffset);
					break;
				case R.id.change_mode:
					armController = new Intent(view.getContext(), ArmJointPosition.class);
					armController.putExtra(YouBot.youBotAddressKey, youBotAddress);
					startActivity(armController);
					finish();
					break;
				case R.id.help_button:
					armController = new Intent(view.getContext(), ArmJointVelocityHelp.class);
					startActivity(armController);
					break;
			}
		}
		
	};
	
	OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener(){
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				switch(seekBar.getId()){
				case R.id.slider_joint_1:
					joint1 = (joint1SeekBar.getProgress() - middleOffset) * (1.0 / middleOffset);
					break;
				case R.id.slider_joint_2:
					joint2 = (joint2SeekBar.getProgress() - middleOffset) *  (1.0 / middleOffset);
					break;
				case R.id.slider_joint_3:
					joint3 = (joint3SeekBar.getProgress() - middleOffset) *  (1.0 / middleOffset);
					break;
				case R.id.slider_joint_4:
					joint4 = (joint4SeekBar.getProgress() - middleOffset) *  (1.0 / middleOffset);
					break;
				case R.id.slider_joint_5:
					joint5 = (joint5SeekBar.getProgress() - middleOffset) *  (1.0 / middleOffset);
					break;
				}
				sendCommand("manipulator,"+joint1 +"," +joint2+ ","+ joint3 + ","+ joint4 + ","+ joint5);
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
		//Log.i(TAG, command);
		if(rBluetoothService != null){
			byte[] sendCommand = command.getBytes();
			rBluetoothService.send(sendCommand);
		}
	};
	
}
