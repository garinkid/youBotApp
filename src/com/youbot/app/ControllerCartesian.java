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
import com.youbot.app.VerticalSeekBar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class ControllerCartesian extends Activity{
	
	private boolean D = true;
	private String TAG = "Controller Cartesian";
	private SeekBar horizontalSeekBar;
	private VerticalSeekBar verticalSeekBar;
	private BaseMovement youBotBaseMovement = new BaseMovement();
	int maxSeekBar = 50;
	int middleOffset = maxSeekBar / 2 ;
	
	private BluetoothAdapter rBluetoothAdapter;
	private BluetoothDevice youBot;
	private BluetoothService rBluetoothService = null;
	
	private String youBotAddress = null;
	private String oldCommand = "";
	
	private FrameLayout cartesianLayout;
	private Button stopView, connectView, switchView, returnView;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "onCreate");
		setContentView(R.layout.controller_cartesian);
		
		horizontalSeekBar = (SeekBar)findViewById(R.id.slider_angular);
		verticalSeekBar = (VerticalSeekBar)findViewById(R.id.slider_longitudinal);
		
		horizontalSeekBar.setMax(maxSeekBar);
		verticalSeekBar.setMax(maxSeekBar);
		horizontalSeekBar.setProgress(middleOffset);
		verticalSeekBar.setProgress(middleOffset);
		
		horizontalSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		horizontalSeekBar.setOnTouchListener(onTouchListener);
		verticalSeekBar.setOnSeekBarChangeListener(onVerticalSeekBarChangeListener);
		
		cartesianLayout =  (FrameLayout)findViewById(R.id.main_controller);
		ViewCartesian viewCartesian = new ViewCartesian(this);
		cartesianLayout.addView(viewCartesian);
		cartesianLayout.setOnTouchListener(onTouchListener);
		
		stopView = (Button)findViewById(R.id.stop_base_button);
		connectView = (Button)findViewById(R.id.connect_button);
		switchView = (Button)findViewById(R.id.switch_controller);
		returnView = (Button)findViewById(R.id.return_main_menu);
		stopView.setOnClickListener(onClickListener);
		connectView.setOnClickListener(onClickListener);
		switchView.setOnClickListener(onClickListener);
		returnView.setOnClickListener(onClickListener);	

		
		
		Bundle controller = getIntent().getExtras();
		youBotAddress = controller.getString(YouBot.youBotAddressKey);
		if(youBotAddress == null){
			setTitle("No device selected");
		}else{
			rBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			youBot = rBluetoothAdapter.getRemoteDevice(youBotAddress);
			setTitle("Selected device "+youBot.getName());	
		}
		
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
	}
	
	private OnClickListener onClickListener = new OnClickListener(){
		public void onClick(View view){
			switch(view.getId()){
				case R.id.stop_base_button:
					sendCommand("base, 0.0, 0.0, 0.0");	
					horizontalSeekBar.setProgress(middleOffset);
					verticalSeekBar.setProgress(middleOffset);
					break;
				case R.id.connect_button:
					if(youBot != null){
						sendCommand("onPause");
						setUpConnection();
						
					}else{
						Toast.makeText(view.getContext(), "no device selected", Toast.LENGTH_SHORT).show();
					}			
					break;
				case R.id.switch_controller:
					Intent controllerAccel = new Intent(view.getContext(), ControllerShift.class);
					controllerAccel.putExtra(YouBot.youBotAddressKey, youBotAddress);
					startActivity(controllerAccel);
					finish();
					break;
				case R.id.return_main_menu:
					finish();				
					break;
			}		
		}
	};
	
	
	OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener(){

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			int increment = 100 / middleOffset;
			switch(seekBar.getId()){
			case R.id.slider_angular:
				if (youBotBaseMovement.longitudinalVelocity < 0 ){
					youBotBaseMovement.setAngular(  ( progress - middleOffset ) * (1.0 / middleOffset) );
				}else{
					youBotBaseMovement.setAngular(  (- ( progress - middleOffset ) ) * (1.0 / middleOffset) );
				}
				
				break;				
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
	
	 VerticalSeekBar.OnSeekBarChangeListener onVerticalSeekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener() {

			public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
				switch(seekBar.getId()){
				case R.id.slider_longitudinal:
					youBotBaseMovement.setLongitudinal( ( progress - middleOffset ) * (1.0 / middleOffset) );
					break;			
				}	
				sendCommand("base, " + youBotBaseMovement.longitudinalVelocity +", " +youBotBaseMovement.transversalVelocity + ","+ youBotBaseMovement.angularVelocity);
			}

			public void onStartTrackingTouch(
					com.youbot.app.VerticalSeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(
					com.youbot.app.VerticalSeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		};
	
	OnTouchListener onTouchListener = new OnTouchListener(){
		public boolean onTouch(View view, MotionEvent motionEvent) {
			double center_x = view.getWidth()/2;
			double x = motionEvent.getX() - center_x;
			double center_y = view.getHeight()/2;
			double y = motionEvent.getY() - center_y;
				//Log.d(TAG,""+motionEvent.getAction());
			int action = motionEvent.getAction();
			switch(action){
				case MotionEvent.ACTION_MOVE | MotionEvent.ACTION_DOWN:
					controlMovement(x, y);
					break;
				case MotionEvent.ACTION_UP:
					sendCommand("base, 0.0, 0.0, 0.0");	
					horizontalSeekBar.setProgress(middleOffset);
					verticalSeekBar.setProgress(middleOffset);
					break;
			}
			return true;
				
		}
		
	};
	
	private void controlMovement(double x, double y){
		double x_max = cartesianLayout.getWidth() / 2;
		double y_max = cartesianLayout.getHeight() / 2;
		int x_offset, y_offset;
		
		if (x < 0){
			x_offset = (int)Math.ceil(x / x_max * middleOffset) ; 
		}else{
			x_offset = (int)Math.floor(x / x_max * middleOffset);
		}
		
		if (y > 0){
			y_offset = (int)Math.floor(y / y_max * middleOffset) ; 
		}else{
			y_offset = (int)Math.ceil(y / y_max * middleOffset);
		}
	
		verticalSeekBar.setProgress(middleOffset - y_offset);	
		horizontalSeekBar.setProgress(middleOffset + x_offset);


	}
	
	
	
	public void sendCommand(String command){
		if(command.compareTo(this.oldCommand) == 0){}
		else{
			this.oldCommand = command;
			byte[] sendCommand = command.getBytes();
			if(rBluetoothService != null){
				rBluetoothService.send(sendCommand);
			}
		}
	};
	
	public void setUpConnection(){
		rBluetoothService = new BluetoothService(this, rBluetoothHandler);
		rBluetoothService.connect(this.youBot);
	}
	
	private Handler rBluetoothHandler = new Handler(){
		@Override
		public void handleMessage(Message message){
			switch(message.what){
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
}