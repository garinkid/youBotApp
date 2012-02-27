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


import java.lang.Math;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.util.*;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class ControllerDrive extends Activity implements OnTouchListener{
	
	private final static String TAG = "controller";
	//private boolean D = true;
	private Button stopView, connectView, switchView, returnView;
		
	private float center_x, center_y;
	private BluetoothAdapter rBluetoothAdapter;
	private BluetoothDevice youBot;
	private BluetoothService rBluetoothService = null;
	
	private String youBotAddress = null;
	private String oldCommand = "";
		
	public void onCreate(Bundle savedInstanceState){

		super.onCreate(savedInstanceState); 
		setContentView(R.layout.controller_drive);

		stopView = (Button)findViewById(R.id.stop_base_button);
		connectView = (Button)findViewById(R.id.connect_button);
		switchView = (Button)findViewById(R.id.switch_controller);
		returnView = (Button)findViewById(R.id.return_main_menu);
		stopView.setOnClickListener(onClickListener);
		connectView.setOnClickListener(onClickListener);
		switchView.setOnClickListener(onClickListener);
		returnView.setOnClickListener(onClickListener);	

		TextView baseMovementView = (TextView)findViewById(R.id.base_movement_text);
		baseMovementView.setText("0.0, 0.0, 0.0");
			
		FrameLayout main = (FrameLayout) findViewById(R.id.main_controller);
		ViewDrive controllerView = new ViewDrive(this);
		main.addView(controllerView);
		controllerView.setOnTouchListener(this);
			
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
		
	public OnClickListener onClickListener = new OnClickListener(){
		public void onClick(View view){
			switch(view.getId()){
				case R.id.stop_base_button:
					if(rBluetoothService != null){
						sendCommand("base, 0.0, 0.0, 0.0");					
					}					
					break;
				case R.id.connect_button:
					if(youBot != null){
						if(rBluetoothService != null){
							sendCommand("onPause");
							setUpConnection();
						}
					}else{
						Toast.makeText(view.getContext(), "no device selected", Toast.LENGTH_SHORT).show();
					}			
					break;
				case R.id.switch_controller:
					Intent controllerAccel = new Intent(view.getContext(), ControllerAccel.class);
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
		
	public boolean onTouch(View view, MotionEvent motionEvent){
		center_x = view.getWidth()/2;
		center_y = view.getHeight()/2;
			//Log.d(TAG,""+motionEvent.getAction());
		int action = motionEvent.getAction();
		switch(action){
			case MotionEvent.ACTION_MOVE | MotionEvent.ACTION_DOWN:
				try{
					controlMovement(motionEvent.getX(),motionEvent.getY());
				}catch (Exception e){
				}
				break;
			case MotionEvent.ACTION_UP:
				String command = ("base, 0.0, 0.0, 0.0");
				if(rBluetoothService != null){
					sendCommand(command);					
				}
				TextView baseMovementView = (TextView)findViewById(R.id.base_movement_text);
				baseMovementView.setText(command);
				break;
		}
				
		return true;
	}


	private void controlMovement(float x_value, float y_value){			
		double x = (x_value-center_x) ;
		double y = (-y_value+center_y);
		double longitudinalVelocity, angularVelocity;
		
		double xyVector = Math.sqrt(Math.pow(x,2) + Math.pow(y, 2));
		//if(Math.abs(x) < 100 && Math.abs(y) < 100){
		if(Math.abs(xyVector) < 100){	
			if(y > 0){
				//forward
				longitudinalVelocity = (Math.floor(xyVector * 0.04) * 25);
				
				if(x < 0){
					angularVelocity = -( (Math.ceil(x * 0.04)) * 25 );
				}else{
					angularVelocity = -( (Math.floor(x * 0.04)) * 25 );
				}
				
			}else{
				//backward
				longitudinalVelocity = -(Math.floor(xyVector * 0.04) * 25);
				
				if(x < 0){
					angularVelocity = (Math.ceil(x * 0.04)) * 25;
				}else{
					angularVelocity = (Math.floor(x * 0.04)) * 25;
				}
			}
			sendCommand("base, " + longitudinalVelocity + "," + "0.0" + "," + angularVelocity);
		}
	}				
	
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
		
	public void sendCommand(String command){
		if(command.compareTo(this.oldCommand) == 0){}
		else{
			this.oldCommand = command;
			if (command != "onPause"){
				TextView baseMovementView = (TextView)findViewById(R.id.base_movement_text);
				baseMovementView.setText(command);
			}
			byte[] sendCommand = command.getBytes();
			rBluetoothService.send(sendCommand);
		}
	};
	
}

