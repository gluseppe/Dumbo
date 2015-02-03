package com.elephant.proga.dumbo.receivers;

import android.util.Log;

import com.elephant.proga.dumbo.interfaces.TrafficStatusHandler;

/**
 * Created by gluse on 03/10/14.
 */
public class TrafficReceiver extends Receiver {
    private TrafficStatusHandler handler;

    public TrafficReceiver(TrafficStatusHandler handler, String source, long sleepingTime) {
        super(source, sleepingTime);
        this.handler = handler;
    }


    @Override
    public void run() {


        while(!Thread.interrupted()) {


            this.content = this.GET();
            //Log.d("RECEIVER", String.format("ASKING CONTENT"));
            if (this.content != null) {
                jcontent = this.toJSON(content);
                this.handler.onTrafficUpdate(jcontent);
            }
            else
                Log.d("TRAFFIC RECEIVER", String.format("THERE ARE PROBLEMS RECEIVING DATA PLEASE CHECK CONNECTIONS AND IP"));





            try {
                Thread.sleep(sleepingTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }


        }
    }



}
