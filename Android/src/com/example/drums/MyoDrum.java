package com.example.drums;

import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;

public class MyoDrum extends AbstractDeviceListener  {
	private Arm mArm = Arm.UNKNOWN;
	private boolean calibrated = false;
	private int roll,pitch,yaw;
	
	private int previous_pitch;
	private DRUM_STATE drum_state;
	private float LEFT_YAW,RIGHT_YAW,MIDDLE_YAW,LOW_PITCH,TOP_PITCH;
	private DrumHitListener drumListener;
	private TextView tvReadings,tvCalib;
	
	public MyoDrum(DrumHitListener dListener, TextView tvReadings,TextView calib){
		this.drumListener = dListener;
		this.tvReadings = tvReadings;
		this.tvCalib = calib;
	}
	
	 @Override
     public void onConnect(Myo myo, long timestamp) {
         // Set the text color of the text view to cyan when a Myo connects.
		 tvReadings.setTextColor(Color.BLACK);
		 myo.vibrate(Myo.VibrationType.LONG);
       
     }
	 
	 @Override
     public void onDisconnect(Myo myo, long timestamp) {
         // Set the text color of the text view to red when a Myo disconnects.
		 tvReadings.setTextColor(Color.RED);
     }

	
	@Override
    public void onArmRecognized(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
        mArm = arm;
        
    }
	
	 @Override
     public void onArmLost(Myo myo, long timestamp) {
         mArm = Arm.UNKNOWN;
     }
	 
	 @Override
	 public void onPose (Myo myo, long timestamp, Pose pose){
		 if(pose == Pose.FIST && !calibrated){
			 calibrated = true;
			 drum_state = DRUM_STATE.GOING_DOWN;
			 LOW_PITCH = pitch;
			 TOP_PITCH = LOW_PITCH + 40;
			 MIDDLE_YAW = yaw;
			 if(mArm == Arm.RIGHT){
				 LEFT_YAW = MIDDLE_YAW + 45;
				 RIGHT_YAW = MIDDLE_YAW - 10;
			 }else{
				 LEFT_YAW = MIDDLE_YAW + 10;
				 RIGHT_YAW = MIDDLE_YAW - 45;
			 }
			 myo.vibrate(Myo.VibrationType.MEDIUM);
			 tvCalib.setText("Calibrated!");
		 }
	 }
	 
	 
	 @Override
     public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
         // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
        float l_roll = (float) Math.toDegrees(Quaternion.roll(rotation));
        float l_pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
        float l_yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));  
        
        roll = (int)((l_roll + (float)Math.PI)/(Math.PI * 2.0f));
        pitch= (int)((l_pitch + (float)Math.PI/2.0f)/Math.PI);
        yaw = (int)((l_yaw + (float)Math.PI)/(Math.PI * 2.0f));

        
        tvReadings.setText("Roll:" + roll + " Pitch:" + pitch + " Yaw:" + yaw);
        if(calibrated){
        	if(drum_state == DRUM_STATE.GOING_DOWN && pitch < previous_pitch){
        		drum_state = DRUM_STATE.GOING_UP;
        		drumListener.OnDrumHit(0);
        	}else if(drum_state == DRUM_STATE.GOING_UP && pitch > previous_pitch){
                drum_state = DRUM_STATE.GOING_DOWN;
            }
        }
        previous_pitch = pitch;
        
     }
	 
	
	
	 
	 
	 
	 
 
}
