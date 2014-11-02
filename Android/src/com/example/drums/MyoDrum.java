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
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;

public class MyoDrum extends AbstractDeviceListener  {
	private Arm mArm = Arm.UNKNOWN;
	private boolean calibrated = false;
	private int roll,pitch,yaw;
	private int calibrate_num = 0;
	double gyro_x = 1.0;
	double gyro_y = 1.0;
	double abs_acceleration = 0;
	
	private int previous_pitch;
	private DRUM_STATE drum_state;
	private int LEFT_YAW,RIGHT_YAW,LOW_PITCH,TOP_LEFT_YAW,TOP_RIGHT_YAW,MIDDLE_YAW;
	
	private DrumHitListener drumListener;
	private TextView tvReadings,tvCalib,tvDrumType;
	
	private KalmanFilter kalmanFilter1,kalmanFilter2;
	
	
	
	
	public MyoDrum(DrumHitListener dListener, TextView tvReadings,TextView calib,TextView tvDrumType){
		this.drumListener = dListener;
		this.tvReadings = tvReadings;
		this.tvCalib = calib;
		this.tvDrumType = tvDrumType;
		kalmanFilter1 = new KalmanFilter();
		kalmanFilter2 = new KalmanFilter();
	}
	
	public void resetCalib(){
		calibrate_num = 0;
		calibrated = false;
		tvCalib.setText("Not Calibrated");
	}
	
	 @Override
     public void onConnect(Myo myo, long timestamp) {
         // Set the text color of the text view to cyan when a Myo connects.
		 tvReadings.setTextColor(Color.BLACK);
		 myo.vibrate(Myo.VibrationType.SHORT);
         
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
				 LOW_PITCH = pitch;
				 MIDDLE_YAW = yaw;          
				 break;
			 case 1:
				 LEFT_YAW = yaw;
				 break;
			 case 2:
				 TOP_LEFT_YAW = yaw;
				 break;
			 case 3:
				 TOP_RIGHT_YAW = yaw;
				 break;
			 case 4:
				 RIGHT_YAW = yaw;
				 break;
			 }
			 myo.vibrate(Myo.VibrationType.SHORT);
			 calibrate_num++;
			 if(calibrate_num >= 5) {
				calibrated = true;
				drum_state = DRUM_STATE.GOING_DOWN;
				tvCalib.setText("Calibrated");
				kalmanFilter1.setAngle(yaw);
				kalmanFilter2.setAngle(pitch);
				calibrated = true;
			 }			 
		 }
	 }
	 
	 @Override
	 public void onGyroscopeData (Myo myo, long timestamp, Vector3 gyro){
		gyro_x  = gyro.x();	
		gyro_y = gyro.y();
	 }
	 
	/* @Override
	 public void onAccelerometerData (Myo myo, long timestamp, Vector3 accel){
		 abs_acceleration = Math.sqrt(Math.pow(accel.x(), 2)+Math.pow(accel.y(), 2)+Math.pow(accel.z(), 2)); 
	 }*/
	 
	 @Override
     public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
         // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
        float l_roll = (float) Math.toDegrees(Quaternion.roll(rotation));
        float l_pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
        float l_yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));  
        
		if (!calibrated) {
			roll = (int) ((l_roll + (float) Math.PI) / (Math.PI * 2.0f));
			yaw = (int) ((l_yaw + (float) Math.PI) / (Math.PI * 2.0f));
			pitch  = (int) ((l_pitch + (float) Math.PI / 2.0f) / Math.PI);
		} else {
			roll = (int) ((l_roll + (float) Math.PI) / (Math.PI * 2.0f));
			float temp_yaw = (int) ((l_yaw + (float) Math.PI) / (Math.PI * 2.0f));
			pitch  = (int) ((l_pitch + (float) Math.PI / 2.0f) / Math.PI);
			yaw = (int) kalmanFilter1.getAngle(temp_yaw, gyro_x / 131, 0.05);
			//pitch = (int) kalmanFilter1.getAngle(temp_pitch, gyro_y / 131, 0.1);
		}
       
        
      
      tvReadings.setText(" Roll:" + roll + " Pitch:" + pitch + " Yaw:" + yaw);
        if(calibrated){
        	if(drum_state == DRUM_STATE.GOING_DOWN && pitch < previous_pitch){
        		drum_state = DRUM_STATE.GOING_UP;
        		
        			if(pitch < LOW_PITCH + 4){
        				if(yaw > LEFT_YAW){
        					tvDrumType.setText("LEFT SNARE");
        					drumListener.OnDrumHit(0);
        				}else if(yaw < RIGHT_YAW){
        					tvDrumType.setText("RIGHT SNARE");
        					drumListener.OnDrumHit(4);
        				}
        			}else if(pitch > LOW_PITCH + 10){
        				if(yaw > TOP_LEFT_YAW){
        					tvDrumType.setText("LEFT CYMBAL");
        					drumListener.OnDrumHit(1);
        				}else if(yaw < TOP_RIGHT_YAW){
        					tvDrumType.setText("RIGHT CYMBAL");
        					drumListener.OnDrumHit(2);
        				}else{
        					tvDrumType.setText("MIDDLE CYMBAL");
        					drumListener.OnDrumHit(3);
        				}
        			
        		}
        			
        	}else if(drum_state == DRUM_STATE.GOING_UP && pitch > previous_pitch){
                drum_state = DRUM_STATE.GOING_DOWN;
            }
        	previous_pitch = pitch;
        }
        previous_pitch = pitch;
        
     }
	
	 
	
	
	 
	 
	 
	 
 
}
