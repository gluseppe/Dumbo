package com.elephant.proga.dumbo.interfaces;

import java.util.HashSet;

/**
 * Created by gluse on 21/01/15.
 */
public interface ConflictHandler {
    public abstract void onConflictDetected(HashSet flightIDs);
}
