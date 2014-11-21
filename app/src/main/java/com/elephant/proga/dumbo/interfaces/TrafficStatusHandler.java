package com.elephant.proga.dumbo.interfaces;

import org.json.JSONObject;

/**
 * Created by gluse on 21/11/14.
 */
public interface TrafficStatusHandler {
    public abstract void onTrafficUpdate(JSONObject jTraffic);
}
