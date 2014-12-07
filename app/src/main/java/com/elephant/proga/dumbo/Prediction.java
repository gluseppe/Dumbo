package com.elephant.proga.dumbo;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by gluse on 05/12/14.
 */
public class Prediction {

    private String flightID;
    private Hashtable<Integer,ArrayList<Particle>> rawPrediction;
    private ArrayList<String> legs;

    Prediction() {

    }

    Prediction(String flight_id) {
        this();
        this.flightID = flight_id;
    }

    Prediction(String flight_id, Hashtable<Integer, ArrayList<Particle>> rawPrediction) {
        this(flight_id);
        this.rawPrediction = rawPrediction;
    }

    Prediction(String flight_id, Hashtable<Integer, ArrayList<Particle>> rawPrediction, ArrayList<String> legs) {
        this(flight_id,rawPrediction);
        this.legs = legs;
    }

    public ArrayList<String> getLegs() {
        return legs;
    }

    public void setLegs(ArrayList<String> legs) {
        this.legs = legs;
    }

    public String getFlightID() {
        return flightID;
    }

    public void setFlightID(String flightID) {
        this.flightID = flightID;
    }

    public Hashtable<Integer, ArrayList<Particle>> getRawPrediction() {
        return rawPrediction;
    }

    public void setRawPrediction(Hashtable<Integer, ArrayList<Particle>> rawPrediction) {
        this.rawPrediction = rawPrediction;
    }





}
