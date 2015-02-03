package com.elephant.proga.dumbo.viewers;

import com.elephant.proga.dumbo.Particle;
import com.elephant.proga.dumbo.Prediction;
import com.elephant.proga.dumbo.interfaces.PredictionViewer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by gluse on 05/11/14.
 */
public class Particles implements PredictionViewer {

    private ArrayList shapes;
    private boolean predictionActive = false;

    public void PredictionParticles() {
        this.shapes = null;

    }



    public void drawPrediction(Object p, GoogleMap map, LatLng targetPosition, LatLng ownShipPosition, double ownShipAltitude) {
        Hashtable<String,Prediction> predictions = (Hashtable<String,Prediction>) p;
        if (this.shapes == null)
            this.shapes = new ArrayList();

        //Hashtable<Integer, ArrayList<Particle>> particles = (Hashtable<Integer, ArrayList<Particle>>) p;

        Enumeration<String> flights = predictions.keys();
        ArrayList coords = new ArrayList();
        while(flights.hasMoreElements()) {
            Prediction pred = predictions.get(flights.nextElement());
            Hashtable raw = pred.getRawPrediction();
            Enumeration times = raw.keys();
            while(times.hasMoreElements()) {
                //coords.addAll((ArrayList) times.nextElement());
                ArrayList particles = (ArrayList) raw.get((times.nextElement()));
                Iterator i = particles.iterator();
                while(i.hasNext())
                {
                    Particle particle = (Particle) i.next();
                    CircleOptions circleOptions = new CircleOptions()
                            .center(particle.getPosition())
                            .radius(200)
                            .fillColor(0x40ff0000)
                            .strokeColor(0x40ff0000)
                            .strokeWidth(1);


                    this.shapes.add(map.addCircle(circleOptions));
                }

            }

        }
        this.predictionActive = true;
    }


    //this method will be called when a visualization of a prediction becomes too old and needs to
    //be removed from the map, the map reference is passed as a parameter
    //The implementer class needs to hold a reference to all the items it placed in the map
    //in order to be able to remove them when this method is called
    public void removePrediction(GoogleMap map) {

        if (this.shapes != null)
        {
            Iterator i = this.shapes.iterator();
            while(i.hasNext()) {
                Circle c = (Circle) i.next();
                c.remove();
            }
        }

        this.predictionActive = false;

    }

    @Override
    public void updatePrediction(GoogleMap map, LatLng newTargetPosition, LatLng ownShipPosition, double ownShipAltitude) {
        if (this.predictionActive) {
            //update the prediction
        }

    }

    @Override
    public boolean isPredictionActive() {
        return this.predictionActive;
    }


}
