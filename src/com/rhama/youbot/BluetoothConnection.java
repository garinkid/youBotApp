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

import java.util.*;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class BluetoothConnection extends Activity implements OnClickListener{
	
	int requestEnable = 0;
	
	private static String TAG = "BluetoothConnection";
	private static boolean D = true;
	Button bluetoothButton;
	
	private ArrayAdapter<String> foundDevicesArray;
	private ArrayAdapter<String> pairedDevicesArray;
	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private IntentFilter intentFilter;
	public static BluetoothDevice youBot; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		if (D) Log.d(TAG, "ON_CREATE" );
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.device);
						
	 	//view for paired devices
	 	pairedDevicesArray = new ArrayAdapter<String>(this, R.layout.name_of_device);
	 	ListView pairedDevicesListView = (ListView) findViewById(R.id.paired_devices);
	 	pairedDevicesListView.setAdapter(pairedDevicesArray);
	 	pairedDevicesListView.setOnItemClickListener(deviceClickListener);
	 	
	 	//fill pairedDevicesArray with previously bonded devices
	 	Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
	 	if(pairedDevices.size() == 0){
	 		String noPairedDevice = getResources().getString(R.string.no_paired_device);
	 		pairedDevicesArray.add(noPairedDevice);
	 	}else{
	 		for(BluetoothDevice device : pairedDevices){
	 			pairedDevicesArray.add(device.getName()+ "\n" + device.getAddress());	 			
	 		} 	
	 	}
	 	
	 	//view for new found devices
	 	foundDevicesArray = new ArrayAdapter<String>(this, R.layout.name_of_device);
	 	ListView foundDevicesListView = (ListView) findViewById(R.id.new_devices);
	 	foundDevicesListView.setAdapter(foundDevicesArray);
	 	foundDevicesListView.setOnItemClickListener(deviceClickListener);
	 	
	 	//view for Bluetooth Button
	 	bluetoothButton = (Button) findViewById(R.id.bluetooth_button);
	 	bluetoothButton.setOnClickListener(this);	
	 	
	 	
	 	// broadcast Bluetooth action to receiver 
	 	intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	 	this.registerReceiver(receiver, intentFilter); 	
	 	intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	 	this.registerReceiver(receiver, intentFilter);
	 	intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
	 	this.registerReceiver(receiver, intentFilter);
	 	
	}
	
	@Override
	protected void onDestroy(){
		Log.d(TAG, "ON_DESTROY" );
		super.onDestroy();
		// Cancel bluetooth adapter if the bluetooth adapter is discovering
		if(bluetoothAdapter != null)
			bluetoothAdapter.cancelDiscovery();	
		// Unregister broadcast receiver
		this.unregisterReceiver(receiver);
	}
	
    public void onClick(View v){
    	switch(v.getId()){
    	case R.id.bluetooth_button:
    		//define action when bluetooth_button is clicked
	    	if(bluetoothAdapter.isDiscovering()== true){
	    		Log.d(TAG, "BLUETOOTH_SCANNING_STOPPED" );
	    		bluetoothButton.setText("Scan");
	       		setProgressBarIndeterminateVisibility(false);
	        	setTitle(R.string.connect_bluetooth);
	    		bluetoothAdapter.cancelDiscovery();
	    	}else{
	    		Log.d(TAG, "BLUETOOTH_SCANNING_STARTED" );
	    		bluetoothButton.setText("Stop");
	      		setProgressBarIndeterminateVisibility(true);
	    		setTitle(R.string.scanning_devices);
	        	foundDevicesArray.clear();
	        	bluetoothAdapter.startDiscovery();
	    	}
        	break;
    	}
    }   
    
    
    //create receiver for bluetooth activities result 
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
    	@Override
    	public void onReceive(Context context, Intent intent){
    		String action = intent.getAction();
    		if(BluetoothDevice.ACTION_FOUND.equals(action)){
    			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    			if(device.getBondState() != BluetoothDevice.BOND_BONDED){
    				foundDevicesArray.add(device.getName() + "\n" +device.getAddress());
    			}
    		}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
    			setProgressBarIndeterminateVisibility(false);
        		bluetoothButton.setText("Scan");
            	setTitle(R.string.connect_bluetooth);
    			if(foundDevicesArray.getCount() == 0){
    				String noFoundDevice = getResources().getString(R.string.no_found_device);
    				foundDevicesArray.add(noFoundDevice);
    			}
    		}    		
    	}
    	
    };
    
    // when a item from list view is selected 
    private OnItemClickListener deviceClickListener = new OnItemClickListener(){
    	public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3 ){
    		// cancel discovery
    		if(bluetoothAdapter.isDiscovering()){
    			bluetoothAdapter.cancelDiscovery();
    		    setProgressBarIndeterminateVisibility(false);
    		    setTitle(R.string.connect_bluetooth);
    		}
    		
    		String info = ((TextView) view).getText().toString();
  
    		//check if the selected item is a MAC Address
    		if(info.length()<17){
    			Toast.makeText(BluetoothConnection.this, info, Toast.LENGTH_LONG).show();
    		}else{
    			String address = info.substring(info.length() - 17);
    			youBot = bluetoothAdapter.getRemoteDevice(address);
    			if (D) Log.d(TAG, youBot.getAddress() + " was selected");
    			Intent bluetoothConnectionIntent = new Intent();
    	 		bluetoothConnectionIntent.putExtra(YouBot.youBotAddressKey, youBot.getAddress());
    	 		Toast.makeText(BluetoothConnection.this, "Selected device : "+youBot.getName(), Toast.LENGTH_SHORT).show();
    			BluetoothConnection.this.setResult(Activity.RESULT_OK, bluetoothConnectionIntent);
    			finish();
    		}	
    	};
    };    
    
}
    
    
 