package com.elephant.proga.dumbo.interfaces;

import org.json.JSONObject;

/**
 * Created by gluse on 21/11/14.
 */
public interface SelfStatusHandler {

    public abstract void onSelfStatusUpdate(JSONObject jSelf);

}
