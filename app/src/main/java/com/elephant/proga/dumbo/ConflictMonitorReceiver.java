package com.elephant.proga.dumbo;

import android.util.Log;

import com.elephant.proga.dumbo.interfaces.ConflictHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by gluse on 21/01/15.
 */
public class ConflictMonitorReceiver extends Receiver {

    private ConflictHandler handler;
    private final static int DT = 0;
    private final static int NSTEPS =0;
    private final static boolean RAW = true;
    private final static String MONITOR_COMMAND = "MONITOR_ME";
    private HashSet conflictingFlights;

    public ConflictMonitorReceiver(ConflictHandler handler, String source, long sleeptime) {
        super(source, sleeptime);
        this.handler = handler;
        setMonitorParams();

    }

    private void setMonitorParams() {

        this.source = this.source + buildRequestString(DT, NSTEPS, RAW);
    }

    private String buildRequestString(int dt, int nsteps, boolean rawPrediction) {
        String dt_string = String.valueOf(dt);
        String nsteps_string = String.valueOf(nsteps);

        return "?" + "flight_id="+ MONITOR_COMMAND + "&" + "deltaT=" + dt_string + "&" + "nsteps=" + nsteps_string + "&" + "raw=" + rawPrediction;
    }


    private boolean anyConflict(JSONObject jcontent) {
        return (jcontent.length() > 0);
    }

    private HashSet getFlightsFromJSON(JSONObject jcontent) {
        HashSet h = new HashSet();
        Iterator i = jcontent.keys();
        while(i.hasNext()) {
            try {
                JSONArray jarr = jcontent.getJSONArray((String) i.next());
                for(int j=0;j<jarr.length();j++)
                    h.add((String) jarr.get(j));
            } catch (JSONException e) {
                //e.printStackTrace();
                Log.d("MONITOR_ME","Monitor Thread was interrupted. No need to sleep");
                return null;
            };
        }

        return h;
    }


    @Override
    public void run() {

        while(!Thread.interrupted()) {

            Log.d("MONITOR_ME","MONITOR THREAD IS RUNNING");

            this.content = this.GET();
            //Log.d("RECEIVER", String.format("ASKING CONTENT"));
            if (this.content != null) {
                Log.d("MONITOR_ME",String.format("Conflict detector said: %s",this.content));
                jcontent = this.toJSON(content);
                if(anyConflict(jcontent)) {
                    conflictingFlights = getFlightsFromJSON(jcontent);
                    if (conflictingFlights != null)
                        this.handler.onConflictDetected(conflictingFlights);
                }
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
