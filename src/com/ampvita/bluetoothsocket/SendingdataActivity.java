package com.ampvita.bluetoothsocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SendingdataActivity extends Activity {
	/** Called when the activity is first created. */
	private BluetoothAdapter mBluetoothAdapter = null;
	UUID MY_UUID;
	static String address = "00:1B:DC:06:62:48";

	static SendData sendData;

	long start = 0;
	long finish = 0;

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
			MY_UUID = UUID.fromString("3e4610be-2d72-11e3-a59d-f23c91aec05e"); // "ba65e08b-6476-42e0-91df-98380308844e");
																				// //
																				// 4e4610be-2d72-11e3-a59d-f23c91aec05e");
		} else {
			findViewById(R.id.ImageView).setVisibility(View.INVISIBLE);
			MY_UUID = UUID.fromString("3e4610be-2d72-11e3-a59d-f23c91aec05e");
		}
		sendData = new SendData();
	}

	int sum = 0;

	private boolean mReceiversRegistered = false;

	static String SENSOR_UPDATE_INTENT = "com.ampvita.bluetoothsocket.SENSOR_UPDATE_INTENT";

	// Define a handler and a broadcast receiver
	private final Handler mHandler = new Handler();
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		public int normalize(float in) {
			if (in > 4)
				return 1;
			else if (in < -4)
				return -1;
			else
				return 0;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SENSOR_UPDATE_INTENT)) {

				float inX = intent.getExtras().getFloat("x");
				// float y = intent.getExtras().getFloat("y");
				float inZ = intent.getExtras().getFloat("z");

				int x = normalize(inX);
				// y = normalize(y);
				int z = normalize(inZ);
				int val = 0;

				switch (x) {
				case 1:
					val = 7;
					break;
				case 0:
					val = 0;
					break;
				case -1:
					val = 4;
					break;
				}

				switch (z) {
				case 1:
					val += 2;
					break;
				case 0:
					val += 0;
					break;
				case -1:
					val += 1;
					break;
				}

				// 987 left right forward +7
				// 654 left right backward +4
				// 210 left right nowhere +1

				if (Build.VERSION.SDK_INT == 15)
					  sendData.readMessage();
				
				TextView snes = (TextView) findViewById(R.id.textView1);
				snes.setText("\nx:" + x + "\nz:" + z);

				finish = System.nanoTime();
				if (finish - start > 5000000) {
					sendData.sendMessage(val + "");
					val = 0;
					start = finish;
				}

				start = System.nanoTime();
			}
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		startService(new Intent(this, AccelListener.class));
	}

	@Override
	public void onResume() {
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(SENSOR_UPDATE_INTENT);
		this.registerReceiver(mIntentReceiver, intentToReceiveFilter, null,
				mHandler);
		mReceiversRegistered = true;
		if (Build.VERSION.SDK_INT == 15)
		  sendData.readMessage();
	}

	@Override
	public void onPause() {
		super.onPause();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// Make sure you unregister your receivers when you pause your activity
		if (mReceiversRegistered) {
			unregisterReceiver(mIntentReceiver);
			mReceiversRegistered = false;
		}
		if (isFinishing())
			this.stopService(new Intent(this, AccelListener.class));
	}

	class SendData extends Thread {
		private BluetoothDevice device = null;
		private BluetoothSocket btSocket = null;
		public InputStream inStream = null;
		private OutputStream outStream = null;

		@SuppressLint("NewApi")
		public SendData() {
			device = mBluetoothAdapter.getRemoteDevice(address);
			for (int i = 0; i < device.getUuids().length; i++)
				Log.e("connect", device.getUuids()[i].toString());
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
				inStream = btSocket.getInputStream();
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

		public void readMessage() {
				Bitmap bitmap = BitmapFactory.decodeStream(inStream);
				((ImageView)findViewById(R.id.ImageView)).setImageBitmap(bitmap);
		}
	}
}

