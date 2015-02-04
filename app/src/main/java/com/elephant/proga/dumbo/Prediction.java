package com.elephant.proga.dumbo;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import com.google.maps.android.SphericalUtil;

/**
 * Created by gluse on 05/12/14.
 */
public class Prediction {

    private String flightID;
    private Hashtable<Integer,ArrayList<Particle>> rawPrediction;
    private ArrayList<String> legs;
    //this will contain a "centroid" particle representing the average quote and position for
    //that group of particles
    private Hashtable<Integer, Particle> altitudes;


    Prediction() {

    }

    public Prediction(String flight_id) {
        this();
        this.flightID = flight_id;
    }

    public Prediction(String flight_id, Hashtable<Integer, ArrayList<Particle>> rawPrediction) {
        this(flight_id);
        this.rawPrediction = rawPrediction;
    }

    public Prediction(String flight_id, Hashtable<Integer, ArrayList<Particle>> rawPrediction, ArrayList<String> legs) {
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

    //returns a hashtable with time as keys, and particle as value. the particle represents a centroid
    //of all the particles we have for that time
    public Hashtable<Integer,Particle> getAltitudes() {
        if (this.rawPrediction != null) {
            Hashtable<Integer,Particle> altitudes = new Hashtable<>();

            Iterator times_iter = this.rawPrediction.keySet().iterator();
            while(times_iter.hasNext())
            {
                double avg_altitude = 0.0;
                Particle centroid = null;
                int the_time = (int) times_iter.next();

                ArrayList ps = (ArrayList) this.rawPrediction.get(the_time);
                Iterator ps_iter = ps.iterator();
                int n_particles = ps.size();
                while(ps_iter.hasNext())
                {
                    Particle p = (Particle) ps_iter.next();
                    avg_altitude = avg_altitude + p.getAltitude();
                    if (centroid == null)
                        centroid = p;
                    else
                    {
                        LatLng centroid_pos = SphericalUtil.interpolate(centroid.getPosition(),p.getPosition(),0.5);
                        centroid.setPosition(centroid_pos);
                    }

                }
                avg_altitude = avg_altitude / n_particles;
                centroid.setAltitude(avg_altitude);
                altitudes.put(the_time,centroid);
            }

            this.altitudes = altitudes;
            return this.altitudes;
        }

        return null;
    }






}
