package com.elephant.proga.dumbo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by gluse on 08/10/14.
 */
public class Particle {

    private int futureTime;
    private LatLng position;


    public Particle(LatLng position, int futureTime) {
        this.position = position;
        this.futureTime = futureTime;
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
