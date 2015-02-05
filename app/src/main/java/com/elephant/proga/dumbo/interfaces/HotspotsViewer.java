package com.elephant.proga.dumbo.interfaces;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by gluse on 04/02/15.
 */
public interface HotspotsViewer {

    public abstract void drawHotspots(GoogleMap map, Object hotspots);
    public abstract void updateHotspotTime(GoogleMap map, int secondsx);
    //rimuove gli hotspots, modo:1 rimuove tutti gli hotspots, modo:2 rimuove solo quelli scaduti
    public abstract void removeHotspots(GoogleMap map, int mode);
    public abstract void onHotspotTouched(Marker hotspot, GoogleMap map);


}
