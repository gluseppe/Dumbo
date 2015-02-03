package com.elephant.proga.dumbo;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.elephant.proga.dumbo.receivers.PredictionReceiver;

/**
 * Created by gluse on 25/11/14.
 */
public class SlidingListener implements View.OnTouchListener {

    //this is the target view to modify in response to the user interaction
    ViewGroup mViewGroup;
    PredictionReceiver mPredictionReceiver;
    String mAircraftID = null;

    public SlidingListener(ViewGroup vg, PredictionReceiver pr) {
        assert vg != null : "viewgroup can't be null";
        assert pr != null : "prediction receiver can't be null";
        mViewGroup = vg;
        mPredictionReceiver = pr;


    }


    public void setAircraftID(String s) {
        assert s != null : "aircraft id can't be null";
        this.mAircraftID = s;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch(action) {
            case MotionEvent.ACTION_DOWN: {
                //finger on the view, switch to sliding mode
                Log.d("GESTURE",String.format("DOWN %f pointer count is %d",event.getX(),event.getPointerCount()));

                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                //finger moving on view, calculate distance and highlight corresponding element
                event.getX();
                Log.d("GESTURE",String.format("MOVING %f pointer count is %d",event.getX(),event.getPointerCount()));
                return false;

            }
            case MotionEvent.ACTION_UP: {
                Log.d("GESTURE","finger released mmmhh");
                //pr.setPredictionParams(marker.getTitle(), 300, 1, USEDPREDICTION==RAWPREDICTIONTYPE);
                //this.predictioThread = new Thread(pr);
                //this.predictioThread.start();
                return true;

            }
            case MotionEvent.ACTION_CANCEL : {
                Log.d("GESTURE",String.format("%d gesture canceled",v.getId()));
                return true;
            }
        }

        return true;

    }
}
