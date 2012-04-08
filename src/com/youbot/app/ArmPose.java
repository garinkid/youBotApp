package com.youbot.app;

import com.youbot.app.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ArmPose extends Activity {

	private String TAG = "Arm Pose";
	private Boolean D = false;
	
	private static String youBotAddress;
	
	private BluetoothService rBluetoothService = null;
	private BluetoothAdapter rBluetoothAdapter;
	private BluetoothDevice youBot = null;
	
	private int i;
	
	private Button[] poseButtons;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "onCreate");
		setContentView(R.layout.arm_pose);
		
		Bundle controllerSimple = getIntent().getExtras();
		youBotAddress = controllerSimple.getString(YouBot.youBotAddressKey);
		if(youBotAddress!=null){
			rBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			youBot = rBluetoothAdapter.getRemoteDevice(youBotAddress);
			setTitle("Selected device : " + youBot.getName());
		}else{
			setTitle("No device selected");
		}
		
		poseButtons = new Button[2];
		poseButtons[0] = (Button)findViewById(R.id.arm_folded);
		poseButtons[1] = (Button)findViewById(R.id.arm_candle);
		
		for(i=0; i<poseButtons.length; i++){
			poseButtons[i].setOnClickListener(onClickListener);
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
	
	private OnClickListener onClickListener = new OnClickListener(){
		public void onClick(View view){
			switch(view.getId()){
			case R.id.arm_folded:
				sendCommand("arm_joint_position, 169.0, 90.0, 146.0, 102.5, 165.0" );
				break;
			case R.id.arm_candle:
				sendCommand("arm_joint_position, 0.0, 0.0, 0.0, 0.0, 0.0" );
				break;
			}
			
		}
	};
	
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
	
	private void sendCommand(String command){
		if(rBluetoothService != null){
			byte[] sendCommand = command.getBytes();
			rBluetoothService.send(sendCommand);
		}
	};
}
