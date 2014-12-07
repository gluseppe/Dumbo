package com.elephant.proga.dumbo.interfaces;

/**
 * Created by gluse on 21/11/14.
 */
public interface PredictionHandler {

    public abstract void onPredictionReceived(Object prediction);
    public abstract void onPredictionFailed(int motivation);

}
