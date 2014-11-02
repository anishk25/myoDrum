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
	private int calibrate_num = 0;
	
	private int previous_pitch;
	private DRUM_STATE drum_state;
	private float LEFT_YAW,RIGHT_YAW,MIDDLE_YAW,TOPLEFT_YAW,TOPRIGHT_YAW,LOW_PITCH,TOPMIDDLE_PITCH,
		TOPLEFT_PITCH, TOPRIGHT_PITCH;
	private DrumHitListener drumListener;
	private TextView tvReadings,tvCalib,tvDrumType;
	
	public MyoDrum(DrumHitListener dListener, TextView tvReadings,TextView calib,TextView tvDrumType){
		this.drumListener = dListener;
		this.tvReadings = tvReadings;
		this.tvCalib = calib;
		this.tvDrumType = tvDrumType;
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
			 switch (calibrate_num) {
			 case 0:
				 MIDDLE_YAW = yaw;
				 break;
			 case 1:
				 LEFT_YAW = yaw;
				 break;
			 case 2:
				 TOPLEFT_YAW = yaw;
				 TOPLEFT_PITCH = pitch;
				 break;
			 case 3:
				 TOPMIDDLE_PITCH = pitch;
				 break;
			 case 4:
				 TOPRIGHT_YAW = yaw;
				 break;
			 case 5:
				 RIGHT_YAW = yaw;
				 break;
			 }
			 myo.vibrate(Myo.VibrationType.MEDIUM);
			 calibrate_num++;
			 if(calibrate_num >= 6) {
				 calibrated = true;
				 drum_state = DRUM_STATE.GOING_DOWN;
				 tvCalib.setText("middle_yaw:" + MIDDLE_YAW + " left_yaw:" + LEFT_YAW + " right_yaw:" + RIGHT_YAW + " top_left:" + TOPLEFT_YAW + " top_right:" + TOPRIGHT_YAW );
			 }			 
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
        myo.requestRssi();
        
       tvReadings.setText("Roll:" + roll + " Pitch:" + pitch + " Yaw:" + yaw);
        if(calibrated){
        	if(drum_state == DRUM_STATE.GOING_DOWN && pitch < previous_pitch){
        		drum_state = DRUM_STATE.GOING_UP;
        		if(pitch >= TOPMIDDLE_PITCH) { //top
        			if(yaw >= TOPLEFT_YAW) {
        				drumListener.OnDrumHit(1);
        				tvDrumType.setText("LEFT CYMBAL");
            		}
        			else if(yaw <= TOPRIGHT_YAW) {
        				drumListener.OnDrumHit(2);
        				tvDrumType.setText("RIGHT CYMBAL");
        			}
        			else {
        				drumListener.OnDrumHit(3);
        				tvDrumType.setText("MIDDLE CYMBAL");
        			}
        		}
        		else if(pitch <= LOW_PITCH + 10){ //bottom
        			if(yaw >= LEFT_YAW) {
        				drumListener.OnDrumHit(0);
        				tvDrumType.setText("LEFT SNARE");
            		}
        			else if(yaw <= RIGHT_YAW) {
        				drumListener.OnDrumHit(0);
        				tvDrumType.setText("RIGHT SNARE");
        			}
        		}
        		
        	}else if(drum_state == DRUM_STATE.GOING_UP && pitch > previous_pitch){
                drum_state = DRUM_STATE.GOING_DOWN;
            }
        }
        previous_pitch = pitch;
        
     }
	
	 
	
	
	 
	 
	 
	 
 
}
