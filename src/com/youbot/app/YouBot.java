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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.graphics.PorterDuff;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;

// main activity, provide the main menu for KUKA youBot Android App

public class YouBot extends Activity implements OnClickListener{
       
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int requestEnableBluetooth = 1;
    private static final int requestSelectDevice = 2;
    private static final String TAG = "YouBot";   
    private static final boolean D = true;
    private static String youBotAddress = null;
    
    public static String youBotAddressKey = "youBotAddressKey";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	if (D) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);       
        
        View selectButton = this.findViewById(R.id.select_device_button); 
        selectButton.setOnClickListener(this);
        selectButton.getBackground().setColorFilter(Color.parseColor("#FF6600"), PorterDuff.Mode.MULTIPLY);
        
        View manipulatorButton = this.findViewById(R.id.manipulator_button);
        manipulatorButton.setOnClickListener(this);
        manipulatorButton.getBackground().setColorFilter(Color.parseColor("#FF6600"), PorterDuff.Mode.MULTIPLY);
        /*
        View motionProfileButton = this.findViewById(R.id.motion_profile_button);
        motionProfileButton.setOnClickListener(this);
        motionProfileButton.getBackground().setColorFilter(Color.parseColor("#FF6600"), PorterDuff.Mode.MULTIPLY);
    	*/
        
        View controllerButton = this.findViewById(R.id.controller_button);
        controllerButton.setOnClickListener(this);
        controllerButton.getBackground().setColorFilter(Color.parseColor("#FF6600"), PorterDuff.Mode.MULTIPLY);
        
        View mazeGameButton = this.findViewById(R.id.maze_game);
        mazeGameButton.setOnClickListener(this);
        mazeGameButton.getBackground().setColorFilter(Color.parseColor("#FF6600"), PorterDuff.Mode.MULTIPLY);
        
        View quitButton = this.findViewById(R.id.quit_button);
        quitButton.setOnClickListener(this);
        quitButton.getBackground().setColorFilter(Color.parseColor("#FF6600"), PorterDuff.Mode.MULTIPLY);   
        
        View helpButton = this.findViewById(R.id.help_button);
        helpButton.setOnClickListener(this);
        helpButton.getBackground().setColorFilter(Color.parseColor("#FF6600"), PorterDuff.Mode.MULTIPLY);
        
    }
        
    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }
    
    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");       
       
    }
    
	@Override
	protected void onDestroy(){
		Log.d(TAG, "ON_DESTROY" );
		super.onDestroy();
	}
        
    public void onClick(View v){
    	switch(v.getId()){
    	case R.id.select_device_button:
       		if(bluetoothAdapter == null){
    			Toast.makeText(this, "No bluetooth device available", Toast.LENGTH_SHORT).show();
    		}else{
    			Intent bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    			startActivityForResult(bluetoothEnableIntent, requestEnableBluetooth);
    		}
    		break;
    	case R.id.controller_button:
    		baseControllerDialog();
    		break;
    	case R.id.manipulator_button:
    		armControllerDialog();
    		break;
    	/*
    	case R.id.motion_profile_button:
    		Intent motionProfile = new Intent(this, MotionProfile.class);
    		motionProfile.putExtra(youBotAddressKey, youBotAddress);
    		startActivity(motionProfile);
    		break;
    	*/
    	case R.id.help_button:
    		startActivity(new Intent(this, Help.class));
    		break;
    	case R.id.maze_game:
    		Intent maze = new Intent(this, YouBotMazeGame.class);
    		maze.putExtra(youBotAddressKey, youBotAddress);
    		startActivity(maze);
    		break;
    	case R.id.quit_button:
    	    youBotAddress = null;
    		finish();
    		break;
    	
    	}
    }
    
    public void baseControllerDialog(){
    	new AlertDialog.Builder(this)
    		.setTitle(R.string.base_controller)
    		.setItems(R.array.base_controller_type,
    				new DialogInterface.OnClickListener(){
    					public void onClick(DialogInterface dialogInterface, int i){
    						startBaseController(i);
    					}
    				}
    		).show();
    }
    
    
    public void armControllerDialog(){
    	new AlertDialog.Builder(this)
    		.setTitle(R.string.arm_controller)
    		.setItems(R.array.arm_controller_type,
    				new DialogInterface.OnClickListener(){
    					public void onClick(DialogInterface dialogInterface, int i){
    						startArmController(i);
    					}
    				}
    		).show();
    }
     
    public void startBaseController(int i){
    	// start controller activity and add youBotAddress as an extra
    	Intent baseController = new Intent();
    	switch(i){
    	case 0:
    		baseController = new Intent(this, ControllerShift.class);
    		break;
    	case 1:
    		baseController = new Intent(this, ControllerDrive.class);
    		break;
    	case 2:
    		baseController = new Intent(this, ControllerAccel.class);
    		break;
    	case 3: 
    		baseController = new Intent(this, ControllerSlider.class);
    		break;
    	case 4: 
    		baseController = new Intent(this, ControllerCartesian.class);
    		break;
    	}
    	baseController.putExtra(youBotAddressKey, youBotAddress);
		startActivity(baseController);
    
    }
 
    public void startArmController(int i){
    	// start controller activity and add youBotAddress as an extra
    	Intent armController = new Intent();
    	switch(i){
    	case 0:
    		armController = new Intent(this, ArmJointVelocity.class);
    		break;
    	case 1:
    		armController = new Intent(this, ArmJointPosition.class);
    		break;
    	case 2:
    		armController = new Intent(this, ArmPose.class);
    	}
    	armController.putExtra(youBotAddressKey, youBotAddress);
		startActivity(armController);
    
    }
    
    
    public void onActivityResult(int request, int result, Intent intent){
        switch (request) {
        case requestEnableBluetooth:
            if (result == Activity.RESULT_OK) {
            	// Bluetooth device was enabled, start BluetoothConnection activity
        		Intent bluetoothConnection = new Intent(this, BluetoothConnection.class);
          		startActivityForResult(bluetoothConnection, requestSelectDevice);
            } else {
            	// Bluetooth device was not enabled, can not proceed
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            }
            break;
        case requestSelectDevice:
        	if (result == Activity.RESULT_OK){
              	// get bluetooth MAC address of the selected device from BluetoothConnection Activity 
        		youBotAddress =  intent.getExtras().getString(YouBot.youBotAddressKey);
        	}else{
        		Toast.makeText(this, "No devices selected", Toast.LENGTH_SHORT).show();
        	}
        	break;
        }
    }
    
}