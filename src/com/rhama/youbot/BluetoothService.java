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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.util.Log;

public class BluetoothService {
	private static final String TAG = "BluetoothService";
	private static boolean D = true;
			
	BluetoothSocket rBluetoothSocket;
	BluetoothSocket temp = null;
	BluetoothAdapter adapter;
	Handler rBluetoothHandler;
		
	private int rBluetoothState;
	
	public static final int CONNECTING = 1;
	public static final int STATE_NONE = 2;
	public static final int STATE_CONNECTED = 3;
	public static final int STATE_CHANGE = 4;	
	
	public BluetoothDevice rBluetoothDevice;
	
	private ConnectingThread rConnectingThread;
	private ConnectedThread rConnectedThread;
	
	
	public BluetoothService(Context context, Handler bluetoothHandler){
		adapter = BluetoothAdapter.getDefaultAdapter();
		rBluetoothHandler = bluetoothHandler;
		rBluetoothState = STATE_NONE;
	}
	
		
	private synchronized void setState (int state){
		rBluetoothState = state;
		rBluetoothHandler.obtainMessage(BluetoothService.STATE_CHANGE, state, -1).sendToTarget();
	} 
	
	
	public synchronized void connect(BluetoothDevice bluetoothDevice){
		if(rBluetoothState != STATE_CONNECTED){
			rBluetoothDevice = bluetoothDevice;
			rConnectingThread = new ConnectingThread(rBluetoothDevice);
		}
		rConnectingThread.start();
	}
	
	public synchronized void connected(BluetoothSocket bluetoothSocket, BluetoothDevice bluetoothDevice){
		Log.d(TAG, bluetoothSocket.getRemoteDevice().getName());
		rConnectedThread = new ConnectedThread(bluetoothSocket);
		rConnectedThread.start();
		setState(STATE_CONNECTED);
	}
	
	public synchronized int getState(){
		return rBluetoothState;
	}
	
	private class ConnectingThread extends Thread{
		private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		private BluetoothDevice rBluetoothDevice;
		private BluetoothSocket rBluetoothSocket;
		public ConnectingThread(BluetoothDevice bluetoothDevice){
			rBluetoothDevice = bluetoothDevice;
			Log.d(TAG, "Connecting to "+rBluetoothDevice.getName());
			BluetoothSocket temp = null;
			Method m = null;
		        try {
		            m = rBluetoothDevice.getClass().getMethod("createRfcommSocket",new Class[] {int.class});
		        } catch (SecurityException e) {
		            e.printStackTrace();
		        } catch (NoSuchMethodException e) {
		            e.printStackTrace();
		        } 
		        
		        try {
		            temp = (BluetoothSocket) m.invoke(rBluetoothDevice, 1);
		        } catch (IllegalArgumentException e) {
		            e.printStackTrace();
		        } catch (IllegalAccessException e) {
		            e.printStackTrace();
		        } catch (InvocationTargetException e) {
		            e.printStackTrace();
		        }
		        rBluetoothSocket = temp;
			}

		public void run(){
			setState(CONNECTING);
	        if(adapter.isDiscovering()){
	        	adapter.cancelDiscovery();
	        }
			try{
				
				rBluetoothSocket.connect();
			}catch(IOException connectException){
				try{
					
					rBluetoothSocket.close();
				}catch(IOException closeException){}
				return;
			}
			
			connected(rBluetoothSocket, rBluetoothDevice);
		}		        
	}
		
	private class ConnectedThread extends Thread{
		private final BluetoothSocket rBluetoothSocket;
		
		private final OutputStream rOutputStream;
		public ConnectedThread(BluetoothSocket bluetoothSocket){
			if(D) Log.d(TAG, "connected thread");
			Log.d(TAG,bluetoothSocket.getRemoteDevice().getName());
			rBluetoothSocket = bluetoothSocket;
			rBluetoothDevice = rBluetoothSocket.getRemoteDevice();
			OutputStream tempOutput = null;	
						
			try
			{
				tempOutput = bluetoothSocket.getOutputStream();
			}catch(IOException e){
				Log.e(TAG, "no socket available",e);
			}
			
			rOutputStream = tempOutput;	
		}
				
		public void write(byte[] buffer){
			try{
				//if(D) Log.d(TAG, "writing buffer");
				rOutputStream.write(buffer);
			}catch(IOException e){
				Log.e(TAG, "Exception during writing", e);
			}
		}
	}
	
	//send command
	public void send(byte[] send){
		ConnectedThread r;
		synchronized (this){
			if (rBluetoothState != STATE_CONNECTED) return;
			r = rConnectedThread;
		}
		r.write(send);
	}

}




