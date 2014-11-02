package com.example.drums;


import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;


public class MainActivity extends ActionBarActivity implements DrumHitListener  {
	private SoundPool soundPool;
	boolean ride_loaded = false;
	boolean crash_loaded = false;
	boolean snare_loaded = false;
	boolean hh_loaded = false;
	
	private int [] soundIDS = new int [5];
	private static final String TAG = "MainActivity";
	private MyoDrum myoDrum;
	private Hub hub;
	private static final int REQUEST_ENABLE_BT = 1;
	private TextView textReadings, tvCalib,tvRSSI;
	
	private long start_time,end_time;
	private AudioManager audioManager;
	float actualVolume,maxVolume ,volume;
	//private final float [] sound_rates = {1.5f,1.4f,2f,2f,1.3f};
	private final float [] sound_rates = {2f,2f,2f,2f,2f};

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Set the hardware buttons to control the music
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// Load the sound
		initSounds();
		loadSounds();
		textReadings = (TextView)findViewById(R.id.myoText);
		tvCalib = (TextView)findViewById(R.id.tvCalib);
		tvRSSI = (TextView)findViewById(R.id.tvRSSI);
		myoDrum = new MyoDrum(this,textReadings,tvCalib,tvRSSI);
		
		loadMyoHub();
	}
	
	
	public void initSounds(){
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		
		actualVolume = (float) audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxVolume = (float) audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = actualVolume / maxVolume;
	}
	public void loadMyoHub(){
		hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        

        // Next, register for DeviceListener callbacks.
        start_time = System.nanoTime();
        Hub.getInstance().addListener((DeviceListener)myoDrum);
	}
	public void loadSounds(){
		soundIDS[0] = soundPool.load(this, R.raw.shortsnare, 1);
		soundIDS[1] = soundPool.load(this, R.raw.shorthh, 1);
		soundIDS[2] = soundPool.load(this,  R.raw.shortride, 1);
		soundIDS[3] = soundPool.load(this, R.raw.shortcrash, 1);
		soundIDS[4] = soundPool.load(this, R.raw.floortom,1);
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
        int id = item.getItemId();
        if (R.id.action_scan == id) {
           Hub.getInstance().pairWithAnyMyo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	

	@Override
	public void OnDrumHit(int drumNumber) {
		soundPool.play(soundIDS[drumNumber], volume, volume, 1, 0, sound_rates[drumNumber]);
	}
	
	
	
	 @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        // We don't want any callbacks when the Activity is gone, so unregister the listener.
	        Hub.getInstance().removeListener(myoDrum);

	        if (isFinishing()) {
	            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
	            Hub.getInstance().shutdown();
	        }
	   }
	 
	 @Override
	    protected void onResume() {
	        super.onResume();

	        // If Bluetooth is not enabled, request to turn it on.
	        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
	            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	        }
	    }
	 
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        // User chose not to enable Bluetooth, so exit.
	        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
	            finish();
	            return;
	        }
	        super.onActivityResult(requestCode, resultCode, data);
	    }

	

}
