package com.elephant.proga.dumbo.interfaces;

/**
 * Created by gluse on 04/12/14.
 */
public interface Label {

    public abstract void hideLabel();
    public abstract void showLabel();
    public abstract void setPosition(float x, float y);
    public abstract void setFlightID(String flightid);
    public abstract void minimize();
    public abstract void maximize();
    public abstract void setLabelUser(LabelUser labelUser);

}
