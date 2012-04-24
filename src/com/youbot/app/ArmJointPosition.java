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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ArmJointPosition extends Activity {
	
	private String TAG = "Controller Arm Joint Position";
	private boolean D = true;
	
	private static String youBotAddress;
	
	private BluetoothService rBluetoothService = null;
	private BluetoothAdapter rBluetoothAdapter;
	private BluetoothDevice youBot = null;
	private RadioButton degreeRadioButton, radianRadioButton;
	private EditText[] jointsEditText;
	private Button sendButton, switchButton;
	
	private View[] jointPlus, jointMinus;
	
	private View helpButton;
	
	private int i;
	private String string;
	
	private float[] jointsAngle, maxJointsAngle, minJointsAngle, home;
	private float angle, checkRange;
	
	int maxSeekBar = 8;
	int middleOffset = maxSeekBar / 2 ;
	
	private String unit;
	
	private Intent armController;
	
	double joint1, joint2, joint3, joint4, joint5;

	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "onCreate");
		setContentView(R.layout.arm_joint_position);
		
		// view to show base movement
		
		// get MAC address
		
		degreeRadioButton = (RadioButton)findViewById(R.id.radio_degree);
		radianRadioButton = (RadioButton)findViewById(R.id.radio_radian);
		
		
		degreeRadioButton.setOnClickListener(onClickRadioListener);
		radianRadioButton.setOnClickListener(onClickRadioListener);
		
		degreeRadioButton.setChecked(true);
		radianRadioButton.setChecked(false);
		unit = "degree";
		
		sendButton = (Button)findViewById(R.id.send_button);
		sendButton.setOnClickListener(onClickButtonListener);
		
		helpButton = (View)findViewById(R.id.help_button);
		helpButton.setOnClickListener(onClickButtonListener);
		
		jointsEditText = new EditText[5];
		jointsEditText[0] =(EditText)findViewById(R.id.entry_joint_1);
		jointsEditText[1] =(EditText)findViewById(R.id.entry_joint_2);
		jointsEditText[2] =(EditText)findViewById(R.id.entry_joint_3);
		jointsEditText[3] =(EditText)findViewById(R.id.entry_joint_4);
		jointsEditText[4] =(EditText)findViewById(R.id.entry_joint_5);
		
		jointPlus = new View[5];
		jointPlus[0] = (View)findViewById(R.id.plus_joint_1);
		jointPlus[1] = (View)findViewById(R.id.plus_joint_2);
		jointPlus[2] = (View)findViewById(R.id.plus_joint_3);
		jointPlus[3] = (View)findViewById(R.id.plus_joint_4);
		jointPlus[4] = (View)findViewById(R.id.plus_joint_5);
		
		jointMinus = new View[5];
		jointMinus[0] = (View)findViewById(R.id.minus_joint_1);
		jointMinus[1] = (View)findViewById(R.id.minus_joint_2);
		jointMinus[2] = (View)findViewById(R.id.minus_joint_3);
		jointMinus[3] = (View)findViewById(R.id.minus_joint_4);
		jointMinus[4] = (View)findViewById(R.id.minus_joint_5);
		
		switchButton = (Button)findViewById(R.id.change_mode);
		switchButton.setOnClickListener(onClickButtonListener);
		
		for(i=0;i<5;i++){
			jointPlus[i].setOnClickListener(onClickPlusListener);
		}
		
		for(i=0;i<5;i++){
			jointMinus[i].setOnClickListener(onClickMinusListener);
		}
		
		float[] home = {(float) -169.0, (float) -65.0, (float) 146.0, (float)-102.5, (float)-165.0} ;
		
		for(i=0; i < 5; i++){
			jointsEditText[i].setText(Float.toString(home[i]));
		}
	
		
		Bundle controllerSimple = getIntent().getExtras();
		youBotAddress = controllerSimple.getString(YouBot.youBotAddressKey);
		if(youBotAddress!=null){
			rBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			youBot = rBluetoothAdapter.getRemoteDevice(youBotAddress);
			setTitle("Selected device : " + youBot.getName());
		}else{
			setTitle("No device selected");
		}	
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
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
	
	public void setUpConnection(){
		rBluetoothService = new BluetoothService(this, bluetoothHandler);
		rBluetoothService.connect(this.youBot);		
	}
	
	private OnClickListener onClickRadioListener = new OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.radio_degree:
					if(unit == "radian"){
						for(i=0;i<5;i++){
							string = jointsEditText[i].getText().toString();
							if (string.contentEquals("") == true){
								string = "00.00";
								jointsEditText[i].setText(string);
							}
							angle = new Float(string);
							jointsEditText[i].setText(Float.toString((float)Math.toDegrees(angle)));
						}
					}
					
					degreeRadioButton.setChecked(true);
					radianRadioButton.setChecked(false);
					unit = "degree";
					break;
				case R.id.radio_radian:
					if(unit == "degree"){
						for(i=0;i<5;i++){
							string = jointsEditText[i].getText().toString();
							if (string.contentEquals("") == true){
								string = "00.00";
								jointsEditText[i].setText(string);
							}
							angle = new Float(string);
							jointsEditText[i].setText(Float.toString((float)Math.toRadians(angle)));
						}
					}
					degreeRadioButton.setChecked(false);
					radianRadioButton.setChecked(true);
					unit = "radian";
					break;
				case R.id.send_button:
					collect_position();
					break;
			}
		}
	};
	
	private OnClickListener onClickButtonListener = new OnClickListener() {
		public void onClick(View view) {
			switch(view.getId()){
				case R.id.send_button:
					collect_position();
					break;
				case R.id.change_mode:
					armController = new Intent(view.getContext(), ArmJointVelocity.class);
					armController.putExtra(YouBot.youBotAddressKey, youBotAddress);
					startActivity(armController);
					finish();
					break;
				case R.id.help_button:
					armController = new Intent(view.getContext(), ArmJointPositionHelp.class);
					startActivity(armController);
					break;
			}
		}
	};
	
	private OnClickListener onClickPlusListener = new OnClickListener() {
		float[] maxJointsAngle = {(float) 169.0, (float) 90.0, (float)146.0, (float)102.5, (float)165.0} ;
		public void onClick(View v) {
			for(i=0;i<5;i++){
				if (v == jointPlus[i]){
					string = jointsEditText[i].getText().toString();
					if (string.contentEquals("") == true){
						string = "00.00";
						jointsEditText[i].setText(string);
					}
					angle = new Float(string);
					if(unit == "radian"){
						angle = (float)(angle + 0.1);
						checkRange = (float)Math.toDegrees(angle);
					}else{
						angle = (float)(angle + 10.0);
						checkRange = angle;
					}
					
					//check range
					if(checkRange > maxJointsAngle[i]){
						angle = maxJointsAngle[i];
						if (unit =="radian"){	
							jointsEditText[i].setText(Float.toString((float)Math.toRadians(maxJointsAngle[i])));
						}else{jointsEditText[i].setText(Float.toString(maxJointsAngle[i]));}
					}
					else {
						jointsEditText[i].setText(Float.toString(angle));
					}
					break;
				}
			}
		}
	};
	
	private OnClickListener onClickMinusListener = new OnClickListener() {
		float[] minJointsAngle = {(float) -169.0, (float) -65.0, (float) -151.0, (float)-102.5, (float)-165.0} ;
		public void onClick(View v) {
			for(i=0;i<5;i++){
				if (v == jointMinus[i]){
					string = jointsEditText[i].getText().toString();
					if (string.contentEquals("") == true){
						string = "00.00";
						jointsEditText[i].setText(string);
					}
					angle = new Float(string);
					if(unit == "radian"){
						angle = (float)(angle - 0.1);
						checkRange = (float)Math.toDegrees(angle);
					}else{
						angle = (float)(angle - 10.0);
						checkRange = angle;
					}
					
					//check range
					if (checkRange < minJointsAngle[i]){
						angle= minJointsAngle[i];
						if (unit =="radian"){	
							jointsEditText[i].setText(Float.toString((float)Math.toRadians(minJointsAngle[i])));
						}else{jointsEditText[i].setText(Float.toString(minJointsAngle[i]));}
					}else {
						jointsEditText[i].setText(Float.toString(angle));
					}
					break;
				}
			}
		}
	};
	
	private void collect_position(){
		float[] maxJointsAngle = {(float) 169.0, (float) 90.0, (float)146.0, (float)102.5, (float)165.0} ;
		float[] minJointsAngle = {(float) -169.0, (float) -65.0, (float) -151.0, (float)-102.5, (float)-165.0} ;
		
		jointsAngle = new float[5];
		for(i=0; i<5; i++){ 
			string = jointsEditText[i].getText().toString();
			if (string.contentEquals("") == true){
				string = "00.00";
				jointsEditText[i].setText(string);
			}
			jointsAngle[i] = new Float(string);
			if (unit == "radian"){
				jointsAngle[i] = (float) Math.toDegrees(jointsAngle[i]);
			}
			if(jointsAngle[i] > maxJointsAngle[i]){
				jointsAngle[i]= maxJointsAngle[i];
				if (unit =="radian"){	
					jointsEditText[i].setText(Float.toString((float)Math.toRadians(maxJointsAngle[i])));
				}else{jointsEditText[i].setText(Float.toString(maxJointsAngle[i]));}
			}
			else if (jointsAngle[i] < minJointsAngle[i]){
				jointsAngle[i]= minJointsAngle[i];
				if (unit =="radian"){	
					jointsEditText[i].setText(Float.toString((float)Math.toRadians(minJointsAngle[i])));
				}else{jointsEditText[i].setText(Float.toString(minJointsAngle[i]));}
			}
		}
		
		sendCommand("arm_joint_position, " + jointsAngle[0] + ", " + jointsAngle[1] + ", "+ jointsAngle[2] + ", "+ jointsAngle[3] + ", "+ jointsAngle[4]);	
	};
	
	private void sendCommand(String command){
		if(rBluetoothService != null){
			byte[] sendCommand = command.getBytes();
			rBluetoothService.send(sendCommand);
		}
	};
	
	public Handler bluetoothHandler = new Handler(){
		String toastMessage;
		@Override
		public void handleMessage(Message message){
			switch (message.what){
				case BluetoothService.STATE_CHANGE:
					switch(message.arg1){
					case BluetoothService.CONNECTING:
						setTitle("Connecting to "+youBot.getName());
						break;
					case BluetoothService.STATE_CONNECTED:
						toastMessage = "Connected to "+youBot.getName();
						Toast.makeText(getBaseContext(), toastMessage.subSequence(0, toastMessage.length()), Toast.LENGTH_SHORT).show();
						setTitle("Connected to "+youBot.getName());		
						break;
					}
				break;
			}
		}
	};
	
}
