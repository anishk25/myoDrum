package com.example.drums;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import android.support.v7.app.ActionBarActivity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {
	private SoundPool soundPool;
	private int soundIDSnare;
	private int soundIDhh; 
	boolean snare_loaded = false;
	boolean hh_loaded = false;
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Set the hardware buttons to control the music
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// Load the sound
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				System.out.println("yolo");
				snare_loaded = true;
				hh_loaded = true; 
			}
		});
		/*soundPoolhh = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPoolhh.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				System.out.println("yolo");
				hh_loaded = true;
			}
		});*/
		soundIDSnare = soundPool.load(this, R.raw.shortsnare, 1);
		soundIDhh = soundPool.load(this, R.raw.shorthh, 1);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button snare = (Button) findViewById(R.id.button1);

		snare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// Getting the user sound settings
				AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				float actualVolume = (float) audioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);
				float maxVolume = (float) audioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				float volume = actualVolume / maxVolume;
				// Is the sound loaded already?
				if (snare_loaded) {
					soundPool.play(soundIDSnare, volume, volume, 1, 0, 1f);
					Log.e("Test", "Played sound");
				}
			}
		});

		Button hh = (Button) findViewById(R.id.button2);

		hh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// Getting the user sound settings
				AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				float actualVolume = (float) audioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);
				float maxVolume = (float) audioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				float volume = maxVolume;
				// Is the sound loaded already?
				Log.e("Test", String.valueOf(hh_loaded));
				if (hh_loaded) {
					soundPool.play(soundIDhh, volume, volume, 1, 0, 1f);
					Log.e("Test", "Played sound");
				}
			}
		});
	}

	public void onPrepared(MediaPlayer player) {
		player.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
