package com.elephant.proga.dumbo.viewers;

import android.content.Context;
import android.graphics.Bitmap;

import com.elephant.proga.dumbo.Particle;
import com.elephant.proga.dumbo.Prediction;
import com.elephant.proga.dumbo.interfaces.PredictionViewer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by gluse on 05/11/14.
 */
public class Particles implements PredictionViewer {

    private ArrayList shapes;
    private ArrayList altitudeMarkers;
    private boolean predictionActive = false;
    private Context context;

    public Particles(Context context) {
        this.context = context;
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
            indicateAltitudes(map,pred);
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

        if (this.altitudeMarkers != null) {
            Iterator k = this.altitudeMarkers.iterator();
            while (k.hasNext())
            {
                Marker m = (Marker) k.next();
                m.remove();
            }
            this.altitudeMarkers.clear();
        }

        this.predictionActive = false;

    }


    private Marker produceAltitudeMarker(GoogleMap map,Particle centroid) {
        IconGenerator ig = new IconGenerator(this.context);
        ig.setStyle(IconGenerator.STYLE_WHITE);

        Bitmap bm = ig.makeIcon(String.format("Alt:%.0f ft",centroid.getAltitude()));
        return map.addMarker(new MarkerOptions()
                        .position(centroid.getPosition())
                        .alpha(0.8f)
                        .icon(BitmapDescriptorFactory.fromBitmap(bm))
        );



    }

    private void indicateAltitudes(GoogleMap map, Prediction p) {

        Hashtable <Integer, Particle> altitudes = p.getAltitudes();
        if (altitudes != null)
        {
            if (this.altitudeMarkers == null)
                this.altitudeMarkers = new ArrayList();

            Iterator i = altitudes.keySet().iterator();
            while(i.hasNext()) {
                int t = (int) i.next();

                altitudeMarkers.add(produceAltitudeMarker(map, altitudes.get(t)));
            }
        }

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
