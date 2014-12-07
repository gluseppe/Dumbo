package com.elephant.proga.dumbo;

import android.graphics.Color;

import com.elephant.proga.dumbo.interfaces.PredictionViewer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gluse on 05/11/14.
 */
public class PredictionHeatMap implements PredictionViewer {

    private HeatmapTileProvider heatmapProvider;
    private TileOverlay heatmapTileOverlay;
    private Hashtable<Integer, ArrayList<Particle>> particles;

    public PredictionHeatMap() {
        this.heatmapProvider = null;
        this.heatmapTileOverlay = null;

    }


    public void drawPrediction(Object p, GoogleMap map) {
        Hashtable<String,Prediction> predictions = (Hashtable<String,Prediction>) p;

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
                    coords.add(particle.getPosition());
                }

            }

        }

        addHeatMap(coords,map);
    }

    private void addHeatMap(List list, GoogleMap map) {

        int[] colors = {
                Color.rgb(71, 255, 13), // green
                Color.rgb(232, 223, 12),
                Color.rgb(255, 179, 0),
                Color.rgb(232, 96, 12),
                Color.rgb(255, 2, 0)// red
        };

        float[] startPoints = {
                0.985f, 0.990f, 0.995f, 0.998999983f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        this.heatmapProvider = new HeatmapTileProvider.Builder()
                .data(list)
        //        .radius(20)
        //        .gradient(gradient)
                .build();

        if (this.heatmapTileOverlay != null)
            this.heatmapTileOverlay.clearTileCache();

        // Add a tile overlay to the map, using the heat map tile provider.

        this.heatmapTileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapProvider));

    }



    public void removePrediction(GoogleMap map) {
        if (this.heatmapTileOverlay != null)
            this.heatmapTileOverlay.remove();
    }


}
