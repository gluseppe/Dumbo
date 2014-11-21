package com.elephant.proga.dumbo.interfaces;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by gluse on 05/11/14.
 */
public interface PredictionViewer {

    //this method will be called when the prediction results contained in the Object p
    //need to be draw on the map referenced by the GoogleMap object map
    public void drawPrediction(Object p, GoogleMap map);


    //this method will be called when a visualization of a prediction becomes too old and needs to
    //be removed from the map, the map reference is passed as a parameter
    //The implementer class needs to hold a reference to all the items it placed in the map
    //in order to be able to remove them when this method is called
    public void removePrediction(GoogleMap map);

}
