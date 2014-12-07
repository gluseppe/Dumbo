package com.elephant.proga.dumbo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by gluse on 08/10/14.
 */
public class Particle {

    private int futureTime;
    private LatLng position;

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    private double altitude;


    public Particle(LatLng position, int futureTime) {
        this.position = position;
        this.futureTime = futureTime;
    }

    public Particle(LatLng position, int futureTime, double altitude) {
        this(position,futureTime);
        this.altitude = altitude;
    }


    public int getFutureTime() {
        return futureTime;
    }

    public void setFutureTime(int futureTime) {
        this.futureTime = futureTime;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }




}
