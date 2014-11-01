// Copyright (C) 2013-2014 Thalmic Labs Inc.
// Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
#define _USE_MATH_DEFINES
#include <cmath>
#include <iostream>
#include <iomanip>
#include <stdexcept>
#include <string>
#include <algorithm>
#include <stdlib.h>

// The only file that needs to be included to use the Myo C++ SDK is myo.hpp.
#include <myo/myo.hpp>

// Classes that inherit from myo::DeviceListener can be used to receive events from Myo devices. DeviceListener
// provides several virtual functions for handling different kinds of events. If you do not override an event, the
// default behavior is to do nothing.
class DataCollector : public myo::DeviceListener {
    
    enum state{GOING_UP,GOING_DOWN};
private:
    int roll_us,pitch_us,yaw_us;
    int previous_pitch;
    state curr_state;
    bool calibrated;
    int left, mid, right;
    int low, high;
    int top_pitch,bottom_pitch;
    
public:
    DataCollector()
    {
        roll_us = 0;
        pitch_us = 0;
        yaw_us = 0;
        onArm = false;
        curr_state = GOING_DOWN;
        previous_pitch = 0;
        calibrated = false;
        left = 0;
        mid = 0;
        right = 0;
        low = 0;
        high = 0;
        top_pitch = 0;
        bottom_pitch = 0;
    }
    
    // onUnpair() is called whenever the Myo is disconnected from Myo Connect by the user.
    void onUnpair(myo::Myo* myo, uint64_t timestamp)
    {
        // We've lost a Myo.
        // Let's clean up some leftover state.
        roll_us = 0;
        pitch_us = 0;
        yaw_us = 0;
        onArm = false;
    }
    
    // onPose() is called whenever the Myo detects that the person wearing it has changed their pose, for example,
    // making a fist, or not making a fist anymore.
    void onPose(myo::Myo* myo, uint64_t timestamp, myo::Pose pose)
    {
        if (!calibrated) {
            if (pose == myo::Pose::fist) {
                low = pitch_us;
                mid = yaw_us;
                if(whichArm == myo::Arm::armRight) {
                    left = mid + 20;
                    right = mid - 10;
                }
                else
                {
                    left = mid + 10;
                    right = mid - 20;
                }
                high = low + 30;
                calibrated = true;
                std::cout << "CALIBRATED";
                myo->vibrate(myo::Myo::vibrationMedium);
            }
        }
        
    }
    
    // onOrientationData() is called whenever the Myo device provides its current orientation, which is represented
    // as a unit quaternion.
    void onOrientationData(myo::Myo* myo, uint64_t timestamp, const myo::Quaternion<float>& quat)
    {
        
        
        using std::atan2;
        using std::asin;
        using std::sqrt;
        using std::max;
        using std::min;
        
        // Calculate Euler angles (roll, pitch, and yaw) from the unit quaternion.
        float roll = atan2(2.0f * (quat.w() * quat.x() + quat.y() * quat.z()),
                           1.0f - 2.0f * (quat.x() * quat.x() + quat.y() * quat.y()));
        float pitch = asin(max(-1.0f, min(1.0f, 2.0f * (quat.w() * quat.y() - quat.z() * quat.x()))));
        float yaw = atan2(2.0f * (quat.w() * quat.z() + quat.x() * quat.y()),
                          1.0f - 2.0f * (quat.y() * quat.y() + quat.z() * quat.z()));
        int zone = 0;
        roll_us = (int)((roll + (float)M_PI)/(M_PI * 2.0f) * 100);
        pitch_us = (int)((pitch + (float)M_PI/2.0f)/M_PI * 100);
        yaw_us = (int)((yaw + (float)M_PI)/(M_PI * 2.0f) * 100);
        
        
        if(calibrated){
            if(curr_state == GOING_DOWN && pitch_us < previous_pitch){
                curr_state = GOING_UP;
                std::cout << "DRUM" << std::endl;
                if (abs(mid - yaw_us) > 30) {
                    if (mid > yaw_us)
                        zone++;
                    else
                        zone--;
                }
                int abs_yaw = zone * 100 + yaw_us; //the absolute yaw position
                
                int zone2 = 0;
                if (abs(low - pitch_us) > 40) {
                    if (low > pitch_us) {
                        zone2++;
                    }
                    else {
                        zone2--;
                    }
                }
                int abs_pitch = zone2 * 100 + pitch_us;
                
                if(whichArm == myo::Arm::armRight) {
                    if (abs_pitch - 20 <= low && abs_pitch + 20 >= low) {
                        if (abs_yaw > mid + 15) {
                            std::cout << "LEFT DRUM";
                        }
                        else if (abs_yaw < mid - 10){
                            std::cout << "RIGHT DRUM";
                        }
                    }
                    else {
                        if (abs_yaw > mid + 15) {
                            std::cout << "LEFT CYMBAL";
                        }
                        else if (abs_yaw < mid - 10){
                            std::cout << "RIGHT CYMBAL";
                        }
                    }
                    
                }
                else {
                    if (abs_pitch - 15 <= low && abs_pitch + 10 >= low) {
                        if (abs_yaw > mid + 10) {
                            std::cout << "LEFT DRUM";
                        }
                        else if (abs_yaw < mid - 15){
                            std::cout << "RIGHT DRUM";
                        }
                    }
                    else if (abs_pitch - 10 <= high && abs_pitch + 15 >= high) {
                        if (abs_yaw > mid + 10) {
                            std::cout << "LEFT CYMBAL";
                        }
                        else if (abs_yaw < mid - 15){
                            std::cout << "RIGHT CYMBAL";
                        }
                    }
                }
            }else if(curr_state == GOING_UP && pitch_us > previous_pitch){
                curr_state = GOING_DOWN;
            }
            
            previous_pitch = pitch_us;
        }
        
        
    }
    
    
    // onArmRecognized() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
    // arm. This lets Myo know which arm it's on and which way it's facing.
    void onArmRecognized(myo::Myo* myo, uint64_t timestamp, myo::Arm arm, myo::XDirection xDirection)
    {
        onArm = true;
        whichArm = arm;
    }
    
    // onArmLost() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
    // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
    // when Myo is moved around on the arm.
    void onArmLost(myo::Myo* myo, uint64_t timestamp)
    {
        onArm = false;
    }
    
    // There are other virtual functions in DeviceListener that we could override here, like onAccelerometerData().
    // For this example, the functions overridden above are sufficient.
    
    // We define this function to print the current values that were updated by the on...() functions above.
    void print()
    {
        /*if(calibrated) {
         rewind(stdout);
         std::cout << "Roll:" << roll_us << " Pitch:" << pitch_us << " Yaw:" << yaw_us << std::endl;
         std::cout << std::flush;
         }*/
    }
    
    // These values are set by onArmRecognized() and onArmLost() above.
    bool onArm;
    myo::Arm whichArm;
    
};



int main(int argc, char** argv)
{
    // We catch any exceptions that might occur below -- see the catch statement for more details.
    try {
        
        // First, we create a Hub with our application identifier. Be sure not to use the com.example namespace when
        // publishing your application. The Hub provides access to one or more Myos.
        myo::Hub hub("com.example.hello-myo");
        
        std::cout << "Attempting to find a Myo..." << std::endl;
        
        // Next, we attempt to find a Myo to use. If a Myo is already paired in Myo Connect, this will return that Myo
        // immediately.
        // waitForAnyMyo() takes a timeout value in milliseconds. In this case we will try to find a Myo for 10 seconds, and
        // if that fails, the function will return a null pointer.
        myo::Myo* myo = hub.waitForMyo(10000);
        
        // If waitForAnyMyo() returned a null pointer, we failed to find a Myo, so exit with an error message.
        if (!myo) {
            throw std::runtime_error("Unable to find a Myo!");
        }
        
        // We've found a Myo.
        std::cout << "Connected to a Myo armband!" << std::endl << std::endl;
        
        // Next we construct an instance of our DeviceListener, so that we can register it with the Hub.
        DataCollector collector;
        
        // Hub::addListener() takes the address of any object whose class inherits from DeviceListener, and will cause
        // Hub::run() to send events to all registered device listeners.
        hub.addListener(&collector);
        
        // Finally we enter our main loop.
        while (1) {
            // In each iteration of our main loop, we run the Myo event loop for a set number of milliseconds.
            // In this case, we wish to update our display 20 times a second, so we run for 1000/20 milliseconds.
            hub.run(1000/20);
            // After processing events, we call the print() member function we defined above to print out the values we've
            // obtained from any events that have occurred.
            collector.print();
        }
        
        // If a standard exception occurred, we print out its message and exit.
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
        std::cerr << "Press enter to continue.";
        std::cin.ignore();
        return 1;
    }
}
