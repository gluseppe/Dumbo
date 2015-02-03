package com.elephant.proga.dumbo.receivers;

import android.util.Log;

import com.elephant.proga.dumbo.interfaces.SelfStatusHandler;

/**
 * Created by gluse on 03/10/14.
 */
public class SelfStatusReceiver extends Receiver {

    SelfStatusHandler handler = null;

    public interface UISelfUpdater {
        public abstract void onSelfUpdate();
    }

    public SelfStatusReceiver(SelfStatusHandler handler, String source, long sleepingTime) {
        super(source, sleepingTime);
        this.handler = handler;

    }

    @Override
    public void run() {


        while(!Thread.interrupted()) {


            this.content = this.GET();

            //Log.d("RECEIVER", String.format("ASKING CONTENT"));
            if (this.content != null) {
                Log.d("SELF STATUS RECEIVER",String.format("content from server was: %s",content));
                jcontent = this.toJSON(content);
                this.handler.onSelfStatusUpdate(jcontent);
            }
            else
                Log.d("RECEIVER", String.format("THERE ARE PROBLEMS RECEIVING DATA PLEASE CHECK CONNECTIONS AND IP"));




            try {
                Thread.sleep(sleepingTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }


        }
    }





}
