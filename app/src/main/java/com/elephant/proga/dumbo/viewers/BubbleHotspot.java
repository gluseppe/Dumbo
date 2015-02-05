package com.elephant.proga.dumbo.viewers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.elephant.proga.dumbo.Particle;
import com.elephant.proga.dumbo.interfaces.HotspotsViewer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by gluse on 04/02/15.
 */
public class BubbleHotspot implements HotspotsViewer {

    //bubble markers
    private ArrayList hotspots;
    //indicators
    private ArrayList indicators;
    private JSONArray jhotspots;
    private ArrayList flights;
    private Context context;

    public BubbleHotspot(Context context) {
        this.context = context;
        this.hotspots = new ArrayList();
        this.indicators = new ArrayList();


    }


    private Marker produceHotspotIndicator(GoogleMap map, LatLng position, double altitude_ft, String hs_time) {
        IconGenerator ig = new IconGenerator(this.context);
        ig.setStyle(IconGenerator.STYLE_WHITE);

        Bitmap bm = ig.makeIcon(String.format("%.0f ft\n%s",altitude_ft,hs_time));
        return map.addMarker(new MarkerOptions()
                        .position(position)
                        .alpha(0.8f)
                        .icon(BitmapDescriptorFactory.fromBitmap(bm))
        );
    }

    private Circle produceHotspot(GoogleMap map, LatLng position) {
        CircleOptions circleOptions = new CircleOptions()
                .center(position)
                .radius(2500)
                .fillColor(0x80ff0000)
                .strokeColor(0x80ff0000)
                .strokeWidth(1);

        return map.addCircle(circleOptions);
    }


    @Override
    public void drawHotspots(GoogleMap map, Object hotspots) {
        Log.d("HOTSPOTS", "I WILL NOW DRAW HOTSPOTS ON MAP");
        this.removeHotspots(map, 2);
        if(hotspots != null) {
            this.jhotspots = (JSONArray) hotspots;
            int n = this.jhotspots.length();
            for (int i=0; i<n; i++) {
                try {
                    JSONArray jhs = this.jhotspots.getJSONArray(i);
                    JSONArray flights = jhs.getJSONArray(0);
                    double hs_lat = jhs.getDouble(1);
                    double hs_lon = jhs.getDouble(2);
                    double hs_h = jhs.getDouble(3);
                    String hs_time = jhs.getString(4);
                    LatLng hs_position = new LatLng(hs_lat,hs_lon);
                    Marker m = produceHotspotIndicator(map,hs_position,hs_h,hs_time);
                    m.setTitle("HOTSPOT");
                    this.hotspots.add(produceHotspot(map, hs_position));
                    this.indicators.add(m);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.jhotspots = null;

        }


    }

    @Override
    public void updateHotspotTime(GoogleMap map, int secondsx) {

    }

    @Override
    public void removeHotspots(GoogleMap map, int mode) {
        Iterator i;
        if (this.hotspots != null) {
            i= this.hotspots.iterator();
            while (i.hasNext()) {
                Circle c = (Circle) i.next();
                c.remove();
            }
            this.hotspots.clear();
        }

        if (this.indicators!=null) {
            i = this.indicators.iterator();
            while (i.hasNext()) {
                Marker m = (Marker) i.next();
                m.remove();
            }
            this.indicators.clear();
        }

    }

    @Override
    public void onHotspotTouched(Marker hotspot, GoogleMap map) {

    }
}
