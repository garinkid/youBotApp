package com.youbot.app;

import com.youbot.app.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
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
	
	private Button[] poseButtons, gripperButtons;
	
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
		
		poseButtons = new Button[9];
		poseButtons[0] = (Button)findViewById(R.id.arm_maze_pose_button);
		poseButtons[1] = (Button)findViewById(R.id.arm_candle_pose_button);
		poseButtons[2] = (Button)findViewById(R.id.arm_pick_up_plate_pose_button);
		poseButtons[3] = (Button)findViewById(R.id.arm_folded_pose_button);
		poseButtons[4] = (Button)findViewById(R.id.arm_pick_up_front_pose_button);
		poseButtons[5] = (Button)findViewById(R.id.arm_pick_up_left_pose_button);
		poseButtons[6] = (Button)findViewById(R.id.arm_pick_up_right_pose_button);
		poseButtons[7] = (Button)findViewById(R.id.arm_zigzag_forward_pose_button);
		poseButtons[8] = (Button)findViewById(R.id.arm_zigzag_up_pose_button);
		
		
		for(i=0; i<poseButtons.length; i++){
			poseButtons[i].setOnClickListener(onClickListener);
		}
		
		gripperButtons = new Button[2];
		gripperButtons[0] = (Button)findViewById(R.id.gripper_open_button);
		gripperButtons[1] = (Button)findViewById(R.id.gripper_close_button);

		for(i=0; i<gripperButtons.length; i++){
			gripperButtons[i].setOnClickListener(onClickListener);
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
	
	private OnClickListener onClickListener = new OnClickListener(){
		public void onClick(View view){
			switch(view.getId()){
			case R.id.arm_maze_pose_button:
				sendCommand("arm_joint_position, -1.78343949044586, -32.5484076433121, 71.4216560509554, 40.0929936305732, -4.45987261146496" );
				break;
			case R.id.arm_candle_pose_button:
				sendCommand("arm_joint_position, 0.0853503184713418, 4.8445859872611, -4.53949044585988,	-13.8783439490446, -5.29108280254778" );
				break;
			case R.id.arm_pick_up_plate_pose_button:
				sendCommand("arm_joint_position, 0.0, -45.0171974522293, -50.0216560509554, -78.2656050955414, 5.00216560509554" );
				break;
			case R.id.arm_pick_up_front_pose_button:
				sendCommand("arm_joint_position, 0,	75.0376433121019,	45.0223566878981,	50.0250955414013,	0" );
				break;
			case R.id.arm_pick_up_left_pose_button:
				sendCommand("arm_joint_position, -90.040127388535,	75.0376433121019,	45.0223566878981,	50.0250955414013,	0" );
				break;
			case R.id.arm_pick_up_right_pose_button:
				sendCommand("arm_joint_position, 90.040127388535,	75.0376433121019,	45.0223566878981,	50.0250955414013,	0" );
				break;
			case R.id.arm_zigzag_up_pose_button:
				sendCommand("arm_joint_position, 0,	-40.0184713375796,	90.0452866242038,	-90.040127388535,	-90.040127388535" );
				break;
			case R.id.arm_zigzag_forward_pose_button:
				sendCommand("arm_joint_position, 0,	90.0452866242038,	90.0452866242038,	-90.040127388535,	-90.040127388535" );
				break;
			case R.id.arm_folded_pose_button:
				sendCommand("arm_joint_position, -169, -65, 146, -102.5, -165" );
				break;
			case R.id.gripper_open_button:
				sendCommand("gripper, 0.023");
				break;
			case R.id.gripper_close_button:
				sendCommand("gripper, 0.000");
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
