/*
    GlassHUD - Heads Up Display for Google Glass
    Copyright (C) 2013 James Betker

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ampvita.bluetoothsocket;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

public class AccelListener extends Service implements SensorEventListener{
	//States
	SensorManager sensorManager;
	Sensor aSensor;
	
	@Override
	public void onCreate(){
		super.onCreate();

		//Then sensors
		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
		
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] values = event.values;
		
		Intent intent = new Intent(SendingdataActivity.SENSOR_UPDATE_INTENT);
		intent.putExtra("x", values[0]);
		intent.putExtra("y", values[1]);
		intent.putExtra("z", values[2]);
		
		sendBroadcast(intent);
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return new Binder();
	}
	
	float val1 = 0f;
	float val2 = 0f;
	float val3 = 0f;
	
	public void gyroReading(float[] values){
		val1 = values[0];
		val2 = values[1];
		val3 = values[2];
	}
}
