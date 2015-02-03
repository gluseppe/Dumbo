package com.elephant.proga.dumbo.receivers;

import android.util.Log;

import com.elephant.proga.dumbo.Particle;
import com.elephant.proga.dumbo.Prediction;
import com.elephant.proga.dumbo.interfaces.PredictionHandler;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by gluse on 06/10/14.
 */
public class PredictionReceiver extends Receiver {

    //source contiene http://127.0.0.1:8080/prediction

    public static final int PREDICTION_FAILED_SERVER_RETURNED_NULL = 0;
    public static final int PREDICTION_FAILED_STRING_TO_JSON = 1;
    public static final int PREDICTION_FAILED_JSON_TO_OBJECT = 2;

    private ArrayList flights;
    private String flight;
    private boolean rawPrediction;
    private Hashtable<Integer,ArrayList<Particle>> particles;
    private int dt;
    private int nsteps;
    private PredictionHandler handler;

    public PredictionReceiver(PredictionHandler handler, String source) {
        super(source, -1);
        this.handler = handler;
    }

    public boolean setFlights(ArrayList flightsList) {

        this.flights = flightsList;
        //this.source = this.source + buildRequestString(flightsList);
        this.rawPrediction = false;
        return true;

    }


    public boolean setPredictionParams(String flight, int dt, int nsteps, boolean rawPrediction) {
        this.flight = flight;
        this.rawPrediction = rawPrediction;
        this.source = this.source + buildRequestString(flight, dt, nsteps, rawPrediction);
        this.dt = dt;
        this.nsteps = nsteps;
        return true;
    }

    private String buildRequestString(String flight, int dt, int nsteps, boolean rawPrediction) {
        String dt_string = String.valueOf(dt);
        String nsteps_string = String.valueOf(nsteps);

        return "?" + "flight_id="+ flight + "&" + "deltaT=" + dt_string + "&" + "nsteps=" + nsteps_string + "&" + "raw=" + rawPrediction;
    }

    private Hashtable<String,Prediction> JSONtoRawPredictions(JSONObject jcontent) {


        String flight;
        Iterator<String> flights = jcontent.keys();
        Hashtable<Integer,ArrayList<Particle>> rawPrediction = new Hashtable<Integer, ArrayList<Particle>>();
        JSONArray particles_and_legs;
        JSONObject particles_and_times;


        Hashtable<String,Prediction> predictions = new Hashtable<String, Prediction>();

        while (flights.hasNext()) {
            flight = flights.next();
            Log.d("JTEST", flight);

            //try to access the prediction information for the flight in iteration
            try {
                particles_and_legs = jcontent.getJSONArray(flight);
                particles_and_times = particles_and_legs.getJSONObject(0);
                Iterator times = particles_and_times.keys();
                JSONArray jparticles;
                while (times.hasNext()) {



                    Integer time = Integer.valueOf((String) times.next());
                    jparticles = particles_and_times.getJSONArray(String.valueOf(time.intValue()));

                    JSONArray jparticle;
                    ArrayList particles = new ArrayList();

                    for (int i = 0; i < jparticles.length(); i++) {

                        jparticle = jparticles.getJSONArray(i);
                        Particle particle = new Particle(new LatLng(jparticle.getDouble(1), jparticle.getDouble(0)), time.intValue(), jparticle.getDouble(2));
                        particles.add(particle);
                        particle = null;
                    }

                    rawPrediction.put(time, particles);
                    particles = null;

                }

            } catch (JSONException e) {
                e.printStackTrace();
                return null;

            }

            predictions.put(flight,new Prediction(flight, rawPrediction));



        }

        return predictions;

    }


    @Override
    public void run() {

        Log.d("PREDICTION RECEIVER", String.format("ASKING PREDICTION FOR FLIGHT "));
        JSONObject jtimes = null;
        JSONArray jtimesraw = null;
        JSONArray jparticles = null;
        JSONArray jlegs = null;
        JSONArray jtimesandlegs = null;

        this.content = GET();
        if (this.content != null) {
            if (this.rawPrediction)
            {
                JSONObject jpred = this.toJSON(content);
                if (jpred == null)
                    this.handler.onPredictionFailed(PredictionReceiver.PREDICTION_FAILED_STRING_TO_JSON);
                else {
                    Hashtable<String, Prediction> predictions = JSONtoRawPredictions(jpred);
                    if (predictions == null)
                        this.handler.onPredictionFailed(PredictionReceiver.PREDICTION_FAILED_JSON_TO_OBJECT);
                    else {
                        this.handler.onPredictionReceived(predictions);
                    }
                }


            }

        }
        else
            this.handler.onPredictionFailed(PredictionReceiver.PREDICTION_FAILED_SERVER_RETURNED_NULL);

    }
}
