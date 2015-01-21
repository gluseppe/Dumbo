package com.elephant.proga.dumbo.viewers;

import android.graphics.Color;

import com.elephant.proga.dumbo.Particle;
import com.elephant.proga.dumbo.Prediction;
import com.elephant.proga.dumbo.interfaces.PredictionViewer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by gluse on 05/11/14.
 */
public class ConnectedParticles implements PredictionViewer {


    public final static String ROUNDSHAPE = "ROUND";
    //only use this when we have information about direction/velocity
    //so we can orient the shape in that direction
    public final static String TRIANGLESHAPE = "TRIANGLE";

    public final static String USESHAPES = ROUNDSHAPE;

    private ArrayList shapes;
    private ArrayList edges;
    private TreeMap predictionOrdered;
    private int n_particles = -1;
    private int n_times = -1;

    private int red_upper_delta = 600;
    private int red_lower_delta = 450;

    private int green_upper_delta = 450;
    private int green_lower_delta = 100;

    private int red_max_color = 255;
    private int red_min_color = 100;

    private int green_max_color = 255;
    private int green_min_color = 0;

    private float red_slope = (red_max_color-red_min_color) / (red_upper_delta-red_lower_delta);
    private float green_slope = (green_max_color-green_min_color) / (green_upper_delta-green_lower_delta);



    public void ConnectedParticles() {
        this.shapes = null;

    }


    public void updatePrediction(LatLng newTargetPosition, LatLng ownShipPosition, double ownShipAltitude) {

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
            this.predictionOrdered = new TreeMap(raw);
            raw.clear();
            raw = null;
            Set times_set = this.predictionOrdered.keySet();
            this.n_times = times_set.size();
            Iterator times = times_set.iterator();

            while(times.hasNext()) {
                //coords.addAll((ArrayList) times.nextElement());
                int time_of_particles = (int) times.next();

                ArrayList particles = (ArrayList) predictionOrdered.get(time_of_particles);
                Iterator i = particles.iterator();
                if (n_particles ==-1) n_particles = particles.size();

                while(i.hasNext())
                {

                    Particle particle = (Particle) i.next();

                    CircleOptions circleOptions = new CircleOptions()
                            .center(particle.getPosition())
                            .radius(300)
                            .fillColor(computeColor(particle.getAltitude(), ownShipAltitude, time_of_particles))
                            .strokeColor(0x40ff0000)
                            .strokeWidth(1)
                            .zIndex(1.0f);


                    this.shapes.add(map.addCircle(circleOptions));

                }

            }

            drawConnections(this.shapes,n_particles, n_times, map, targetPosition);

        }
    }

    private void drawConnections(ArrayList shapes, int n, int n_times, GoogleMap map, LatLng targetPosition) {

        //Iterator i = shapes.iterator();
        //int count = 0;

        if (this.edges == null)
            this.edges = new ArrayList();

        //draws from the target flight position to the first set of shapes
        for (int j=0; j<n; j++) {
            Circle c = (Circle) shapes.get(j);
            LatLng to = c.getCenter();
            this.edges.add(
                    map.addPolyline(new PolylineOptions()
                                    .add(targetPosition, to)
                                    .width(2)
                                    .color(Color.argb(100,150,40,0))
                                    .zIndex(0.0f)
                    )
            );


        }

        //draws from the first group of shapes to the subsequents
        for (int i = 0; i<= n * (n_times-1)-1; i++) {
            Circle c = (Circle) shapes.get(i);
            LatLng from = c.getCenter();
            Circle c_to = (Circle) shapes.get(i+n);
            LatLng to = c_to.getCenter();

            this.edges.add(
                    map.addPolyline(new PolylineOptions()
                                    .add(from, to)
                                    .width(2)
                                    .color(Color.argb(100,150,40,0))
                                    .zIndex(0.0f)
                    )
            );
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

            this.shapes.clear();
        }


        if (this.edges != null)
        {
            Iterator j = this.edges.iterator();
            while(j.hasNext()) {
                Polyline l = (Polyline) j.next();
                l.remove();
            }

            this.edges.clear();
        }


    }

    private int computeColor(double targetAltitude, double ownShipAltitude, int future_time ) {
        double deltaQuote = Math.abs(targetAltitude-ownShipAltitude);

        return Color.argb(compute_alpha(0,2000.0),150,40,0);
        //return Color.argb(compute_alpha(future_time,deltaQuote), compute_red(deltaQuote), compute_green(deltaQuote), compute_blue());

    }


    private int compute_red(double deltaQuote) {
        if(deltaQuote <= red_lower_delta) return red_max_color;

        int inv_red = (int) (red_min_color + red_slope * (deltaQuote - red_lower_delta));
        inv_red = Math.min(inv_red,red_max_color);
        return (red_max_color-inv_red);
    }

    private int compute_green(double deltaQuote) {
        if(deltaQuote >= green_upper_delta) return green_max_color;
        int green = (int) (green_min_color + green_slope * (deltaQuote - green_lower_delta));
        return Math.min(green,green_max_color);
    }

    private int compute_blue() {
        return 0;
    }

    private int compute_alpha(int time, double deltaQuote) {
        return 125;
    }


    private double computeSize() {
        return 300;
    }


}
