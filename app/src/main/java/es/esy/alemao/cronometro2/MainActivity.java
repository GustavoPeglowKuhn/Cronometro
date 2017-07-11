package es.esy.alemao.cronometro2;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
	private static final int interval = 5;

	Button btn_start_stop, btn_reset, btn_lap;
	TextView txt_time, txt_media, txt_laps;
	ListView listLaps;
	Timer timer;

	static ArrayList list = new ArrayList();

	static boolean contando = false, atualizando = false, lap = false, reset = false;
	static long baseTime=0, stopTime=0, totalTime=0;
	static int laps = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listLaps = (ListView) findViewById(R.id.list);

		txt_time = (TextView) findViewById(R.id.txt_time);
		txt_media = (TextView) findViewById(R.id.txt_media);
		txt_laps = (TextView) findViewById(R.id.txt_laps);

		btn_start_stop = (Button) findViewById(R.id.btn_start_stop);
		btn_reset = (Button) findViewById(R.id.btn_reset);
		btn_lap = (Button) findViewById(R.id.btn_lap);

		btn_start_stop.setOnClickListener(this);
		btn_reset.setOnClickListener(this);
		btn_lap.setOnClickListener(this);

		btn_reset.setEnabled(false);
		btn_lap.setEnabled(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(laps == 0) txt_media.setText(R.string.t0);
		else txt_media.setText(formatTime(totalTime/laps));
		listLaps.setAdapter(new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, list));

		if(contando){
			if(timer != null) timer.cancel();
			timer = new Timer();
			timer.schedule(new MyTimerTask(), interval, interval);
			atualizando = false;
		}else{
			txt_time.setText(formatTime(stopTime));
		}

		txt_laps.setText(String.valueOf(laps));
		btn_lap.setEnabled(lap);
		btn_reset.setEnabled(reset);
	}
	@Override
	protected void onPause() {
		super.onPause();
		if(timer != null) timer.cancel();
	}

	@Override
	public void onClick(View view){
		switch(view.getId()){
			case R.id.btn_start_stop:
				if(contando){    //stop
					contando = false;
					stopTime = SystemClock.elapsedRealtime()-baseTime;
					timer.cancel();
					timer.purge();
					txt_time.setText(formatTime(stopTime));
					btn_start_stop.setText(R.string.start);
				}else{            //start
					baseTime = SystemClock.elapsedRealtime()-stopTime;
					contando = true;

					timer = new Timer();
					timer.schedule(new MyTimerTask(), interval, interval);
					atualizando = false;

					btn_start_stop.setText(R.string.stop);
					btn_reset.setEnabled(true);
					btn_lap.setEnabled(true);
					lap = true;
					reset = true;
				}
				break;
			case R.id.btn_reset:
				baseTime = SystemClock.elapsedRealtime();
				if(!contando){
					btn_reset.setEnabled(false);
					btn_lap.setEnabled(false);
					lap = false;
					reset = false;
					txt_time.setText(R.string.t0);
				}
				txt_media.setText(R.string.t0);
				laps = 0;
				stopTime = 0;
				totalTime = 0;
				list = new ArrayList();
				listLaps.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));
				txt_laps.setText("0");
				break;
			case R.id.btn_lap:
				long lapTime;
				if(!contando){
					btn_lap.setEnabled(false);
					lap = false;
					lapTime = stopTime;
					txt_time.setText(R.string.t0);
				}else lapTime = SystemClock.elapsedRealtime()-baseTime;
				baseTime += lapTime;	//baseTime = SystemClock.elapsedRealtime();
				stopTime = 0;

				totalTime += lapTime;
				list.add(0, ""+(++laps)+"     -     "+formatTime(lapTime)+"     -     "+formatTime(totalTime));
				txt_media.setText(formatTime(totalTime/laps));
				listLaps.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));
				txt_laps.setText(String.valueOf(laps));
				break;
		}
	}

	String formatTime(long time){
		long h, m, s, ms;
		ms = time%1000;
		time/=1000;
		s = time%60;
		time/=60;
		m = time%60;
		h=time/60;
		return ""+(h>0?(h+":"):"")+(m<10?"0":"")+m+":"+(s<10?"0":"")+s+"."+(ms<100?(ms<10?"00":"0"):"")+ms;
		//return String.format("%02d:%02d:%02d.%03d", h, m, s, ms );
	}

	class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(!atualizando){
						atualizando = true;
						if(contando){
							txt_time.setText(formatTime(SystemClock.elapsedRealtime()-baseTime));
						}else{
							timer.cancel();
							timer.purge();
						}
						atualizando = false;
					}
				}
			});
		}
	}
}
