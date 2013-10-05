package com.ampvita.bluetoothsocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class SendingdataActivity extends Activity {
	/** Called when the activity is first created. */
	private BluetoothAdapter mBluetoothAdapter = null;
	UUID MY_UUID;
	static String address = "00:1B:DC:06:62:48";
	
	static SendData sendData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available.",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(this,
					"Please enable your BT and re-run this program.",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		} // have correct adapter
		if (Build.VERSION.SDK_INT == 15) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			MY_UUID = UUID.fromString("4e4610be-2d72-11e3-a59d-f23c91aec05e");
		} else {
			findViewById(R.id.VideoView).setVisibility(View.INVISIBLE);
			MY_UUID = UUID.fromString("3e4610be-2d72-11e3-a59d-f23c91aec05e");
		}
		sendData = new SendData();
		sendData.sendMessage("hey dan");
	}

	private boolean mReceiversRegistered = false;

	static String SENSOR_UPDATE_INTENT = "com.ampvita.bluetoothsocket.SENSOR_UPDATE_INTENT";

	// Define a handler and a broadcast receiver
	private final Handler mHandler = new Handler();
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
	    
		public float normalize(float in) {
			return (float)(Math.floor((((in+9.8)/19.6)*255)));
		}
		
		@Override
	    public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(SENSOR_UPDATE_INTENT)) {
	        	
	        	float x = intent.getExtras().getFloat("x");
	        	float y = intent.getExtras().getFloat("y");
	        	float z = intent.getExtras().getFloat("z");
	        	
	        	x = normalize(x);
	        	y = normalize(y);
	        	z = normalize(z);
	        	
	        	TextView snes = (TextView) findViewById(R.id.textView1);
	        	snes.setText("x:"+x+"\ny:"+y+"\nz:"+z);
	        	
	        	sendData.sendMessage(x + " " + y + " " + z);
	    	}
	    }
	};
	
	@Override
	public void onStart(){
		super.onStart();
		startService(new Intent(this, AccelListener.class));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(SENSOR_UPDATE_INTENT);
		this.registerReceiver(mIntentReceiver, intentToReceiveFilter, null, mHandler);
		mReceiversRegistered = true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		  // Make sure you unregister your receivers when you pause your activity
		  if(mReceiversRegistered) {
		    unregisterReceiver(mIntentReceiver);
		    mReceiversRegistered = false;
		  }
	}
	
	class SendData extends Thread {
		private BluetoothDevice device = null;
		private BluetoothSocket btSocket = null;
		private OutputStream outStream = null;
		
		@SuppressLint("NewApi") public SendData() {
			device = mBluetoothAdapter.getRemoteDevice(address);
			try {
				btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (Exception e) {
				Log.e("connect", "no socket");
			}
			mBluetoothAdapter.cancelDiscovery();
			try {
				btSocket.connect();
			} catch (IOException e) {
				Log.e("connect", "no connect");
				e.printStackTrace();
				try {
					btSocket.close();
				} catch (IOException e2) {
					Log.e("connect", "no close");
				}
			}
			Toast.makeText(getBaseContext(),
					"Connected to " + device.getName(), Toast.LENGTH_SHORT)
					.show();
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				Log.e("connect", "no stream");
			}
		}

		public void sendMessage(String data) {
			try {
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				byte[] b = data.getBytes();
				outStream.write(b);
				outStream.flush();
			} catch (IOException e) {
			}
		}
	}
}