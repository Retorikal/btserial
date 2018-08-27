package com.retorikal.btserial;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.retorikal.btserial.BTService;

public class Interface extends AppCompatActivity {

	TextView log;
	Thread btnActTrd;
	boolean doWait = true;
	TouchPost tPos = new TouchPost();
	Instrumentation m_Instrumentation = new Instrumentation();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_interface);
		log = (TextView) findViewById(R.id.logView);

		btnActTrd = new Thread(new Runnable(){
			@Override
			public void run(){
				while(true){
					synchronized (tPos){
						try {
							if (doWait) {
								tPos.wait();
							}
						} catch (InterruptedException e) {e.printStackTrace();}
						m_Instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_B);
						doWait=true;
					}
				}
			}
		});
		btnActTrd.start();
		BTService.start(this);
	}

	public void btnAct(View view){
		final View viewFinal = view;
		this.runOnUiThread(new Runnable() {
			@Override
			public void run(){
				log.append(Integer.toString(viewFinal.getId())+'\n');
			}
		});
	}

	public void clrLog(View view){
		final View viewFinal = view;
		this.runOnUiThread(new Runnable() {
			@Override
			public void run(){
				log.setText("Cleared \n");
			}
		});
	}

	public void btnInject(View view){
		synchronized(tPos){
			tPos.setPos(25,25);
			doWait=false;
			tPos.notify();
			log.append("Notified! \n");
		}
	}
}

class TouchPost{
	private int xPos;
	private int yPos;

	void setPos(int x,int y){
		xPos=x;
		yPos=y;
	}
}


