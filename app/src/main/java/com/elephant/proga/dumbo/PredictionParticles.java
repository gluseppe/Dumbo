package com.elephant.proga.dumbo;

import com.elephant.proga.dumbo.interfaces.PredictionViewer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by gluse on 05/11/14.
 */
public class PredictionParticles implements PredictionViewer {

    private ArrayList shapes;

    public void PredictionParticles() {
        this.shapes = null;

    }

    //this method will be called when the prediction results contained in the Object p
    //need to be draw on the map referenced by the GoogleMap object map
    public void drawPrediction(Object p, GoogleMap map) {
        Hashtable<Integer, ArrayList<Particle>> particles = (Hashtable<Integer, ArrayList<Particle>>) p;
        this.shapes = new ArrayList();

        Enumeration<Integer> i = particles.keys();
        ArrayList coords = new ArrayList();

        while (i.hasMoreElements()) {
            ArrayList timesparticles = (ArrayList) particles.get(i.nextElement());
            Iterator j = timesparticles.iterator();
            while(j.hasNext()) {

                Particle part = (Particle) j.next();
                CircleOptions circleOptions = new CircleOptions()
                        .center(part.getPosition())
                        .radius(200)
                        .fillColor(0x40ff0000)
                        .strokeColor(0x40ff0000)
                        .strokeWidth(1);


                this.shapes.add(map.addCircle(circleOptions));
            }
        }




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

    }


}
