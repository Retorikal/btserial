package com.retorikal.btserial;

import android.app.Instrumentation;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.List;

import me.aflak.bluetooth.Bluetooth;

public class BTService extends Service implements Bluetooth.CommunicationCallback {
	private Bluetooth bt;
	private BluetoothDevice device;
	private Injector injector;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		bt = new Bluetooth(this);
		bt.enableBluetooth();
		bt.setCommunicationCallback(this);
		injector = new Injector();

		notifyMsg("Implements \"Injektor\" by Ganthet");
		device = getDeviceByName("HC-05");

		Thread delayer = new Thread(new Runnable() {
			@Override
			public void run() {
				try {Thread.sleep(3000);}
				catch (InterruptedException e) {e.printStackTrace();}
				if (device != null);
				bt.connectToDevice(device);
			}
		});
		delayer.start();

		return START_NOT_STICKY;
	}

	public BluetoothDevice getDeviceByName(String name) {
		List<BluetoothDevice> paired = bt.getPairedDevices();

		for (BluetoothDevice dVice : paired) {
			if (name.equals(dVice.getName())) {
				notifyMsg("Connect to: " + dVice.getName());
				return dVice;
			}
		}
		return null;
	}

	public void notifyMsg(final String msg) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onConnect(BluetoothDevice device) {
		notifyMsg("Connected!");
	}

	@Override
	public void onDisconnect(BluetoothDevice device, String message) {}

	@Override
	public void onMessage(String message) {
		float xCrd, yCrd;
		message.replaceAll("\n", " ");
		String tmpMsg[] = message.split(" ");
		xCrd = Float.parseFloat(tmpMsg[0]);
		yCrd = Float.parseFloat(tmpMsg[1]);
		injector.injectTouch(xCrd, yCrd);
	}

	@Override
	public void onError(String message) {}

	@Override
	public void onConnectError(BluetoothDevice device, String message) {}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public static void start(Context contxt) {
		contxt.startService(new Intent(contxt, BTService.class));
	}

	private class Injector extends Thread {
		private Instrumentation instrument;
		private int scrWdth;
		private int scrHght;
		private Boolean waiting = true;
		private final Object lock = new Object();
		private TouchPos tPos = new TouchPos();

		Injector() {
			scrWdth = Resources.getSystem().getDisplayMetrics().widthPixels;
			scrHght = Resources.getSystem().getDisplayMetrics().heightPixels;
			instrument = new Instrumentation();
			this.start();
		}

		public void run() {
			while (true) {
				synchronized (lock) {
					try {
						if (waiting) {
							lock.wait();
						}
					} catch (InterruptedException e) {e.printStackTrace();}

					instrument.sendPointerSync(MotionEvent.obtain(	SystemClock.uptimeMillis(),
					                           SystemClock.uptimeMillis(),
					                           MotionEvent.ACTION_DOWN,
					                           tPos.getxPos(),
					                           tPos.getyPos(),
					                           0));
					instrument.sendPointerSync(MotionEvent.obtain(	SystemClock.uptimeMillis(),
					                           SystemClock.uptimeMillis(),
					                           MotionEvent.ACTION_UP,
					                           tPos.getxPos(),
					                           tPos.getyPos(),
					                           0));

					waiting = true;
				}
			}
		}

		public void injectTouch(float xPos, float yPos) {
			synchronized (lock) {
				tPos.setRelativePos(xPos, yPos);
				if (waiting) {
					waiting = false;
					lock.notify();
				}
			}
		}

		private class TouchPos {
			private float xPos = 360;
			private float yPos = 200;

			void setRelativePos(float x, float y) {
				xPos = scrWdth * (x / 100f);
				yPos = scrHght * (y / 100f);
			}

			float getxPos() {
				return xPos;
			}

			float getyPos() {
				return yPos;
			}
		}
	}
}
